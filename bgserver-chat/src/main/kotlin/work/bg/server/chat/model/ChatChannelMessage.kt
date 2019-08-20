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

package work.bg.server.chat.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelMany2OneField
import work.bg.server.core.spring.boot.annotation.Model

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

    val message = ModelField(null,
            "message",
            FieldType.TEXT,
            "信息")
}