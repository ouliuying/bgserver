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

import io.vertx.core.DeploymentOptions
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.*
import io.vertx.core.Vertx
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import work.bg.server.chat.verticle.HttpServerVerticle
import work.bg.server.chat.verticle.ModelClientHubVerticle

@Component
class Server: ApplicationRunner{
    private val logger = LogFactory.getLog(javaClass)
    lateinit var vertx:Vertx
    @Autowired
    lateinit var httpServerVerticle:HttpServerVerticle
    @Autowired
    lateinit var modelClientHubVerticle: ModelClientHubVerticle


    override fun run(args: ApplicationArguments?) {
        logger.info("start chat server at ${Date()}")
        this.initialize()
        val httpServerVerticleOptions=DeploymentOptions()
        this.vertx.deployVerticle(this.httpServerVerticle,httpServerVerticleOptions){
            if(it.succeeded()){
                logger.info("chat server verticle start success")
            }
            else{
                logger.info("chat server verticle start fail reason ${it.cause().toString()}")
            }
        }

        val modelClientHubVerticleOptions = DeploymentOptions()
        this.vertx.deployVerticle(this.modelClientHubVerticle,modelClientHubVerticleOptions){
            if(it.succeeded()){
                logger.info("model client  hub verticle start success")
            }
            else{
                logger.info("model client  hub verticle start fail reason ${it.cause().toString()}")
            }
        }
    }

    private fun initialize(){
        this.vertx= Vertx.vertx()
        ModelClientHub.vertx=this.vertx
    }
}