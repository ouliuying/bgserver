/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.chat

import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.redis.client.*
import org.apache.commons.logging.LogFactory

class ModelClientRedis(var redis:Redis,channelMeta:String?) {
    val logger = LogFactory.getLog(javaClass)
    lateinit var channels:MutableMap<String,ChatChannel>
    lateinit var chatSession: ModelClientChatSession
    init {
        this.redis.endHandler {
            this.reStart()
        }
        this.redis.handler{
            this.receiveScribeResponse(it)
        }
    }
    private fun receiveScribeResponse(resp:Response?){
        resp?.let {
            val msg = when(it.type()){
                ResponseType.SIMPLE->it.toString()
                else->null
            }
            msg?.let {
                val eb= this.chatSession.vertx.eventBus()
                val msgObj = JsonObject(msg)
                val fromChatSessionID = msgObj.getString(ChatEventBusConstant.CHAT_SESSION_ID)
                val fromUUID = msgObj.getString(ChatEventBusConstant.CHAT_FROM_UUID)
                this.chatSession.deviceSessionIDArray.forEach {ds->
                    if(fromChatSessionID!=ds.chatSessionID){
                        eb.publish(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_HEADER+ds.chatSessionID,msgObj)
                    }
                }
            }
        }
    }
    private fun reStart(){
        this.logger.info("reconnect redis ..")
        val options = RedisOptions()
        Redis.createClient(ModelClientHub.vertx, options).connect {
            if(it.succeeded()){
                this.redis = it.result()
                this.redis.endHandler {
                    this.reStart()
                }
                this.redis.handler {
                    this.receiveScribeResponse(it)
                }
                this.subscribe(Handler<AsyncResult<UInt>> {
                    if(it.failed()){
                        this.redis.close()
                    }
                })
            }
            else{
                ModelClientHub.vertx.setTimer(5000) {
                  reStart()
                }
            }
        }
    }

    fun subscribe(handler: Handler<AsyncResult<UInt>>){
        val redisApi = RedisAPI.api(this.redis)
        var patterns = this.channels.values.filter {
            it.isPattern
        }.map { it.pattern }

        var channels = this.channels.values.filter {
            !it.isPattern
        }.map { it.name }
        if(patterns.isNotEmpty() && channels.isNotEmpty()){
            CompositeFuture.join(redisApi.subscribe(channels), redisApi.psubscribe(patterns)).setHandler {
                if(it.succeeded()){
                    handler.handle(Future.succeededFuture())
                }
                else{
                    handler.handle(Future.failedFuture(it.cause()))
                }
            }
        }
        else if(patterns.isNotEmpty()){
            redisApi.psubscribe(patterns).setHandler {
                if(it.succeeded()){
                    handler.handle(Future.succeededFuture())
                }
                else{
                    handler.handle(Future.failedFuture(it.cause()))
                }
            }
        }
        else if(channels.isNotEmpty()){
            redisApi.subscribe(channels).setHandler {
                if(it.succeeded()){
                    handler.handle(Future.succeededFuture())
                }
                else{
                    handler.handle(Future.failedFuture(it.cause()))
                }
            }
        }
        else{
            handler.handle(Future.succeededFuture())
        }
    }
    private fun getP2PChatToUUID(message:Message<JsonObject>):String?{
        return  message.body().getString(ChatEventBusConstant.CHAT_TO_UUID)
    }

    private fun p2pPublish(ch:ChatChannel,
                           message: Message<JsonObject>,
                           chatSession:ModelClientChatSession){
        val chatUUID = this.getP2PChatToUUID(message)
        chatUUID?.let {
            val activeToChatSession = ModelClientHub.getModelClientChatSessionByChatUUID(chatUUID)
            activeToChatSession?.let {
                val eb = it.vertx.eventBus()
                it.deviceSessionIDArray.forEach {
                    ds->
                    var msgBody = message.body()
                    msgBody.put(ChatEventBusConstant.CHAT_FROM_UUID,chatSession.chatUUID)
                    eb.publish(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_HEADER+ds.chatSessionID,msgBody)
                }
            }
        }

    }
    private fun isP2PMessage(ch:ChatChannel,
                             message: Message<JsonObject>):Boolean{
        return ch.isP2P
    }
    private fun persistMessage(ch:ChatChannel,
                               message: Message<JsonObject>,
                               chatSession:ModelClientChatSession){

    }
    fun publish(message: Message<JsonObject>,
                chatSession:ModelClientChatSession){
        val channelTag = message.body().getString(ChatEventBusConstant.CHANNEL_TAG)
        channelTag?.let {
            val ch = this.channels[channelTag]
            ch?.let {
                this.persistMessage(ch,message,chatSession)
                if(!this.isP2PMessage(ch,message)){
                    this.p2pPublish(ch,message,chatSession)
                }
                else{
                    val redisApi = RedisAPI.api(this.redis)
                    val msgBody = message.body()
                    msgBody.put(ChatEventBusConstant.CHAT_FROM_UUID,chatSession.chatUUID)
                    redisApi.publish(ch.name,msgBody.toString())
                }
            }
        }
    }
}