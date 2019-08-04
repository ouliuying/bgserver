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

package work.bg.server.chat.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.bridge.PermittedOptions
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import work.bg.server.chat.ChatEventBusConstant


@Component
class HttpServerVerticle:AbstractVerticle() {
    private val logger = LogFactory.getLog(javaClass)
    lateinit var httpServer:HttpServer
    @Value("\${bg.chat-server.port}")
    var port:Int=8080
    override fun start() {
        var option = HttpServerOptions()
        option.port=port
        this.httpServer=this.vertx.createHttpServer(option)

        val router = Router.router(vertx)

        val options = BridgeOptions().addInboundPermitted(PermittedOptions().setAddress(ChatEventBusConstant.CLIENT_TO_SERVER_ADDRESS))
                                     .addOutboundPermitted(PermittedOptions().setAddressRegex(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_PATTERN))

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options))

        this.httpServer.requestHandler(router).listen{
            if(it.succeeded()){
                this.logger.info("chat http server port $port success")
            }
            else{
                this.logger.info("chat http server port $port fail")
            }
        }
        val eb = vertx.eventBus()
        eb.consumer<JsonObject>("chat.to.server").handler { message ->
           eb.publish("chat.to.server.message",message)
        }
    }

    override fun stop() {

    }
}