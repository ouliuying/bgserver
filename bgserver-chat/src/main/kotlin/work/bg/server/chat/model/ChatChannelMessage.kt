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

import dynamic.model.query.exception.ModelErrorException
import dynamic.model.query.mq.*
import dynamic.model.query.mq.join.JoinModel
import dynamic.model.query.mq.model.ModelBase
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.acrule.ModelReadFieldFilterRule
import work.bg.server.core.acrule.ModelReadIsolationRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel

@Model("chatChannelMessage","通讯信息")
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

    val channelID = ModelField(null,
            "channel_id",
            FieldType.BIGINT,
            "频道ID")

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


    override fun beforeRead(vararg queryFields: FieldBase, criteria: ModelExpression?, model: ModelBase, useAccessControl: Boolean, partnerCache: PartnerCache?, joinModels: Array<JoinModel>?): Pair<ModelExpression?, Array<FieldBase>> {
        var ruleCriteria=criteria
        var newQueryFields = arrayListOf<FieldBase>()
        if (useAccessControl && partnerCache!=null){
            var models = arrayListOf<ModelBase>(model)
            joinModels?.let {
                it.forEach { sit->
                    sit.model?.let {
                        models.add(it)
                    }
                }
            }
            partnerCache?.let {
                ruleCriteria = or(`in`(this.channelID, select(ChatChannel.ref.id,fromModel = ChatChannel.ref).where(eq(ChatChannel.ref.owner,it.partnerID))),
                        `in`(this.channelID, select(ChatModelJoinChannelRel.ref.joinChannel,fromModel =ChatModelJoinChannelRel.ref).where(eq(ChatModelJoinChannelRel.ref.joinPartner,it.partnerID))))
            }
        }
        else if(useAccessControl){
            throw ModelErrorException("权限错误")
        }
        else{
            newQueryFields.addAll(queryFields)
        }
        return Pair(ruleCriteria,newQueryFields.toTypedArray())
    }
}