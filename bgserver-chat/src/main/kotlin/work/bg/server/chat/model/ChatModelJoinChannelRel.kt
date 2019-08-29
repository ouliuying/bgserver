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

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("chatModelJoinChannelRel")
class ChatModelJoinChannelRel:ContextModel("chat_model_join_channel_rel","public") {
    companion object: RefSingleton<ChatModelJoinChannelRel> {
        override lateinit var ref: ChatModelJoinChannelRel
    }


    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())


    val joinPartner= ModelMany2OneField(null,
            "join_partner_id",
            FieldType.BIGINT,
            "加入的员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))


    val joinChannel= ModelMany2OneField(null,
            "join_channel_id",
            FieldType.BIGINT,
            "加入的频道",
            targetModelTable = "public.chat_channel",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))


}