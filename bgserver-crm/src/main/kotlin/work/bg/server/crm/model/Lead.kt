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

package work.bg.server.crm.model

import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.annotation.Action
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.field.EventLogField
import work.bg.server.crm.field.ModelFullAddressField
import work.bg.server.util.TypeConvert
import java.lang.Exception

@Model(name="lead",title = "线索")
class Lead:ContextModel("crm_lead","public") {
    companion object : RefSingleton<Lead> {
        override lateinit var ref: Lead
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null,
            "name",
            FieldType.STRING,
            title = "姓名",
            defaultValue = "")
    val corpName = ModelField(null,
            "corp_name",
            FieldType.STRING,
            title = "公司名称",
            defaultValue = "")
    val isCorp = ModelField(null,
            "is_corp",
            FieldType.INT,
            title = "公司",
            defaultValue = -1)
    val mobile = ModelField(null,
            "mobile",
            FieldType.STRING,
            title = "手机",
            defaultValue = "")
    val telephone = ModelField(null,
            "telephone",
            FieldType.STRING,
            title = "电话",
            defaultValue = "")
    val fax = ModelField(null,
            "fax",
            FieldType.STRING,
            title = "传真",
            defaultValue = "")
    val qq = ModelField(null,
            "qq",
            FieldType.STRING,
            title = "QQ",
            defaultValue = "")
    val weiXin = ModelField(null,
            "wei_xin",
            FieldType.STRING,
            title = "微信",
            defaultValue = "")
    val province = ModelField(null,
            "province",
            FieldType.STRING,
            title = "省",
            defaultValue = "")
    val city = ModelField(null,
            "city",
            FieldType.STRING,
            title = "市",
            defaultValue = "")
    val district = ModelField(null,
            "district",
            FieldType.STRING,
            title = "区/县",
            defaultValue = "")
    val streetAddress = ModelField(null,
            "street_address",
            FieldType.STRING,
            "详细地址",
            defaultValue = "")

    val toCustomer = ModelOne2OneField(null,
            "to_customer_id",
            FieldType.BIGINT,
            "转化客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val partners = ModelMany2ManyField(null,
            "own_partner_id",
            FieldType.BIGINT, title = "占有人",
            relationModelTable = "public.crm_partner_lead_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    //最新交流人
    val commPartner = ModelMany2OneField(null,
            "comm_partner_id",
            FieldType.BIGINT,
            title = "联系人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val event = ModelMany2OneField(null,
            "event_id",
            FieldType.BIGINT,
            "活动",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val communications = ModelOne2ManyField(null,
            "communications",
            FieldType.BIGINT,
            "沟通记录",
            targetModelTable = "public.crm_lead_customer_communication_history",
            targetModelFieldName = "lead_id")

    val interactionStatus= ModelMany2OneField(null,
            "interaction_status_id",
            FieldType.BIGINT,
            "最近状态",
            targetModelTable = "public.crm_lead_interaction_status",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val fullAddress by lazy {
            ModelFullAddressField(null,
                    "fullAddress",
                    "地址",
                    this.province,this.city,
                    this.district,
                    this.streetAddress,
                    this.gson)
    }
    val eventLogs = EventLogField(null,"event_logs","跟踪日志")
    @Action("createCustomer")
    fun createCustomer(@RequestBody data:JsonObject?, partnerCache: PartnerCache):ActionResult{
        var ar = ActionResult()
        val modelID =data?.get("modelID")?.asLong
        if(modelID!=null){
            val modelData = this.rawRead(criteria = eq(this.id,modelID),partnerCache = partnerCache,useAccessControl = true)?.firstOrNull()
            modelData?.let {

                val def = DefaultTransactionDefinition()
                def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
                val status = txManager.getTransaction(def)
                try {
                    var mo = ModelDataObject(model = Customer.ref)
                    mo.setFieldValue(Customer.ref.name,modelData.getFieldValue(this.name))
                    mo.setFieldValue(Customer.ref.corpName,modelData.getFieldValue(this.corpName))
                    mo.setFieldValue(Customer.ref.isCorp,modelData.getFieldValue(this.isCorp))
                    mo.setFieldValue(Customer.ref.mobile,modelData.getFieldValue(this.mobile))
                    mo.setFieldValue(Customer.ref.telephone,modelData.getFieldValue(this.telephone))
                    mo.setFieldValue(Customer.ref.fax,modelData.getFieldValue(this.fax))
                    mo.setFieldValue(Customer.ref.qq,modelData.getFieldValue(this.qq))
                    mo.setFieldValue(Customer.ref.weiXin,modelData.getFieldValue(this.weiXin))
                    mo.setFieldValue(Customer.ref.province,modelData.getFieldValue(this.province))
                    mo.setFieldValue(Customer.ref.city,modelData.getFieldValue(this.city))
                    mo.setFieldValue(Customer.ref.district,modelData.getFieldValue(this.district))
                    mo.setFieldValue(Customer.ref.streetAddress,modelData.getFieldValue(this.streetAddress))
                    val eventData = modelData.getFieldValue(this.event)
                    val eventID =  ModelDataObject.getModelDataObjectID(eventData)
                    eventData?.let {
                        mo.setFieldValue(Customer.ref.event,eventID)
                    }
                    val commPartnerObj = modelData.getFieldValue(this.commPartner)
                    val comPartnerID =  ModelDataObject.getModelDataObjectID(commPartnerObj)
                    comPartnerID?.let {
                        mo.setFieldValue(Customer.ref.commPartner,comPartnerID)
                    }

                    val ret = Customer.ref.rawCreate(mo,partnerCache = partnerCache,useAccessControl = true)
                    if(ret.first!=null && ret.first!! > 0.toLong()){

                        val partnerRels = CrmPartnerLeadRel.ref.rawRead(criteria = eq(CrmPartnerLeadRel.ref.lead,modelID))?.toModelDataObjectArray()
                        partnerRels?.let {
                            it.forEach {
                                val relMo = ModelDataObject(model=CrmPartnerCustomerRel.ref)
                                relMo.setFieldValue(CrmPartnerCustomerRel.ref.customer,ret.first)
                                relMo.setFieldValue(CrmPartnerCustomerRel.ref.ownFlag,it.getFieldValue(CrmPartnerLeadRel.ref.ownFlag))
                                val partnerID = ModelDataObject.getModelDataObjectID(it.getFieldValue(CrmPartnerLeadRel.ref.partner))
                                relMo.setFieldValue(CrmPartnerCustomerRel.ref.partner,partnerID)
                                CrmPartnerCustomerRel.ref.rawCreate(relMo,partnerCache = partnerCache,useAccessControl = true)
                            }
                        }
                        //TODO add communications

                        var uModelData = ModelDataObject(model=this)
                        uModelData.setFieldValue(this.id,modelID)
                        uModelData.setFieldValue(this.toCustomer,ret.first)
                        this.rawEdit(uModelData,useAccessControl = true,partnerCache = partnerCache)
                    }
                    ar.errorCode = ErrorCode.SUCCESS
                    txManager.commit(status)
                    return ar
                }
                catch (ex:Exception){
                    ex.printStackTrace()
                    txManager.rollback(status)
                }
                finally {


                }

            }
        }

        ar.errorCode = ErrorCode.UNKNOW
        return ar
    }



}