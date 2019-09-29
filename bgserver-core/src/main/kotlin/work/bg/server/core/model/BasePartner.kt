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

package work.bg.server.core.model

import dynamic.model.query.mq.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import javax.servlet.http.HttpSession
import work.bg.server.core.constant.SessionTag

import dynamic.model.web.errorcode.ErrorCode
import org.springframework.web.bind.annotation.RequestParam
import dynamic.model.query.mq.model.AppModel
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import dynamic.model.web.spring.boot.model.ActionResult
import work.bg.server.core.acrule.ModelEditRecordFieldsValueFilterRule
import work.bg.server.core.acrule.ModelReadFieldFilterRule
import work.bg.server.core.acrule.bean.ModelEditPartnerInnerRecordFieldsValueFilterBean
import work.bg.server.core.acrule.bean.ModelReadPartnerInnerFilterBean
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheKey
import work.bg.server.core.model.billboard.PartnerTagBillboard



@Model(name = "partner",title="员工")
open class  BasePartner(table:String, schema:String): ContextModel(table,schema){
    @Value("\${bg.work.auth-url}")
    private  val authUrl:String?= null
    companion object : RefSingleton<BasePartner> {
        override lateinit var ref: BasePartner
    }
    @Autowired
    lateinit var readPartnerInnerFilterBean:ModelReadPartnerInnerFilterBean
    @Autowired
    lateinit var editPartnerInnerFilterBean:ModelEditPartnerInnerRecordFieldsValueFilterBean
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val userName= dynamic.model.query.mq.ModelField(null,
            "user_name",
            dynamic.model.query.mq.FieldType.STRING,
            "用户名",
            index = arrayListOf(dynamic.model.query.mq.FieldIndex(unique = true)))
    val password= dynamic.model.query.mq.ModelField(null,
            "password",
            dynamic.model.query.mq.FieldType.STRING,
            "密码")
    val name= dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            "姓名")
    val birthday= dynamic.model.query.mq.ModelField(null,
            "birthday",
            dynamic.model.query.mq.FieldType.DATE,
            "生日")
    val mobile= dynamic.model.query.mq.ModelField(null,
            "mobile",
            dynamic.model.query.mq.FieldType.STRING,
            "手机")
    val nickName= dynamic.model.query.mq.ModelField(null,
            "nick_name",
            dynamic.model.query.mq.FieldType.STRING,
            "昵称")
    val userTitle= dynamic.model.query.mq.ModelField(null,
            "user_title",
            dynamic.model.query.mq.FieldType.STRING,
            "用户标题")
    val userIcon= dynamic.model.query.mq.ModelField(null,
            "user_icon",
            dynamic.model.query.mq.FieldType.STRING,
            "图标")
    val userComment= dynamic.model.query.mq.ModelField(null,
            "user_comment",
            dynamic.model.query.mq.FieldType.TEXT,
            "用户注释")

    val tag= dynamic.model.query.mq.ModelField(null,
            "tag",
            dynamic.model.query.mq.FieldType.BIGINT,
            "用户标识",
            index = arrayListOf(dynamic.model.query.mq.FieldIndex(unique = true)),
            defaultValue = PartnerTagBillboard())

    val telephone= dynamic.model.query.mq.ModelField(null,
            "telephone",
            dynamic.model.query.mq.FieldType.STRING,
            "电话")
    val email= dynamic.model.query.mq.ModelField(null,
            "email",
            dynamic.model.query.mq.FieldType.STRING,
            "E-Mail")
    val syncTag= dynamic.model.query.mq.ModelField(null,
            "sync_tag",
            dynamic.model.query.mq.FieldType.STRING,
            "信息同步Tag")
    val accessTokenKey= dynamic.model.query.mq.ModelField(null,
            "access_token_key",
            dynamic.model.query.mq.FieldType.STRING,
            "Token")

    val corps= dynamic.model.query.mq.ModelMany2ManyField(null,
            "corp_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "公司",
            "public.base_corp_partner_rel",
            "corp_id",
            "public.base_corp",
            "id")

    val partnerRoles= dynamic.model.query.mq.ModelMany2ManyField(null,
            "partner_role_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "角色",
            "public.base_corp_partner_rel",
            "partner_role_id",
            "public.base_partner_role",
            "id")
    //function fields
    val setCurrentCorpAsDefault by lazy {
        dynamic.model.query.mq.ProxyRelationModelField<Int, PartnerCache>(null,
                BaseCorpPartnerRel.ref.isDefaultCorp,
                "set_current_corp_as_default",
                BaseCorpPartnerRel.ref.isDefaultCorp.fieldType,
                "当前公司为默认")
    }

    val modelLogs = dynamic.model.query.mq.ModelOne2ManyField(null,
            "model_logs",
            dynamic.model.query.mq.FieldType.BIGINT,
            "日志",
            targetModelTable = "public.base_model_log",
            targetModelFieldName = "partner_id")

    val storageEntities = ModelMany2ManyField(null,
            "storageEntities",
            FieldType.BIGINT,
            "文件资源",
            relationModelTable = "public.base_partner_storage_entity_rel",
            relationModelFieldName = "storage_entity_id",
            targetModelTable = "public.base_storage_entity",
            targetModelFieldName = "id")


    constructor():this("base_partner","public")
    init {

    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.userName,this.password,advice = "用户名或密码必须输入的信息！"),
                ModelFieldNotNullOrEmpty(this.userName,this.password,advice = "用户名或密码不能为空！"),
                ModelFieldRequired(this.email,advice = "用户邮箱不能我为空！"),
                ModelFieldRequired(this.mobile,advice = "用户手机不能我为空！")
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.userName,advice = "用户名或密码必须输入的信息！"),
                ModelFieldRequired(this.email,advice = "用户邮箱不能我为空！"),
                ModelFieldRequired(this.mobile,advice = "用户手机不能我为空！")
        )
    }

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.userName,advice = "用户名必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL),
                ModelFieldUnique(this.mobile,advice = "用户手机号必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL),
                ModelFieldUnique(this.email,advice = "用户邮箱必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL)
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.userName,advice = "用户名必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL),
                ModelFieldUnique(this.mobile,advice = "用户手机号必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL),
                ModelFieldUnique(this.email,advice = "用户邮箱必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL)
        )
    }

    override fun getModelReadAccessFieldFilterRule(): ModelReadFieldFilterRule? {
        return this.readPartnerInnerFilterBean
    }

    override fun getModelEditAccessFieldFilterRule(): ModelEditRecordFieldsValueFilterRule<*>? {
        return this.editPartnerInnerFilterBean
    }
    @Action(name="login")
    open fun login(@RequestParam userName:String, @RequestParam password:String, @RequestParam devType:Int, session:HttpSession):ActionResult?{
        var md5Password= work.bg.server.util.MD5.hash(password)
        var partner=this.rawRead(criteria = and(eq(this.userName,userName)!!,eq(this.password,md5Password)!!),
                attachedFields = arrayOf(dynamic.model.query.mq.AttachedField(this.corps), dynamic.model.query.mq.AttachedField(this.partnerRoles)))
        return when{
            partner!=null->{
                    var id=partner?.data?.firstOrNull()?.getValue(this.id) as Long?
                    if(id!=null && id>0) {
                        var ar = ActionResult()
                        var corpPartnerRels = (partner?.data?.
                                firstOrNull()?.
                                getValue(ConstRelRegistriesField.ref!!) as dynamic.model.query.mq.ModelDataSharedObject?)?.data?.get(BaseCorpPartnerRel.ref)
                        as dynamic.model.query.mq.ModelDataArray?
                        corpPartnerRels?.data?.sortByDescending {
                            (it.getValue(BaseCorpPartnerRel.ref!!.corp) as dynamic.model.query.mq.ModelDataObject?)?.data?.getValue(BasePartnerRole.ref!!.isSuper) as Int
                        }
                        var corpObject=corpPartnerRels?.data?.firstOrNull()?.getValue(BaseCorpPartnerRel.ref!!.corp) as dynamic.model.query.mq.ModelDataObject?
                        var partnerRole=corpPartnerRels?.data?.firstOrNull()?.getValue(BaseCorpPartnerRel.ref!!.partnerRole) as dynamic.model.query.mq.ModelDataObject?
                        var corpID=corpObject?.data?.getValue(BaseCorp.ref!!.id) as Long?
                        var partnerRoleID = partnerRole?.idFieldValue?.value as Long?
                        var corps=corpPartnerRels?.data?.map {
                            it.getValue(BaseCorpPartnerRel.ref!!.corp) as dynamic.model.query.mq.ModelDataObject
                        }
                        if(corpID!=null && corpID>0){
                            session.setAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY, PartnerCacheKey(id, corpID, devType))
                            var pc = this.partnerCacheRegistry?.get(PartnerCacheKey(id, corpID, devType))
                            if (pc != null) {
                                var ar = ActionResult()
                                var partner = mutableMapOf<String, Any?>()
                                ar.bag["partner"] = partner
                                partner["status"] = 1
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
                                sys["shortcutApps"]=BasePartnerAppShortcut.ref.getPartnerApps(id)
                                return ar
                            }
                        }
                    }
                    return ActionResult(ErrorCode.RELOGIN)
                }
                else -> ActionResult(ErrorCode.RELOGIN)
            }
        }
    }



