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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestParam
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

    val chatUUID = ModelOne2OneField(null,
            "chat_partner_uuid",
            FieldType.BIGINT,
            "通讯UUID",
            isVirtualField = true,
            targetModelTable = "public.chat_partner_uuid",
            targetModelFieldName = "partner_id")

    val ownChannels = ModelOne2ManyField(null,
            "own_channels",
            FieldType.BIGINT,
            "我的频道",
            targetModelTable = "public.chat_channel",
            targetModelFieldName = "owner")

    val joinChannels = ModelMany2ManyField(null,"join_channels",
            FieldType.BIGINT,
            "加入的频道",
            relationModelTable = "public.chat_partner_join_channel_rel",
            relationModelFieldName = "join_partner_id",
            targetModelTable = "public.chat_channel",
            targetModelFieldName = "join_partners")


    @Action(name="login")
    override fun login(@RequestParam userName:String, @RequestParam password:String, @RequestParam devType:Int, session: HttpSession): ActionResult?{
        var md5Password= work.bg.server.util.MD5.hash(password)
        var partner=this.rawRead(criteria = and(eq(this.userName,userName)!!, eq(this.password,md5Password)!!),
                attachedFields = arrayOf(AttachedField(this.corps), AttachedField(this.partnerRoles)))
        return when{
            partner!=null->{
                var id=partner?.data?.firstOrNull()?.getValue(this.id) as Long?
                if(id!=null && id>0) {
                    val chatUUID = (partner?.firstOrNull()?.
                            getFieldValue(this.chatUUID) as ModelDataObject?)?.getFieldValue(ChatPartnerUUID.ref.uuid) as String?
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
                select(ChatPartnerJoinChannelRel.ref.joinChannel,
                fromModel= ChatPartnerJoinChannelRel.ref)
                .where(eq(ChatPartnerJoinChannelRel.ref.joinPartner,partnerID)))))?.toModelDataObjectArray()
        var ja = JsonArray()
        channels?.let {
            channels.forEach {
                var jo = JsonObject()
                jo.addProperty("id",it.getFieldValue(chModel.id)?.toString())
                jo.addProperty("name",it.getFieldValue(chModel.name)?.toString())
                jo.addProperty("tag",it.getFieldValue(chModel.tag)?.toString())
                jo.addProperty("defaultFlag",it.getFieldValue(chModel.defaultFlag)?.toString())
                jo.addProperty("mustJoinFlag",it.getFieldValue(chModel.mustJoinFlag)?.toString())
                jo.addProperty("broadcastType",it.getFieldValue(chModel.broadcastType)?.toString())
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

    override fun afterCreateObject(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var ret = super.afterCreateObject(modelDataObject, useAccessControl, pc)
        if(!ret.first){
            return ret
        }
        var cID = modelDataObject.getFieldValue(this.id) as Long?
        var puuid = ModelDataObject(model=ChatPartnerUUID.ref)
        puuid.setFieldValue(ChatPartnerUUID.ref.partner,cID)
        puuid.setFieldValue(ChatPartnerUUID.ref.uuid,UUID.randomUUID().toString())
        val pd = ChatPartnerUUID.ref.rawCreate(puuid,useAccessControl,pc)
        if(pd.first!=null && pd.first!!>0){
            return ret
        }
        return Pair(false,"创建企信UUID失败！")
    }

    override fun afterEditObject(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var ret= super.afterEditObject(modelDataObject, useAccessControl, pc)
        val pid = TypeConvert.getLong(modelDataObject.idFieldValue?.value as Number?)
        pid?.let {
            var chatUUIDObj = ChatPartnerUUID.ref.rawRead(criteria = eq(ChatPartnerUUID.ref.partner,pid))?.firstOrNull()
            if(chatUUIDObj==null){
                chatUUIDObj = ModelDataObject(model=ChatPartnerUUID.ref)
                chatUUIDObj.setFieldValue(ChatPartnerUUID.ref.partner,pid)
                chatUUIDObj.setFieldValue(ChatPartnerUUID.ref.uuid,UUID.randomUUID().toString())
                val pd = ChatPartnerUUID.ref.rawCreate(chatUUIDObj,useAccessControl,pc)
                if(pd.first!=null && pd.first!!>0){
                    return ret
                }
                return Pair(false,"创建企信UUID失败！")
            }
        }
        return ret
    }
}