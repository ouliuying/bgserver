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
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import work.bg.server.chat.ChatEventBusConstant
import work.bg.server.chat.kafka.PersistChatMessage

@Component
class PersistLogServerVerticle:AbstractVerticle() {
    @Autowired
    lateinit var persistStore:PersistChatMessage
    override fun start() {
        this.vertx.eventBus().consumer<JsonObject>(ChatEventBusConstant.INNER_SERVER_REDIS_IN_QUEUE_ADDRESS){
            it.body()?.let { msgObj->
                persistStore.write(msgObj.toString(), Handler {

                })
            }
        }

    }

    override fun stop() {


    }
}