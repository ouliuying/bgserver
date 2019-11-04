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

package work.bg.server.chat.model

import dynamic.model.query.mq.FieldPrimaryKey
import dynamic.model.query.mq.FieldType
import dynamic.model.query.mq.ModelField
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model("chatChannelMessage")
class ChatChannelMessage:ContextModel("chat_channel_message","public") {
    companion object: RefSingleton<ChatChannelMessage> {
        override lateinit var ref: ChatChannelMessage
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val channelUUID = ModelField(null,
            "channel_uuid",
            FieldType.STRING,
            "频道UUID")

    val fromChatUUID = ModelField(null,
            "from_chat_uuid",
            FieldType.STRING,
            "发起ChatUUID")

    val toChatUUID = ModelField(null,
            "to_chat_uuid",
            FieldType.STRING,
            "接收ChatUUID")

    val uuid = ModelField(null,
            "uuid",
            FieldType.STRING,
            "信息UUID")

    val message = ModelField(null,
            "message",
            FieldType.TEXT,
            "信息")
}