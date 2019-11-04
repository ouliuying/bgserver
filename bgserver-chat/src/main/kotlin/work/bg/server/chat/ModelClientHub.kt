/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *it under the terms of the GNU Affero General Public License as published by
t *  *  *he Free Software Foundation, either version 3 of the License.

 *  *  *This program is distributed in the hope that it will be useful,
 *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *GNU Affero General Public License for more details.

 *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  *
  *
  */

package work.bg.server.chat

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.kotlin.redis.client.redisOptionsOf
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions
import org.apache.commons.logging.LogFactory
import java.net.URI
import org.bouncycastle.crypto.tls.ConnectionEnd.server
import work.bg.server.chat.ChannelConsumer
import work.bg.server.chat.ModelClientChatSession
import work.bg.server.chat.model.ChatPartner


object ModelClientHub {
    lateinit var vertx: Vertx
    lateinit var redisUrl:String
    private val logger = LogFactory.getLog(javaClass)
    private var sessionIDToUUIDMap =  HashMap<String,String>()
    private var chatUUIDToSessionMap = HashMap<String,ModelClientChatSession>()
    private val channelConsumerMap= HashMap<String,ChannelConsumer> ()
    private const val TRY_CONNECT_REDIS_LIMIT =10
    fun getModelClientChatSessionBySessionID(chatSessionID:String,refreshTimeout:Boolean=true):ModelClientChatSession?{
        val uuid= this.sessionIDToUUIDMap[chatSessionID]
        return uuid?.let {
            val mccs = chatUUIDToSessionMap[uuid]
            if(refreshTimeout){
                mccs?.let {
                    mccs.refreshTimeout(chatSessionID)
                }
            }
            return mccs
        }
    }

    fun getModelClientChatSessionByChatUUID(chatUUID:String,
                                  refreshTimeout: Boolean=true):ModelClientChatSession?{
        val mccs = this.chatUUIDToSessionMap[chatUUID]
        if(refreshTimeout){
            mccs?.let {
                mccs.refreshTimeout()
            }
        }
        return mccs
    }
    fun registerClient(chatSessionID: String){
        this.readModelClientChatSessionFromRedis(chatSessionID, Handler {
            if(it.succeeded()){
                val mccs = it.result()
                mccs?.let {
                    this.rebuildModelClientChatSession(mccs)
                    this.sessionIDToUUIDMap[chatSessionID] = mccs.chatUUID
                    this.rebuildChannelConsumerMap(mccs)
                }
            }
            else{
                this.logger.trace("read $chatSessionID model client session failed")
            }
        })
    }
    private fun rebuildModelClientChatSession(mccs:ModelClientChatSession){
        val oldMccs = this.chatUUIDToSessionMap[mccs.chatUUID]
        if(oldMccs!=null){
            oldMccs.deviceSessionIDArray.removeIf { x->x.devType == mccs.deviceSessionIDArray.firstOrNull()?.devType }
            oldMccs.deviceSessionIDArray.addAll(mccs.deviceSessionIDArray)
            mccs.deviceSessionIDArray=oldMccs.deviceSessionIDArray
        }
        else{
            this.chatUUIDToSessionMap[mccs.chatUUID]=mccs
        }
    }
    private fun rebuildChannelConsumerMap(mccs:ModelClientChatSession){
        var channels = JsonArray(mccs.channelMeta)
        channels.forEach {
            if(it is JsonObject){
                this.logger.trace(it.toString())
                val id = it.getLong("id")
                val name = it.getString("name")
                val uuid = it.getString("uuid")
                val defaultFlag = it.getInteger("defaultFlag")
                val broadcastType = it.getInteger("broadcastType")
                val icon = it.getString("icon")
                if(this.channelConsumerMap.containsKey(uuid)){
                    var ccm = this.channelConsumerMap[uuid]
                    ccm?.broadcastType=broadcastType
                    ccm?.channelDefaultFlag = defaultFlag
                    ccm?.channelName = name
                    ccm?.icon=icon
                    ccm?.chatUUIDSet?.add(mccs.chatUUID)
                    if(ccm?.Consumer?.isRegistered!=true){
                       ccm?.Consumer = this.vertx.eventBus()
                               .consumer<JsonObject>("${ChatEventBusConstant.INNER_SERVER_CHANNEL_ADDRESS_HEADER}${uuid}")
                               .handler {
                            this.sendMessageToChannel(uuid,it.body())
                       }
                    }
                }
                else{
                    var channelConsumer = ChannelConsumer(id,uuid,name,defaultFlag,broadcastType,icon)
                    channelConsumer.chatUUIDSet.add(mccs.chatUUID)
                    channelConsumer.Consumer=this.vertx.eventBus()
                            .consumer<JsonObject>("${ChatEventBusConstant.INNER_SERVER_CHANNEL_ADDRESS_HEADER}${uuid}")
                            .handler{
                                this.sendMessageToChannel(uuid,it.body())
                            }
                    this.channelConsumerMap[uuid]=channelConsumer
                }
            }
        }
    }

    private fun sendMessageToChannel(channelUUID:String,msg:JsonObject?){
        if(msg==null){
            return
        }
        this.channelConsumerMap[channelUUID]?.let {
            val fromUUID = msg.getString(ChatEventBusConstant.CHAT_FROM_UUID)
            val toUUID = msg.getString(ChatEventBusConstant.CHAT_TO_UUID)
            val sessionID = msg.getString(ChatEventBusConstant.CHAT_SESSION_ID)
            if(toUUID.isNullOrBlank() || toUUID.isNullOrEmpty()){
                it.chatUUIDSet.forEach {itToUUID->
                    this.sendMessageToClient(fromUUID,itToUUID,sessionID,msg)
                }
            }
            else{
                if(it.chatUUIDSet.contains(toUUID)){
                    this.sendMessageToClient(fromUUID,toUUID,sessionID,msg)
                }
            }
        }
    }

    private fun sendMessageToClient(fromUUID:String,
                                    toUUID:String,
                                    sessionID:String,
                                    msg:JsonObject){
        msg.put(ChatEventBusConstant.CHAT_TO_UUID, toUUID)
        msg.remove(ChatEventBusConstant.CHAT_SESSION_ID)
        if(toUUID!= fromUUID) {
            this.chatUUIDToSessionMap[toUUID]?.let {
                it.deviceSessionIDArray.forEach { ds ->
                    this.vertx.eventBus().publish("${ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_HEADER}${ds.chatSessionID}", msg)
                }
            }
        }
        this.chatUUIDToSessionMap[fromUUID]?.let {
            it.deviceSessionIDArray.forEach { ds->
                if(ds.chatSessionID!=sessionID){
                    this.vertx.eventBus().publish("${ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_HEADER}${ds.chatSessionID}", msg)
                }
            }
        }
    }
    private fun readModelClientChatSessionFromRedis(chatSessionID: String,
                                                    handler:Handler<AsyncResult<ModelClientChatSession>>){
        var tryCount = 0
        this.getRegModelClientChatSessionFromRedis(chatSessionID,
                Handler<AsyncResult<ModelClientChatSession>>{
            if(it.succeeded()){
                handler.handle(it)
            }
            else if(it.failed()){
                tryCount+=1
                if(tryCount<=TRY_CONNECT_REDIS_LIMIT){
                    this.vertx.setTimer(1000){
                        this.readModelClientChatSessionFromRedis(chatSessionID,handler)
                    }
                }
                else{
                    handler.handle(Future.failedFuture(it.cause()))
                }
            }
        })
    }
    fun removeModelClientChatSession(chatSessionID: String){
        val mccs = this.getModelClientChatSessionBySessionID(chatSessionID,
                false)
        mccs?.let {
            this.sessionIDToUUIDMap.remove(chatSessionID)
            mccs.removeDeviceSessionID(chatSessionID)
        }
    }

    private fun getRegModelClientChatSessionFromRedis(chatSessionID: String,
                                              handler:Handler<AsyncResult<ModelClientChatSession>>){
        this.logger.trace("start get chat session id $chatSessionID relation data from redis")
        val options = redisOptionsOf(
                endpoint = this.redisUrl
        )
        Redis.createClient(this.vertx, options)
                .connect { onConnect ->
                    if (onConnect.succeeded()) {
                        val client = onConnect.result()
                        val redisApi = RedisAPI.api(client)
                        redisApi.hgetall(chatSessionID){
                            if(it.succeeded()){
                                val resp = it.result()
                                val len = resp.size()
                                if(len>0){
                                    this.logger.trace(resp.toString())
                                }
                                var corpID=null as Long?
                                var model = null as String?
                                var modelID=null as Long?
                                var devType=null as Int?
                                var channelMeta =null as String?
                                var chatUUID = null as String?
                                for(i in 0..(len-2)/2){
                                    val key = resp[i*2].toString()
                                    val value = resp[i*2+1]
                                    when (key) {
                                        "corpID" -> corpID = value?.toLong()
                                        "model" -> model=value?.toString()
                                        "modelID" -> modelID = value?.toLong()
                                        "devType" -> devType = value?.toInteger()
                                        "chatUUID" ->chatUUID = value?.toString()
                                    }
                                }
                                this.vertx.executeBlocking<String?>({ promise ->
                                    // Call some blocking API that takes a significant amount of time to return
                                    try {
                                        val result = when{
                                            model?.compareTo("partner",true)==0->{
                                                ChatPartner.ref.getPartnerChannelMeta(partnerID = modelID!!,
                                                        corpID = corpID!!)
                                            }
                                            else->null
                                        }
                                        promise.complete(result)
                                    }
                                    catch (ex:Exception){
                                        promise.fail(ex.message)
                                    }
                                }, { res ->
                                    if(res.succeeded()){
                                        channelMeta = res.result()

                                        this.logger.info("corpID = $corpID model = $model modelID = $modelID devType = $devType channelMeta = $channelMeta chatUUID = $chatUUID")
                                        if(corpID!=null && model!=null && modelID!=null && devType!=null && chatUUID!=null){
                                            val mccs = ModelClientChatSession(model,
                                                    corpID,
                                                    modelID,
                                                    chatUUID,
                                                    channelMeta,
                                                    arrayListOf(ModelClientChatDeviceSessionID(devType,chatSessionID)
                                                    ))
                                            mccs.vertx = this.vertx
                                            handler.handle(Future.succeededFuture(mccs))
                                        }
                                        else{
                                            handler.handle(Future.failedFuture(resp.toString()))
                                        }
                                    }
                                    else{
                                        this.logger.trace("read channel meta failed")
                                        handler.handle(Future.failedFuture("read channel meta failed"))
                                    }
                                })
                            }
                            else{
                                this.logger.trace("get chat sesion id $chatSessionID relation data failed,cause:${it.cause()}")
                                handler.handle(Future.failedFuture(it.cause()))
                            }
                        }
                    }
                    else{
                        this.logger.trace("get chat session id $chatSessionID relation data from redis failed,cause: ${onConnect.cause()}")
                        handler.handle(Future.failedFuture(onConnect.cause()))
                    }
         }
    }
}