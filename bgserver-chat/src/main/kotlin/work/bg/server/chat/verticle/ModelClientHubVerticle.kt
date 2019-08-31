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
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import work.bg.server.chat.ChatEventBusConstant
import work.bg.server.chat.ModelClientChatSession
import work.bg.server.chat.ModelClientHub

@Component
class ModelClientHubVerticle: AbstractVerticle() {
    @Value("\${bg.chat.redis.url}")
    private lateinit var redisUrl:String
    private val logger = LogFactory.getLog(javaClass)
    override fun start() {
        val eb = vertx.eventBus()
        ModelClientHub.redisUrl=this.redisUrl
        eb.consumer<String>(ChatEventBusConstant.INNER_REGISTER_CLIENT_TO_SERVER).handler{ sessionID ->
            this.logger.trace("${ChatEventBusConstant.INNER_REGISTER_CLIENT_TO_SERVER} receive $sessionID")
            if(!sessionID.body().isNullOrBlank() && !sessionID.body().isNullOrEmpty()){
                val chatSessionID = sessionID.body()
                if(chatSessionID.isNotEmpty() &&
                        chatSessionID.isNotBlank()){
                        ModelClientHub.registerClient(chatSessionID)
                }
            }
        }
    }

    override fun stop() {

    }
}