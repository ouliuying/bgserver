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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.beans.factory.annotation.Autowired
import work.bg.server.core.model.field.EventLogField
import work.bg.server.mail.MailSender
import work.bg.server.sms.SmsSender
import work.bg.server.util.TypeConvert
import java.math.BigInteger

@Model("customerOrder","客户订单")
class CustomerOrder:
        ContextModel("crm_customer_order","public") {
    @Autowired
    private lateinit var smsSender: SmsSender
    @Autowired
    private lateinit var mailSender: MailSender
    companion object : RefSingleton<CustomerOrder> {
        override lateinit var ref: CustomerOrder
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val title= ModelField(null,
            "title",
            FieldType.STRING,
            "标题")

    val price = ModelField(null,
            "price",
            FieldType.NUMBER,
            "金额")

    val customer= ModelMany2OneField(null,
            "customer_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val products = ModelMany2ManyField(null,
            "products",
            FieldType.BIGINT,
            "产品",
            relationModelTable = "public.crm_customer_opportunity_order_product_rel",
            relationModelFieldName = "product_id",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id")

    val quotation = ModelOne2OneField(null,
            "quotation",
            FieldType.BIGINT,
            "报价单",
            isVirtualField = true,
            targetModelTable = "public.crm_customer_opportunity_order_quotation",
            targetModelFieldName = "order_id")

    val opportunity = ModelOne2OneField(null,
            "opportunity_id",
            FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id"
    )

    val step = ModelField(null,
            "step",
            FieldType.INT,
            "状态",
            defaultValue = 0)

    val invoice = ModelOne2OneField(null,
            "invoice",
            fieldType = FieldType.BIGINT,
            title = "票据",
            isVirtualField = true,
            targetModelTable = "public.crm_customer_order_invoice",
            targetModelFieldName = "order_id")


    val sms = FunctionField<String,PartnerCache>(null, "sms", FieldType.TEXT, "短信")
    val smsMobiles = FunctionField<String,PartnerCache>(null, "sms_mobiles", FieldType.TEXT, "号码")
    val mail = FunctionField<String,PartnerCache>(null, "mail", FieldType.TEXT, "邮件内容")
    val mailAddresses = FunctionField<String,PartnerCache>(null, "mail_addresses", FieldType.TEXT, "邮件地址")

    val eventLogs = EventLogField(null,"event_logs","跟踪日志")



    @Action("confirmCustomerOrder")
    fun confirmCustomerOrder(@RequestBody modelData: ModelDataObject?, pc:PartnerCache): ActionResult?{
        var r = ActionResult(ErrorCode.UNKNOW)
        var d = this.rawRead(criteria = eq(this.id,modelData?.idFieldValue?.value),partnerCache = pc,useAccessControl = true)?.firstOrNull()
       d?.getFieldValue(this.step)?.let {
           if(it!=CustomerOrderStep.NEW_STEP.step){
                r.description="只能确认信订单"
           }
           else{
               var mo= ModelDataObject(model = this)
               mo.setFieldValue(this.id,modelData?.idFieldValue?.value)
               mo.setFieldValue(this.step,CustomerOrderStep.CONFIRM_STEP.step)
               this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = true)
               r.errorCode=ErrorCode.SUCCESS
           }
       }
        return r
    }

    @Action("notifyOrderStepBySmsEmail")
    fun notifyOrderStepBySmsEmail(@RequestBody data: JsonObject?,
                                  pc:PartnerCache):ActionResult?{
        var r =ActionResult()
        val smsMessage = data?.get("sms")?.asString
        val smsMobiles = data?.get("smsMobiles")?.asJsonArray
        val mail = data?.get("mail")?.asString
        val mailAddresses = data?.get("mailAddresses")?.asJsonArray


        smsMessage?.let {
            val mobiles = arrayListOf<String>()
            smsMobiles?.forEach {
                mobiles.add(it.toString().substringBefore("(").trim())
            }
            this.smsSender.send(mobiles = mobiles,message = smsMessage,repeatFilter = false,useAccessControl = true,partnerCache = pc)
        }
        mail?.let {
            val addresses = arrayListOf<String>()
            mailAddresses?.forEach {
                addresses.add(it.toString().substringBefore("(").trim())
            }
            this.mailSender.send(addresses,"订单通知",mail)
        }
        return r
    }

    @Action("confirmOrderPayment")
    fun confirmOrderPayment(@RequestBody modelData: ModelDataObject?, pc:PartnerCache):ActionResult?{
        var r = ActionResult(ErrorCode.UNKNOW)
        var d = this.rawRead(criteria = eq(this.id,modelData?.idFieldValue?.value),partnerCache = pc,useAccessControl = true)?.firstOrNull()
        d?.getFieldValue(this.step)?.let {
            if(it!=CustomerOrderStep.INVOICE_STEP.step){
                r.description="未开票订单"
            }
            else{
                var mo= ModelDataObject(model = this)
                mo.setFieldValue(this.id,modelData?.idFieldValue?.value)
                mo.setFieldValue(this.step,CustomerOrderStep.PAYMENT_STEP.step)
                this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = true)
                r.errorCode=ErrorCode.SUCCESS
            }
        }
        return r
    }


    override fun loadCreateModelViewData(mv: ModelView, viewData: MutableMap<String, Any>, pc: PartnerCache, ownerFieldValue: FieldValue?, toField: FieldBase?, ownerModelID: Long?, reqData: JsonObject?): ModelDataObject? {

        if(ownerModelID!=null && ownerModelID>0){
           var co = CustomerOpportunity.ref.rawRead(criteria = eq(CustomerOpportunity.ref.id,ownerModelID),partnerCache = pc,useAccessControl = true)?.firstOrNull()
            co?.let {
                var retCO = ModelDataObject(model = this)
                retCO.setFieldValue(ref.opportunity,co)
                retCO.setFieldValue(ref.customer,co.getFieldValue(CustomerOpportunity.ref.customer))
                retCO.setFieldValue(ref.price,co.getFieldValue(CustomerOpportunity.ref.price))
                retCO.setFieldValue(ref.title,co.getFieldValue(CustomerOpportunity.ref.title))
                return retCO
            }
        }

        return super.loadCreateModelViewData(mv, viewData, pc, ownerFieldValue, toField, ownerModelID, reqData)
    }

    override fun afterCreateObject(modelDataObject: ModelDataObject, useAccessControl:Boolean, pc:PartnerCache?):Pair<Boolean,String?>{

        //super.afterCreateObject(modelDataObject)
        var d = modelDataObject.getFieldValue(ref.opportunity)
       if(d is ModelDataObject){
          val oid= d.getFieldValue(ref.id)
           oid?.let {
               var mo = ModelDataObject(model = CrmCustomerOpportunityOrderProductRel.ref)
               mo.setFieldValue(CrmCustomerOpportunityOrderProductRel.ref.customerOrder,modelDataObject.idFieldValue?.value)
              val ret=  CrmCustomerOpportunityOrderProductRel.ref.rawEdit(mo,eq(CrmCustomerOpportunityOrderProductRel.ref.customerOpportunity,
                       oid),useAccessControl,pc)
//               if(ret.first==null){
//                   return Pair(false,"添加失败")
//               }
           }
       }
        else{
           d?.let {
               val oid = (d as BigInteger).toLong()
               var mo = ModelDataObject(model = CrmCustomerOpportunityOrderProductRel.ref)
               mo.setFieldValue(CrmCustomerOpportunityOrderProductRel.ref.customerOrder,modelDataObject.idFieldValue?.value)
               val ret = CrmCustomerOpportunityOrderProductRel.ref.rawEdit(mo,eq(CrmCustomerOpportunityOrderProductRel.ref.customerOpportunity,
                       oid),useAccessControl,pc)
//               if(ret.first==null){
//                   return Pair(false,"添加失败")
//               }
           }
       }
        return Pair(true,null)
    }
    fun setStep(id:Long,step:Int,pc:PartnerCache?,useAccessControl:Boolean){
        var mo = ModelDataObject(model = this)
        mo.setFieldValue(this.id,id)
        mo.setFieldValue(this.step,step)
        this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = useAccessControl)
    }

    override fun fillEditModelViewMeta(mv: ModelView,
                                       modelData: ModelData?,
                                       viewData: MutableMap<String, Any>,
                                       pc: PartnerCache,
                                       ownerFieldValue: FieldValue?,
                                       toField: FieldBase?,
                                       reqData: JsonObject?): ModelView {

         super.fillEditModelViewMeta(mv, modelData, viewData, pc, ownerFieldValue, toField, reqData)

         if(mv.viewType == ModelView.ViewType.MODEL_ACTION){
            var orderID = TypeConvert.getLong((modelData as ModelDataObject?)?.idFieldValue?.value as Number?)
            orderID?.let {
                var order = this.rawRead(this.customer,criteria = eq(this.id,orderID))?.firstOrNull()
                order?.let {odi->
                    var customer = odi.getFieldValue(this.customer) as ModelDataObject?
                    customer?.let { ci->
                        val customerID = TypeConvert.getLong(ci.idFieldValue?.value as Number?)
                        val addresses = CustomerContactAddress.ref.rawRead(criteria = eq(CustomerContactAddress.ref.customer,customerID))?.toModelDataObjectArray()
                        var mobiles = JsonArray()
                        var emails =JsonArray()
                        mobiles.add("${ci.getFieldValue(Customer.ref.mobile)}(${ci.getFieldValue(Customer.ref.name)})")
                        emails.add("${ci.getFieldValue(Customer.ref.email)}(${ci.getFieldValue(Customer.ref.name)})")
                        addresses?.forEach {addi->
                            mobiles.add("${addi.getFieldValue(CustomerContactAddress.ref.mobile)}(${addi.getFieldValue(CustomerContactAddress.ref.name)})")
                            emails.add("${addi.getFieldValue(CustomerContactAddress.ref.email)}(${addi.getFieldValue(CustomerContactAddress.ref.name)})")

                        }
                        mv.fields.firstOrNull {
                            it.name == this.smsMobiles.propertyName
                        }?.let {
                            val options = JsonObject()
                            options.add("options",mobiles)
                            it.meta = options
                        }

                        mv.fields.firstOrNull {
                            it.name == this.mailAddresses.propertyName
                        }?.let {
                            val options = JsonObject()
                            options.add("options",emails)
                            it.meta = options
                        }
                    }
                }
            }
        }
        return mv
    }
}