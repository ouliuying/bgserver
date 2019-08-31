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
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.redis.client.redisOptionsOf
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
class RedisReceiveServerVerticle:AbstractVerticle() {
    private val logger = LogFactory.getLog(javaClass)
    @Value("\${bg.chat.redis.url}")
    private lateinit var redisUrl:String
    var redisClient:Redis?=null
    override fun start() {
        this.createRedisClient()
        this.vertx.eventBus()
                .consumer<JsonObject>(ChatEventBusConstant.INNER_SERVER_REDIS_IN_QUEUE_ADDRESS)
                .handler {
                    val msgBody = it.body()
                    this.logger.trace("${ChatEventBusConstant.INNER_SERVER_REDIS_IN_QUEUE_ADDRESS} receive ${msgBody?.toString()}")
                    if(redisClient!=null) {
                        try {
                            this.redisClient?.send(Request
                                    .cmd(Command.PUBLISH)
                                    .arg(ChatEventBusConstant.INNER_SERVER_MESSAGE_REDIS_QUEUE)
                                    .arg(it.body().toString())
                            ){
                                if(it.succeeded()){
                                    this.logger.trace("publish to ${ChatEventBusConstant.INNER_SERVER_MESSAGE_REDIS_QUEUE} data = ${msgBody?.toString()} ret = ${it.result().toString()}")
                                }
                                else{
                                    this.logger.trace("publish to ${ChatEventBusConstant.INNER_SERVER_MESSAGE_REDIS_QUEUE} data = ${msgBody?.toString()} ret = ${it.cause().toString()}")
                                }
                            }
                        }
                        catch (ex:Exception){
                            this.logger.trace(ex.toString())
                        }
                    }
                    else{
                        this.logger.trace("connect redis failed")
                        val msg = it.body()
                        val channelUUID = msg.getString(ChatEventBusConstant.CHANNEL_UUID)
                        channelUUID?.let {
                            this.vertx.eventBus().publish("${ChatEventBusConstant.INNER_SERVER_CHANNEL_ADDRESS_HEADER}${channelUUID}",msg)
                        }
                    }
        }

    }
    private fun createRedisClient(){
        val options = redisOptionsOf(
                endpoint = this.redisUrl
        )
        Redis.createClient(this.vertx,options).connect {
            if(it.succeeded()){
                this.redisClient = it.result()
                this.redisClient?.exceptionHandler {
                    this.redisClient=null
                    this.retryCreateRedisClient()
                }
                this.logger.trace("receive server connect redis successful!")
            }
            else if(it.failed()){
                this.redisClient = null
                this.retryCreateRedisClient()
                this.logger.trace("receive server connect redis failed!")
            }
        }
    }
    private fun retryCreateRedisClient(){
        this.vertx.setTimer(10000, Handler<Long>{
                this.createRedisClient()
        })
    }

    override fun stop() {

    }
}