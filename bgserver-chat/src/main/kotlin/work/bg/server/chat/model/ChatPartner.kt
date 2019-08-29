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
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestParam
import work.bg.server.chat.billboard.ChatGuidBillboard
import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheKey
import work.bg.server.core.constant.SessionTag
import work.bg.server.core.model.BaseCorp
import work.bg.server.core.model.BaseCorpPartnerRel
import work.bg.server.core.model.BasePartnerAppShortcut
import work.bg.server.core.model.BasePartnerRole
import work.bg.server.core.mq.*
import work.bg.server.core.mq.specialized.ConstRelRegistriesField
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.core.spring.boot.model.AppModel
import work.bg.server.errorcode.ErrorCode
import work.bg.server.sms.model.SmsPartner
import work.bg.server.util.TypeConvert
import java.lang.Exception
import java.net.URI
import java.util.*
import javax.servlet.http.HttpSession

@Model("partner")
class ChatPartner: SmsPartner() {

    @Value("\${bg.chat.redis.url}")
    private lateinit var redisUrl:String

    companion object: RefSingleton<ChatPartner> {
        override lateinit var ref: ChatPartner
    }

    val chatUUID = ModelField(null,
            "chat_uuid",
            FieldType.STRING,
            "通讯UUID",
            defaultValue = ChatGuidBillboard())

    val ownChannels = ModelOne2ManyField(null,
            "own_channels",
            FieldType.BIGINT,
            "我的频道",
            targetModelTable = "public.chat_channel",
            targetModelFieldName = "owner")

    val joinChannels = ModelMany2ManyField(null,"join_channels",
            FieldType.BIGINT,
            "加入的频道",
            relationModelTable = "public.chat_model_join_channel_rel",
            relationModelFieldName = "join_channel_id",
            targetModelTable = "public.chat_channel",
            targetModelFieldName = "id")


    @Action(name="login")
    override fun login(@RequestParam userName:String, @RequestParam password:String, @RequestParam devType:Int, session: HttpSession): ActionResult?{
        var md5Password= work.bg.server.util.MD5.hash(password)
        var partner=this.rawRead(criteria = and(eq(this.userName,userName)!!, eq(this.password,md5Password)!!),
                attachedFields = arrayOf(AttachedField(this.corps), AttachedField(this.partnerRoles)))
        return when{
            partner!=null->{
                var id=partner?.data?.firstOrNull()?.getValue(this.id) as Long?
                if(id!=null && id>0) {
                    val chatUUID = partner?.firstOrNull()?.
                            getFieldValue(this.chatUUID) as String?
                    var ar = ActionResult()
                    var corpPartnerRels = (partner?.data?.
                            firstOrNull()?.
                            getValue(ConstRelRegistriesField.ref!!) as ModelDataSharedObject?)?.data?.get(BaseCorpPartnerRel.ref)
                            as ModelDataArray?
                    corpPartnerRels?.data?.sortByDescending {
                        (it.getValue(BaseCorpPartnerRel.ref!!.corp) as ModelDataObject?)?.data?.getValue(BasePartnerRole.ref!!.isSuper) as Int
                    }
                    var corpObject=corpPartnerRels?.data?.firstOrNull()?.getValue(BaseCorpPartnerRel.ref!!.corp) as ModelDataObject?
                    var partnerRole=corpPartnerRels?.data?.firstOrNull()?.getValue(BaseCorpPartnerRel.ref!!.partnerRole) as ModelDataObject?
                    var corpID=corpObject?.data?.getValue(BaseCorp.ref!!.id) as Long?
                    var partnerRoleID = partnerRole?.idFieldValue?.value as Long?
                    var corps=corpPartnerRels?.data?.map {
                        it.getValue(BaseCorpPartnerRel.ref!!.corp) as ModelDataObject
                    }
                    if(corpID!=null && corpID>0){
                        session.setAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY, PartnerCacheKey(id, corpID, devType))
                        var pc = this.partnerCacheRegistry?.get(PartnerCacheKey(id, corpID, devType))
                        if (pc != null) {
                            var ar = ActionResult()
                            var partner = mutableMapOf<String, Any?>()
                            ar.bag["partner"] = partner
                            partner["status"] = 1
                            partner["partnerID"] = id
                            partner["corpID"]=corpID
                            partner["chatUUID"] =chatUUID
                            var channelMeta = getChannelMeta(id,corpID)
                            var chatSessionID = this.queryChatSessionID(id,corpID,devType,channelMeta?:"",chatUUID?:"")
                            if(chatSessionID.isNullOrEmpty()){
                                return ActionResult(ErrorCode.UNKNOW,"获取企信授权失败")
                            }
                            partner["chatSessionID"] = chatSessionID
                            var sys= mutableMapOf<String,Any?>()
                            ar.bag["sys"]=sys
                            sys["corps"] = corps?.toList()?.stream()?.map {
                                mapOf(
                                        "id" to it.data.getValue(BaseCorp.ref!!.id),
                                        "name" to it.data.getValue(BaseCorp.ref!!.name),
                                        "comment" to it.data.getValue(BaseCorp.ref!!.comment)
                                )
                            }?.toArray()
                            sys["currCorp"] = pc.activeCorp
                            sys["installApps"]= AppModel.ref.appPackageManifests
                            sys["roleApps"]=if(partnerRoleID!=null) BasePartnerRole.ref.getInstallApps(partnerRoleID) else emptyList()
                            sys["shortcutApps"]= BasePartnerAppShortcut.ref.getPartnerApps(id)
                            return ar
                        }
                    }
                }
                return ActionResult(ErrorCode.RELOGIN)
            }
            else -> ActionResult(ErrorCode.RELOGIN)
        }
    }

    private  fun getChannelMeta(partnerID: Long,corpID: Long):String?{
        val chModel = ChatChannel.ref
        var channels = ChatChannel.ref.rawRead(criteria = or(eq(
                chModel.defaultFlag,1
        ),eq(chModel.owner,partnerID),`in`(chModel.id,
                select(ChatModelJoinChannelRel.ref.joinChannel,
                fromModel= ChatModelJoinChannelRel.ref)
                .where(eq(ChatModelJoinChannelRel.ref.joinPartner,partnerID)))))?.toModelDataObjectArray()
        var ja = JsonArray()
        channels?.let {
            channels.forEach {
                var jo = JsonObject()
                jo.addProperty("id",it.getFieldValue(chModel.id)?.toString())
                jo.addProperty("name",it.getFieldValue(chModel.name)?.toString())
                jo.addProperty("uuid",it.getFieldValue(chModel.uuid)?.toString())
                jo.addProperty("defaultFlag",it.getFieldValue(chModel.defaultFlag)?.toString())
                jo.addProperty("broadcastType",it.getFieldValue(chModel.broadcastType)?.toString())
                jo.addProperty("icon",it.getFieldValue(chModel.icon)?.toString())
                ja.add(jo)
            }
        }
        return ja.toString()
    }
    private fun queryChatSessionID(partnerID:Long,
                                   corpID:Long,
                                   devType:Int,
                                   channelMeta:String,
                                   chatUUID:String):String?{
        try {
            var pool= redis.clients.jedis.JedisPool(URI(this.redisUrl))
            val chatSessionID = UUID.randomUUID().toString()
            var ret = pool.resource.hmset(chatSessionID, mapOf(
                    "modelID" to partnerID.toString(),
                    "corpID" to corpID.toString(),
                    "chatUUID" to chatUUID,
                    "model" to "partner",
                    "devType" to devType.toString(),
                    "channelMeta" to channelMeta
            ))
            return if(ret.compareTo("ok",true)==0){
                pool.resource.expire(chatSessionID,1800)
                return chatSessionID
            } else null
        }
        catch (ex:Exception){
            ex.printStackTrace()
        }
        return null
    }

//    override fun afterCreateObject(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
//       return super.afterCreateObject(modelDataObject, useAccessControl, pc)
//    }

    override fun afterEditObject(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var ret= super.afterEditObject(modelDataObject, useAccessControl, pc)
        val pid = TypeConvert.getLong(modelDataObject.idFieldValue?.value as Number?)
        pid?.let {
            var selfObj = this.rawRead(criteria = eq(this.id,pid))?.firstOrNull()
            if(selfObj!=null && selfObj.getFieldValue(this.chatUUID)==null){
                selfObj = ModelDataObject(model=this)
                selfObj.setFieldValue(this.chatUUID,UUID.randomUUID().toString())
                val pd = this.update(selfObj,criteria = eq(this.id,pid))
                if(pd!=null && pd!!>0){
                    return ret
                }
                return Pair(false,"创建企信UUID失败！")
            }
        }
        return ret
    }
}