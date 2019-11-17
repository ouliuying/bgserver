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

package work.bg.server.chat.kafka

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import work.bg.server.chat.ChatEventBusConstant
import work.bg.server.chat.model.ChatChannelMessage
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.eq
import work.bg.server.chat.model.ChatChannel
import work.bg.server.util.TypeConvert
import java.lang.Exception

@Service
class PersistChatMessage {
    private val logger = LogFactory.getLog(javaClass)
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    companion object{
        const val CHAT_MESSAGE = "chatMessage"
    }

    fun write(msg:String,handler:Handler<AsyncResult<Boolean>>){
        this.kafkaTemplate.send(CHAT_MESSAGE,msg).addCallback(
                object:ListenableFutureCallback<SendResult<String?,String?>>{
                    override fun onFailure(ex: Throwable) {
                         handler.handle(Future.failedFuture(ex))
                    }

                    override fun onSuccess(result: SendResult<String?, String?>?) {
                        handler.handle(Future.succeededFuture(true))
                    }
                }
        )
    }

    @KafkaListener(topics = [CHAT_MESSAGE], groupId = "main")
    fun flushToDatabase(msg:String?){
        msg?.let {
            try {
                val msgObj = JsonObject(it)
                var mo = dynamic.model.query.mq.ModelDataObject(model = ChatChannelMessage.ref)
                val channelUUID = msgObj.getString(ChatEventBusConstant.CHANNEL_UUID)
                var channelObj = ChatChannel.ref.rawRead(criteria = eq(ChatChannel.ref.uuid,channelUUID))?.firstOrNull()
                val channelID =TypeConvert.getLong(channelObj?.idFieldValue?.value as Number?)
                val fromUUID = msgObj.getString(ChatEventBusConstant.CHAT_FROM_UUID)
                val toUUID = msgObj.getString(ChatEventBusConstant.CHAT_TO_UUID)
                val uuid = msgObj.getString(ChatEventBusConstant.CHAT_MESSAGE_UUID)
                mo.setFieldValue(ChatChannelMessage.ref.channelUUID,channelUUID)
                mo.setFieldValue(ChatChannelMessage.ref.channelID,channelID)
                mo.setFieldValue(ChatChannelMessage.ref.fromChatUUID,fromUUID)
                mo.setFieldValue(ChatChannelMessage.ref.toChatUUID,toUUID)
                mo.setFieldValue(ChatChannelMessage.ref.uuid,uuid)
                mo.setFieldValue(ChatChannelMessage.ref.message,msg)
                mo.setFieldValue(ChatChannelMessage.ref.createCorpID,0)
                mo.setFieldValue(ChatChannelMessage.ref.lastModifyCorpID,0)
                mo.setFieldValue(ChatChannelMessage.ref.createPartnerID,0)
                mo.setFieldValue(ChatChannelMessage.ref.lastModifyPartnerID,0)
                ChatChannelMessage.ref.rawCreate(mo)
            }
            catch (ex:Exception){
                ex.printStackTrace()
                this.logger.error(ex.toString())
            }

        }
    }
}