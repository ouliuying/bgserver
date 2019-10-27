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

package work.bg.server.chat.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.bridge.PermittedOptions
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.redis.client.Command
import io.vertx.redis.client.Redis
import io.vertx.redis.client.Request
import work.bg.server.chat.ChatEventBusConstant
import work.bg.server.chat.message.MessageResponseType
import java.util.*
import java.util.Date


@Component
class HttpServerVerticle:AbstractVerticle() {
    private val logger = LogFactory.getLog(javaClass)
    lateinit var httpServer:HttpServer
    private val sessionIDToFromUUID = mutableMapOf<String,String>()
    private  val sockHandlerIDToSessionID = mutableMapOf<String,String>()
    @Value("\${bg.chat-server.port}")
    var port:Int=8080
    @Value("\${bg.chat.redis.url}")
    private lateinit var redisUrl:String

    private fun registerClientToHub(sessionID:String?,handler: Handler<AsyncResult<Boolean>>){
        if(sessionID!=null){
            val eb = this.vertx.eventBus()
            eb.publish(ChatEventBusConstant.INNER_REGISTER_CLIENT_TO_SERVER,sessionID)
        }
        handler.handle(Future.succeededFuture(true))
    }

    private  fun checkAndRegisterClientSessionID(sessionID: String,be:BridgeEvent,handler:Handler<AsyncResult<Boolean>>){
        Redis.createClient(this.vertx,this.redisUrl).connect {
            if(it.succeeded()){
                val redisClient = it.result()
                redisClient?.send(Request.cmd(Command.EXISTS).arg(sessionID)){
                    if(it.succeeded()){
                        val ret =  it.result().toInteger()
                        if(ret==1){
                            val  sockHandlerID = be.socket().writeHandlerID()
                            val  fromUUID = be.rawMessage?.getJsonObject("headers")?.getString(ChatEventBusConstant.CHAT_FROM_UUID)
                            this.sessionIDToFromUUID[sessionID]=fromUUID?:""
                            this.sockHandlerIDToSessionID[sockHandlerID] = sessionID
                            this.logger.trace("sockHanderID = $sockHandlerID fromUUID = $fromUUID sesssionID = $sessionID")
                            this.registerClientToHub(sessionID,
                                    Handler<AsyncResult<Boolean>> {
                                        handler.handle(Future.succeededFuture(true))
                                    })
                        }
                        else{
                            handler.handle(Future.failedFuture("无对应sessionID，不能注册通讯节点"))
                        }
                    }
                    else{
                        handler.handle(Future.failedFuture("redis通讯失败：${it.cause().toString()}"))
                    }
                }?.exceptionHandler {
                    handler.handle(Future.failedFuture(it))
                }
            }
            else{
                handler.handle(Future.failedFuture(it.cause()))
            }
        }.exceptionHandler {
            handler.handle(Future.failedFuture(it))
        }
    }
    override fun start() {
        var option = HttpServerOptions()
        option.port=port
        option.host="0.0.0.0"
        this.httpServer=this.vertx.createHttpServer(option)

        val router = Router.router(vertx)

        val options = BridgeOptions()
                .addInboundPermitted(PermittedOptions().setAddress(ChatEventBusConstant.CLIENT_TO_SERVER_ADDRESS))
                .addOutboundPermitted(PermittedOptions().setAddressRegex(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_PATTERN))
                .addInboundPermitted(PermittedOptions().setAddressRegex(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_PATTERN))

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options){be->
            vertx.runOnContext {
                try {
                    val eb = vertx.eventBus()
                    be?.let {
                        when(be.type()){
                            BridgeEventType.REGISTERED->{
                                val sessionID = be.rawMessage?.getString(ChatEventBusConstant.ADDRESS_KEY)?.substringAfterLast(".")
                                if(!(sessionID.isNullOrEmpty() || sessionID.isNullOrBlank())){
                                    this.checkAndRegisterClientSessionID(sessionID,be, Handler {
                                        if(it.succeeded()){
                                            be.complete(true)
                                        }
                                        else{
                                            be.tryFail("check session failed")
                                        }

                                    })
                                }
                                else{
                                    be.tryFail("cant get session id")
                                }
                            }
                            else->{
                                be.complete(true)
                            }
                        }
                    }
                }
                catch (ex:Exception){
                    ex.printStackTrace()
                    if(!be.future().isComplete){
                        be.tryFail(ex)
                    }
                }

            }
        })

        this.httpServer.requestHandler(router).listen{
            if(it.succeeded()){
                this.logger.info("chat http server port $port success")
                val eb = vertx.eventBus()
                eb.consumer<JsonObject>(ChatEventBusConstant.CLIENT_TO_SERVER_ADDRESS).handler { message ->
                    val msgObj = message.body()
                    val sessionID = msgObj?.getString(ChatEventBusConstant.CHAT_SESSION_ID)
                    if(!(sessionID.isNullOrBlank() || sessionID.isNullOrEmpty())){
                        val fromUUID = this.sessionIDToFromUUID[sessionID]
                        if(fromUUID!=null){
                            msgObj.put(ChatEventBusConstant.CHAT_FROM_UUID,fromUUID)
                            this.logger.trace("redirect receive client message to ${ChatEventBusConstant.INNER_SERVER_REDIS_IN_QUEUE_ADDRESS} ${msgObj?.toString()}")
                            val resp = createUpdateUUIDResponseMessage(msgObj)
                            resp?.let {
                                eb.publish("${ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_HEADER}$sessionID",
                                        resp)
                            }
                            this.logger.trace("server receive msg ${msgObj?.toString()}")
                            eb.publish(ChatEventBusConstant.INNER_SERVER_REDIS_IN_QUEUE_ADDRESS,msgObj)
                        }
                    }

                }
            }
            else{
                this.logger.info("chat http server port $port fail")
            }
        }

    }

    private fun createUpdateUUIDResponseMessage(message:JsonObject):JsonObject?{
        val seq =message.getInteger("seq",0)
        val responseType = message.getInteger("responseType",-1)
        if(responseType!=MessageResponseType.NONE_TYPE.typ){
            val uuid = UUID.randomUUID().toString()
            this.logger.trace("new uuid = $uuid")
            message.put(ChatEventBusConstant.CHAT_MESSAGE_UUID,uuid)
            val channelUUID = message.getString(ChatEventBusConstant.CHANNEL_UUID)
            val toUUID = message.getString(ChatEventBusConstant.CHAT_TO_UUID)
            return JsonObject(mapOf(
                    "type" to  MessageResponseType.UPDATE_MESSAGE_UUID_TYPE.typ,
                    "isResponse" to 1,
                    "seq" to  seq,
                    "toUUID" to toUUID,
                    "uuid" to  uuid,
                    "channelUUID" to channelUUID,
                    "timestamp" to Date().toInstant().epochSecond
            ))
        }
        return null
    }

    override fun stop() {

    }
}