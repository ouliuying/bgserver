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
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("chatPartnerUUID")
class ChatPartnerUUID:ContextModel("chat_partner_uuid","public") {
    companion object: RefSingleton<ChatPartnerUUID> {
        override lateinit var ref: ChatPartnerUUID
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val partner = ModelOne2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val uuid = ModelField(null,
            "chat_uuid",
            FieldType.STRING,
            "通讯UUID")
}