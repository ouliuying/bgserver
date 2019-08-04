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
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.springframework.stereotype.Component
import work.bg.server.chat.ChatEventBusConstant
import work.bg.server.chat.ModelClientChatSession
import work.bg.server.chat.ModelClientHub

@Component
class ModelClientHubVerticle: AbstractVerticle() {
    override fun start() {
        val eb = vertx.eventBus()
        eb.consumer<JsonObject>("chat.to.server.message"){ message ->
            message.body()?.let {
                if(!it.isEmpty){
                    val chatSessionID = it.getString(ChatEventBusConstant.CHAT_SESSION_ID)
                    chatSessionID?.let {
                        if(chatSessionID.isNotEmpty() &&
                                chatSessionID.isBlank()){
                           val mccs =  ModelClientHub.getModelClientChatSessionBySessionID(chatSessionID,true)
                            if(mccs!=null){
                                mccs.dispatchMessage(message)
                            }
                            else{
                                 ModelClientHub.getRegModelClientChatSessionFromRedis(chatSessionID, Handler<AsyncResult<ModelClientChatSession>> {
                                     if(it.succeeded()){
                                         it.result().dispatchMessage(message)
                                     }
                                 })
                            }
                        }
                    }
                }
            }
        }
    }

    override fun stop() {

    }
}