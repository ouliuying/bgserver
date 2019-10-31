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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.chat.billboard.ChatChannelGuidBillboard
import work.bg.server.chat.field.PartnerJoinStatusField
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.and
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.model.ActionResult
import work.bg.server.util.TypeConvert

@Model("chatChannel","频道")
class ChatChannel:ContextModel("chat_channel","public") {
    companion object: RefSingleton<ChatChannel> {
        override lateinit var ref: ChatChannel
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val name = dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            "名称")

    val icon = dynamic.model.query.mq.ModelField(null,
            "icon",
            dynamic.model.query.mq.FieldType.STRING,
            "图标")

    val uuid = dynamic.model.query.mq.ModelField(null,
            "uuid",
            dynamic.model.query.mq.FieldType.STRING,
            "内部标识",
            defaultValue = ChatChannelGuidBillboard())

    val defaultFlag = dynamic.model.query.mq.ModelField(null,
            "default_flag",
            dynamic.model.query.mq.FieldType.INT,
            "默认频道",
            defaultValue = 0)

    val broadcastType = dynamic.model.query.mq.ModelField(null,
            "broadcast_type",
            dynamic.model.query.mq.FieldType.INT,
            "投递方式",
            defaultValue = 0,comment = "1 表示广播，0 p2p")

    val owner = dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            defaultValue = CurrPartnerBillboard(),
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val joinPartners = dynamic.model.query.mq.ModelMany2ManyField(null,
            "join_partners",
            dynamic.model.query.mq.FieldType.BIGINT,
            "加入的员工",
            relationModelTable = "public.chat_model_join_channel_rel",
            relationModelFieldName = "join_partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")

    val joinStatus =PartnerJoinStatusField(null,
            "join_status",
            "关系",
            this.owner)

    @Action("getChannelJoinModels")
    fun getChannelJoinModels(@RequestBody data:JsonObject,partnerCache:PartnerCache?):ActionResult?{
          val ar = ActionResult(ErrorCode.RELOGIN)
          if(partnerCache!=null){
              val uuid = data.get("uuid")?.asString
              val mo= this.acRead(criteria = eq(this.uuid,uuid),
                      partnerCache = partnerCache,
                      attachedFields = arrayOf(dynamic.model.query.mq.AttachedField(this.joinPartners)))?.firstOrNull()
              ar.errorCode = ErrorCode.SUCCESS
              val joinModels = getJoinPartners(mo)
              joinModels?.let {
                  ar.bag["joinModels"]= joinModels
              }
          }
          return ar
    }
    @Action("doJoinChannel")
    fun doJoinChannel(@RequestBody data:JsonObject,partnerCache: PartnerCache?):ActionResult{
        var ar = ActionResult()
        var modelID = data.get("modelID")?.asLong
        modelID?.let {
            if(partnerCache!=null && modelID>0){
                if(ChatModelJoinChannelRel.ref.rawCount(criteria = and(eq(ChatModelJoinChannelRel.ref.joinChannel,modelID),
                                eq(ChatModelJoinChannelRel.ref.joinPartner,partnerCache.partnerID)))>0){
                    ar.errorCode = ErrorCode.UNKNOW
                    ar.description= "已经加入！"
                    return ar
                }

                if(this.rawCount(criteria = and(eq(this.owner,partnerCache.partnerID),eq(this.id,modelID)))>0){
                    ar.errorCode = ErrorCode.UNKNOW
                    ar.description= "已经加入！"
                    return ar
                }

                val mo = dynamic.model.query.mq.ModelDataObject(model = ChatModelJoinChannelRel.ref)
                mo.setFieldValue(ChatModelJoinChannelRel.ref.joinChannel,modelID)
                mo.setFieldValue(ChatModelJoinChannelRel.ref.joinPartner,partnerCache.partnerID)
                var ret = ChatModelJoinChannelRel.ref.acCreate(mo,partnerCache)
                if(ret.first!=null && ret.first!!>0){
                    ar.errorCode = ErrorCode.SUCCESS
                }
                else{
                    ar.errorCode=ErrorCode.UNKNOW
                    ar.description = ret.second
                }
                return ar
            }
        }
        ar.errorCode = ErrorCode.RELOGIN
        return ar
    }
    private fun getJoinPartners(mo: dynamic.model.query.mq.ModelDataObject?):JsonArray?{
        var joinModels = JsonArray()
        var ownerPartner = this.getJoinModelFromPartner(mo?.getFieldValue(this.owner) as dynamic.model.query.mq.ModelDataObject?)
        ownerPartner?.let {
            ownerPartner.addProperty("isOwner","1")
            joinModels.add(ownerPartner)
        }
        ((mo?.getFieldValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject?)?.data?.get(ChatModelJoinChannelRel.ref) as dynamic.model.query.mq.ModelDataArray?)?.let {
            it.toModelDataObjectArray().forEach {
                var partnerModelDataObject = it.getFieldValue(ChatModelJoinChannelRel.ref.joinPartner) as dynamic.model.query.mq.ModelDataObject?
                partnerModelDataObject?.let {
                    val joinPartner = this.getJoinModelFromPartner(partnerModelDataObject)
                    joinPartner?.let {
                        joinModels.add(joinPartner)
                    }
                }
            }
        }
        //continue add other models have chat function
        return joinModels
    }
    private fun getJoinModelFromPartner(partner: dynamic.model.query.mq.ModelDataObject?):JsonObject?{
        return partner?.let {
            val jo = JsonObject()
            jo.addProperty("model","partner")
            jo.addProperty("id",TypeConvert.getLong(partner.getFieldValue(ChatPartner.ref.id) as Number?))
            jo.addProperty("name",partner.getFieldValue(ChatPartner.ref.name) as String?)
            jo.addProperty("userName",partner.getFieldValue(ChatPartner.ref.userName) as String?)
            jo.addProperty("icon",partner.getFieldValue(ChatPartner.ref.userIcon) as String?)
            jo.addProperty("title",partner.getFieldValue(ChatPartner.ref.userTitle) as String?)
            jo.addProperty("uuid",partner.getFieldValue(ChatPartner.ref.chatUUID) as String?)
            jo.addProperty("nickName",partner.getFieldValue(ChatPartner.ref.nickName) as String?)
            jo
        }
    }
}