/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  * GNU Lesser General Public License Usage
 *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  * General Public License version 3 as published by the Free Software
 *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  * project of this file. Please review the following information to
 *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *
 *
 */

package work.bg.server.core.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.AppModel
import javax.servlet.http.HttpSession
import work.bg.server.core.constant.SessionTag

import work.bg.server.errorcode.ErrorCode
import org.springframework.web.bind.annotation.RequestParam
import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.ModelEditRecordFieldsValueFilterRule
import work.bg.server.core.acrule.ModelReadFieldFilterRule
import work.bg.server.core.acrule.bean.ModelEditPartnerInnerRecordFieldsValueFilterBean
import work.bg.server.core.acrule.bean.ModelReadPartnerInnerFilterBean
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCacheKey
import work.bg.server.core.model.billboard.PartnerTagBillboard
import work.bg.server.core.mq.*
import work.bg.server.core.mq.specialized.ConstRelRegistriesField
import work.bg.server.core.spring.boot.model.ActionResult


@Model(name = "partner",title="员工")
class  BasePartner(table:String,schema:String): ContextModel(table,schema){
    @Value("\${bg.work.auth-url}")
    private  val authUrl:String?= null
    companion object :RefSingleton<BasePartner>{
        override lateinit var ref: BasePartner
    }
    @Autowired
    lateinit var readPartnerInnerFilterBean:ModelReadPartnerInnerFilterBean
    @Autowired
    lateinit var editPartnerInnerFilterBean:ModelEditPartnerInnerRecordFieldsValueFilterBean
    val id=ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())
    val userName=ModelField(null,
            "user_name",
            FieldType.STRING,
            "用户名",
            index = arrayListOf(FieldIndex(unique = true)))
    val password=ModelField(null,
            "password",
            FieldType.STRING,
            "密码")
    val name=ModelField(null,
            "name",
            FieldType.STRING,
            "姓名")
    val birthday=ModelField(null,
            "birthday",
            FieldType.DATE,
            "生日")
    val mobile=ModelField(null,
            "mobile",
            FieldType.STRING,
            "手机")
    val nickName=ModelField(null,
            "nick_name",
            FieldType.STRING,
            "昵称")
    val userTitle=ModelField(null,
            "user_title",
            FieldType.STRING,
            "用户标题")
    val userIcon=ModelField(null,
            "user_icon",
            FieldType.STRING,
            "图标")
    val userComment=ModelField(null,
            "user_comment",
            FieldType.TEXT,
            "用户注释")

    val tag=ModelField(null,
            "tag",
            FieldType.BIGINT,
            "用户标识",
            index = arrayListOf(FieldIndex(unique = true)),
            defaultValue = PartnerTagBillboard())

    val telephone=ModelField(null,
            "telephone",
            FieldType.STRING,
            "电话")
    val email=ModelField(null,
            "email",
            FieldType.STRING,
            "E-Mail")
    val syncTag=ModelField(null,
            "sync_tag",
            FieldType.STRING,
            "信息同步Tag")
    val accessTokenKey=ModelField(null,
            "access_token_key",
            FieldType.STRING,
            "Token")

    val corps=ModelMany2ManyField(null,
            "corp_id",
            FieldType.BIGINT,
            "公司",
            "public.base_corp_partner_rel",
            "corp_id",
    "public.base_corp",
            "id")

    val partnerRoles=ModelMany2ManyField(null,
            "partner_role_id",
            FieldType.BIGINT,
            "角色",
            "public.base_corp_partner_rel",
            "partner_role_id",
    "public.base_partner_role",
            "id")
    //function fields
    val setCurrentCorpAsDefault by lazy {ProxyRelationModelField<Int>(null,
            BaseCorpPartnerRel.ref.isDefaultCorp,
            "set_current_corp_as_default",
            BaseCorpPartnerRel.ref.isDefaultCorp.fieldType,
            "设定当前公司为默认")}


    constructor():this("base_partner","public")
    init {

    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.userName,this.password,advice = "用户名或密码必须输入的信息！"),
                ModelFieldRequired(this.email,advice = "用户邮箱必须输入的信息！"),
                ModelFieldRequired(this.mobile,advice = "用户手机必须输入的信息！"),
                ModelFieldNotNullOrEmpty(this.userName,this.password,advice = "用户名或密码不能为空！"),
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

    override fun getModelReadAccessFieldFilterRule(): ModelReadFieldFilterRule? {
        return this.readPartnerInnerFilterBean
    }

    override fun getModelEditAccessFieldFilterRule(): ModelEditRecordFieldsValueFilterRule<*>? {
        return this.editPartnerInnerFilterBean
    }
    @Action(name="login")
    fun login(@RequestParam userName:String,@RequestParam password:String,@RequestParam devType:Int,session:HttpSession):ActionResult?{
        var md5Password=util.MD5.hash(password)
        var partner=this.rawRead(criteria = and(eq(this.userName,userName)!!,eq(this.password,md5Password)!!),
                attachedFields = arrayOf(AttachedField(this.corps),AttachedField(this.partnerRoles)))
        return when{
            partner!=null->{
                    var id=partner?.data?.firstOrNull()?.getValue(this.id) as Long?
                    if(id!=null && id>0) {
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



