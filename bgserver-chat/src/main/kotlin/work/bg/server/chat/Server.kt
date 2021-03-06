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

import io.vertx.core.DeploymentOptions
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.*
import io.vertx.core.Vertx
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import work.bg.server.chat.verticle.*

@Component
class Server: ApplicationRunner{
    private val logger = LogFactory.getLog(javaClass)
    lateinit var vertx:Vertx
    @Autowired
    lateinit var httpServerVerticle:HttpServerVerticle
    @Autowired
    lateinit var modelClientHubVerticle: ModelClientHubVerticle
    @Autowired
    lateinit var redisReceiveServerVerticle:RedisReceiveServerVerticle
    @Autowired
    lateinit var redisSendServerVerticle: RedisSendServerVerticle

    @Autowired
    lateinit var persistLogServerVerticle: PersistLogServerVerticle


    override fun run(args: ApplicationArguments?) {
        logger.info("start chat server at ${Date()}")
        this.initialize()



        val persistLogServerVerticleOptions = DeploymentOptions()
        persistLogServerVerticleOptions.isWorker=true
        this.vertx.deployVerticle(this.persistLogServerVerticle,persistLogServerVerticleOptions){
            if(it.succeeded()){
                logger.info("message log persist server verticle start success")
            }
            else{
                logger.info("message log persist server verticle start failed reason ${it.cause()}")
            }
        }

        val modelClientHubVerticleOptions = DeploymentOptions()
        this.vertx.deployVerticle(this.modelClientHubVerticle,modelClientHubVerticleOptions){
            if(it.succeeded()){
                logger.info("model client  hub verticle start success")
            }
            else{
                logger.info("model client  hub verticle start fail reason ${it.cause()}")
            }
        }


        this.vertx.deployVerticle(this.redisSendServerVerticle,DeploymentOptions(

        )){
            if(it.succeeded()){
                this.logger.info("redis sender server start success")
            }
            else{
                this.logger.info("redis sender server start failed")
            }
        }

        this.vertx.deployVerticle(this.redisReceiveServerVerticle,DeploymentOptions(

        )){
            if(it.succeeded()){
                this.logger.info("redis receive server start success")
            }
            else{
                this.logger.info("redis receive server start failed")
            }
        }




        val httpServerVerticleOptions=DeploymentOptions()
        this.vertx.deployVerticle(this.httpServerVerticle,httpServerVerticleOptions){
            if(it.succeeded()){
                logger.info("chat server verticle start success")
            }
            else{
                logger.info("chat server verticle start fail reason ${it.cause()}")
            }
        }

    }

    private fun initialize(){
        this.vertx= Vertx.vertx()
        ModelClientHub.vertx=this.vertx
    }
}