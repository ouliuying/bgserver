/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.chat.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.redis.client.Command
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.Request
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import work.bg.server.chat.ChatEventBusConstant
import java.lang.Exception

@Component
class RedisSendServerVerticle:AbstractVerticle() {
    @Value("\${bg.chat.redis.url}")
    private lateinit var redisUrl:String
    private val logger = LogFactory.getLog(javaClass)
    override fun start() {
      this.startSendRedisServer()
    }
    private  fun startSendRedisServer(){
        Redis.createClient(this.vertx,this.redisUrl).connect {
            if(it.succeeded()){
                it.result()?.let {
                    it.handler {msg->
                        try{
                            this.logger.trace("redis receive msg = $msg")
                            val msgBody = msg[2].toString()
                            this.logger.trace("real message = ${msgBody}")
                            val msgObj = JsonObject(msgBody)
                            val channelUUID = msgObj.getString(ChatEventBusConstant.CHANNEL_UUID)
                            channelUUID?.let {

                                this.vertx.eventBus().publish("${ChatEventBusConstant.INNER_SERVER_CHANNEL_ADDRESS_HEADER}$channelUUID",
                                        msgObj)
                            }
                        }
                        catch (ex:Exception){
                            this.logger.error(ex.message)
                        }

                    }.send(Request.cmd(Command.SUBSCRIBE).arg(ChatEventBusConstant.INNER_SERVER_MESSAGE_REDIS_QUEUE)){
                        if(it.succeeded()){
                            this.logger.trace(it.result().toString())
                        }
                        else{
                            this.logger.trace(it.cause().toString())
                        }
                    }
                    it.exceptionHandler {
                        this.restartSendRedisServer()
                    }
                }
            }
            else{
                this.restartSendRedisServer()
            }
        }.exceptionHandler {
            this.restartSendRedisServer()
        }
    }
    private  fun restartSendRedisServer(){
        this.vertx.setTimer(10000){
            startSendRedisServer()
        }
    }
    override fun stop() {

    }
}