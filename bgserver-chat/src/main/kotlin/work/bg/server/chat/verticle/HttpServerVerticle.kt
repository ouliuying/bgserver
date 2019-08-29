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

        val options = BridgeOptions()
                .addInboundPermitted(PermittedOptions().setAddress(ChatEventBusConstant.CLIENT_TO_SERVER_ADDRESS))
                .addOutboundPermitted(PermittedOptions().setAddressRegex(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_PATTERN))
                .addInboundPermitted(PermittedOptions().setAddressRegex(ChatEventBusConstant.SERVER_TO_CLIENT_ADDRESS_PATTERN))

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options))

        this.httpServer.requestHandler(router).listen{
            if(it.succeeded()){
                this.logger.info("chat http server port $port success")
                val eb = vertx.eventBus()
                eb.consumer<JsonObject>(ChatEventBusConstant.CLIENT_TO_SERVER_ADDRESS).handler { message ->
                    this.logger.info(message.body().toString())
                    eb.publish("chat.to.server.message",message.body())
                }
            }
            else{
                this.logger.info("chat http server port $port fail")
            }
        }

    }

    override fun stop() {

    }
}