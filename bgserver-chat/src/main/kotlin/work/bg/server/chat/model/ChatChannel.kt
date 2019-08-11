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
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("chatChannel")
class ChatChannel:ContextModel("public","chat_channel") {
    companion object: RefSingleton<ChatChannel> {
        override lateinit var ref: ChatChannel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null,
            "name",
            FieldType.STRING,
            "名称")

    val tag = ModelField(null,
            "tag",
            FieldType.STRING,
            "内部标识")

    val defaultFlag = ModelField(null,
            "default_flag",
            FieldType.INT,
            "默认频道",
            defaultValue = 0)

    val mustJoinFlag = ModelField(null,
            "must_join_flag",
            FieldType.INT,
            "必须加入",
            defaultValue = 1)

    val broadcastType = ModelField(null,
            "broadcast_type",
            FieldType.INT,
            "投递方式",
            defaultValue = 1)

    val owner = ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            defaultValue = CurrPartnerBillboard(),
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val joinPartners = ModelMany2ManyField(null,
            "join_partners",
            FieldType.BIGINT,
            "加入的员工",
            relationModelTable = "public.chat_partner_join_channel_rel",
            relationModelFieldName = "join_partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "join_channels")
}