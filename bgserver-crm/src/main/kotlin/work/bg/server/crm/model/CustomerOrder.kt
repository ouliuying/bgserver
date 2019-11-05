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
import org.springframework.web.bind.annotation.RequestBody
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.eq
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.model.ActionResult
import work.bg.server.core.model.field.EventLogField
import java.math.BigInteger

@Model("customerOrder","客户订单")
class CustomerOrder:
        ContextModel("crm_customer_order","public") {
    companion object : RefSingleton<CustomerOrder> {
        override lateinit var ref: CustomerOrder
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val title= dynamic.model.query.mq.ModelField(null,
            "title",
            dynamic.model.query.mq.FieldType.STRING,
            "标题")

    val price = dynamic.model.query.mq.ModelField(null,
            "price",
            dynamic.model.query.mq.FieldType.NUMBER,
            "金额")

    val customer= dynamic.model.query.mq.ModelMany2OneField(null,
            "customer_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val products = dynamic.model.query.mq.ModelMany2ManyField(null,
            "products",
            dynamic.model.query.mq.FieldType.BIGINT,
            "产品",
            relationModelTable = "public.crm_customer_opportunity_order_product_rel",
            relationModelFieldName = "product_id",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id")

    val quotation = dynamic.model.query.mq.ModelOne2OneField(null,
            "quotation",
            dynamic.model.query.mq.FieldType.BIGINT,
            "报价单",
            isVirtualField = true,
            targetModelTable = "public.crm_customer_opportunity_order_quotation",
            targetModelFieldName = "order_id")

    val opportunity = dynamic.model.query.mq.ModelOne2OneField(null,
            "opportunity_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id"
    )

    val step = dynamic.model.query.mq.ModelField(null,
            "step",
            dynamic.model.query.mq.FieldType.INT,
            "状态",
            defaultValue = 0)

    val invoice = dynamic.model.query.mq.ModelOne2OneField(null,
            "invoice",
            fieldType = dynamic.model.query.mq.FieldType.BIGINT,
            title = "票据",
            isVirtualField = true,
            targetModelTable = "public.crm_customer_order_invoice",
            targetModelFieldName = "order_id")

    val sms = dynamic.model.query.mq.FunctionField<String,PartnerCache>(null, "sms", dynamic.model.query.mq.FieldType.TEXT, "短信")
    val mail = dynamic.model.query.mq.FunctionField<String,PartnerCache>(null, "mail", dynamic.model.query.mq.FieldType.TEXT, "邮件")
    val eventLogs = EventLogField(null,"event_logs","跟踪日志")

    @Action("confirmCustomerOrder")
    fun confirmCustomerOrder(@RequestBody modelData: dynamic.model.query.mq.ModelDataObject?, pc:PartnerCache): ActionResult?{
        var r = ActionResult(ErrorCode.UNKNOW)
        var d = this.rawRead(criteria = eq(this.id,modelData?.idFieldValue?.value),partnerCache = pc,useAccessControl = true)?.firstOrNull()
       d?.getFieldValue(this.step)?.let {
           if(it!=CustomerOrderStep.NEW_STEP.step){
                r.description="只能确认信订单"
           }
           else{
               var mo= dynamic.model.query.mq.ModelDataObject(model = this)
               mo.setFieldValue(this.id,modelData?.idFieldValue?.value)
               mo.setFieldValue(this.step,CustomerOrderStep.CONFIRM_STEP.step)
               this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = true)
               r.errorCode=ErrorCode.SUCCESS
           }
       }
        return r
    }

    @Action("notifyOrderStepBySmsEmail")
    fun notifyOrderStepBySmsEmail(@RequestBody modelData: dynamic.model.query.mq.ModelDataObject?, pc:PartnerCache):ActionResult?{
        var r =ActionResult()
        return r
    }

    @Action("confirmOrderPayment")
    fun confirmOrderPayment(@RequestBody modelData: dynamic.model.query.mq.ModelDataObject?, pc:PartnerCache):ActionResult?{
        var r = ActionResult(ErrorCode.UNKNOW)
        var d = this.rawRead(criteria = eq(this.id,modelData?.idFieldValue?.value),partnerCache = pc,useAccessControl = true)?.firstOrNull()
        d?.getFieldValue(this.step)?.let {
            if(it!=CustomerOrderStep.INVOICE_STEP.step){
                r.description="未开票订单"
            }
            else{
                var mo= dynamic.model.query.mq.ModelDataObject(model = this)
                mo.setFieldValue(this.id,modelData?.idFieldValue?.value)
                mo.setFieldValue(this.step,CustomerOrderStep.PAYMENT_STEP.step)
                this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = true)
                r.errorCode=ErrorCode.SUCCESS
            }
        }
        return r
    }


    override fun loadCreateModelViewData(mv: ModelView, viewData: MutableMap<String, Any>, pc: PartnerCache, ownerFieldValue: dynamic.model.query.mq.FieldValue?, toField: dynamic.model.query.mq.FieldBase?, ownerModelID: Long?, reqData: JsonObject?): dynamic.model.query.mq.ModelDataObject? {

        if(ownerModelID!=null && ownerModelID>0){
           var co = CustomerOpportunity.ref.rawRead(criteria = eq(CustomerOpportunity.ref.id,ownerModelID),partnerCache = pc,useAccessControl = true)?.firstOrNull()
            co?.let {
                var retCO = dynamic.model.query.mq.ModelDataObject(model = this)
                retCO.setFieldValue(CustomerOrder.ref.opportunity,co)
                retCO.setFieldValue(CustomerOrder.ref.customer,co.getFieldValue(CustomerOpportunity.ref.customer))
                retCO.setFieldValue(CustomerOrder.ref.price,co.getFieldValue(CustomerOpportunity.ref.price))
                retCO.setFieldValue(CustomerOrder.ref.title,co.getFieldValue(CustomerOpportunity.ref.title))
                return retCO
            }
        }

        return super.loadCreateModelViewData(mv, viewData, pc, ownerFieldValue, toField, ownerModelID, reqData)
    }

    override fun afterCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl:Boolean, pc:PartnerCache?):Pair<Boolean,String?>{

        //super.afterCreateObject(modelDataObject)
        var d = modelDataObject.getFieldValue(CustomerOrder.ref.opportunity)
       if(d is dynamic.model.query.mq.ModelDataObject){
          val oid= d.getFieldValue(CustomerOrder.ref.id)
           oid?.let {
               var mo = dynamic.model.query.mq.ModelDataObject(model = CrmCustomerOpportunityOrderProductRel.ref)
               mo.setFieldValue(CrmCustomerOpportunityOrderProductRel.ref.customerOrder,modelDataObject.idFieldValue?.value)
              val ret=  CrmCustomerOpportunityOrderProductRel.ref.rawEdit(mo,eq(CrmCustomerOpportunityOrderProductRel.ref.customerOpportunity,
                       oid),useAccessControl,pc)
               if(ret.first==null){
                   return Pair(false,"添加失败")
               }
           }
       }
        else{
           d?.let {
               val oid = (d as BigInteger).toLong()
               var mo = dynamic.model.query.mq.ModelDataObject(model = CrmCustomerOpportunityOrderProductRel.ref)
               mo.setFieldValue(CrmCustomerOpportunityOrderProductRel.ref.customerOrder,modelDataObject.idFieldValue?.value)
               val ret = CrmCustomerOpportunityOrderProductRel.ref.rawEdit(mo,eq(CrmCustomerOpportunityOrderProductRel.ref.customerOpportunity,
                       oid),useAccessControl,pc)
               if(ret.first==null){
                   return Pair(false,"添加失败")
               }
           }
       }
        return Pair(true,null)
    }
    fun setStep(id:Long,step:Int,pc:PartnerCache?,useAccessControl:Boolean){
        var mo = dynamic.model.query.mq.ModelDataObject(model = this)
        mo.setFieldValue(this.id,id)
        mo.setFieldValue(this.step,step)
        this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = useAccessControl)
    }

}