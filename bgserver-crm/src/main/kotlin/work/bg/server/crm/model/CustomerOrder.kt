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
import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.core.ui.ModelView
import work.bg.server.errorcode.ErrorCode
import java.math.BigInteger

@Model("customerOrder")
class CustomerOrder:
        ContextModel("crm_customer_order","public") {
    companion object : RefSingleton<CustomerOrder> {
        override lateinit var ref: CustomerOrder
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val title=ModelField(null,
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
            foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))

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

    val sms = FunctionField<String>(null,"sms",FieldType.TEXT,"短信")
    val mail = FunctionField<String>(null,"mail",FieldType.TEXT,"邮件")


    @Action("confirmCustomerOrder")
    fun confirmCustomerOrder(@RequestBody modelData:ModelDataObject?, pc:PartnerCache): ActionResult?{
        var r = ActionResult(ErrorCode.UNKNOW)
        var d = this.acRead(criteria = eq(this.id,modelData?.idFieldValue?.value),partnerCache = pc)?.firstOrNull()
       d?.getFieldValue(this.step)?.let {
           if(it!=CustomerOrderStep.NEW_STEP.step){
                r.description="只能确认信订单"
           }
           else{
               var mo=ModelDataObject(model=this)
               mo.setFieldValue(this.id,modelData?.idFieldValue?.value)
               mo.setFieldValue(this.step,CustomerOrderStep.CONFIRM_STEP.step)
               this.acEdit(mo,criteria = null,partnerCache = pc)
               r.errorCode=ErrorCode.SUCCESS
           }
       }
        return r
    }

    @Action("notifyOrderStepBySmsEmail")
    fun notifyOrderStepBySmsEmail(@RequestBody modelData:ModelDataObject?, pc:PartnerCache):ActionResult?{
        var r =ActionResult()
        return r
    }

    @Action("confirmOrderPayment")
    fun confirmOrderPayment(@RequestBody modelData:ModelDataObject?, pc:PartnerCache):ActionResult?{
        var r = ActionResult(ErrorCode.UNKNOW)
        var d = this.acRead(criteria = eq(this.id,modelData?.idFieldValue?.value),partnerCache = pc)?.firstOrNull()
        d?.getFieldValue(this.step)?.let {
            if(it!=CustomerOrderStep.INVOICE_STEP.step){
                r.description="未开票订单"
            }
            else{
                var mo=ModelDataObject(model=this)
                mo.setFieldValue(this.id,modelData?.idFieldValue?.value)
                mo.setFieldValue(this.step,CustomerOrderStep.PAYMENT_STEP.step)
                this.acEdit(mo,criteria = null,partnerCache = pc)
                r.errorCode=ErrorCode.SUCCESS
            }
        }
        return r
    }


    override fun loadCreateModelViewData(mv: ModelView, viewData: MutableMap<String, Any>, pc: PartnerCache, ownerFieldValue: FieldValue?, toField: FieldBase?, ownerModelID: Long?, reqData: JsonObject?): ModelDataObject? {

        if(ownerModelID!=null && ownerModelID>0){
           var co = CustomerOpportunity.ref.acRead(criteria = eq(CustomerOpportunity.ref.id,ownerModelID),partnerCache = pc)?.firstOrNull()
            co?.let {
                var retCO = ModelDataObject(model=this)
                retCO.setFieldValue(CustomerOrder.ref.opportunity,co)
                retCO.setFieldValue(CustomerOrder.ref.customer,co.getFieldValue(CustomerOpportunity.ref.customer))
                retCO.setFieldValue(CustomerOrder.ref.price,co.getFieldValue(CustomerOpportunity.ref.price))
                retCO.setFieldValue(CustomerOrder.ref.title,co.getFieldValue(CustomerOpportunity.ref.title))
                return retCO
            }
        }

        return super.loadCreateModelViewData(mv, viewData, pc, ownerFieldValue, toField, ownerModelID, reqData)
    }

    override fun afterCreateObject(modelDataObject: ModelDataObject,useAccessControl:Boolean,pc:PartnerCache?):Pair<Boolean,String?>{

        //super.afterCreateObject(modelDataObject)
        var d = modelDataObject.getFieldValue(CustomerOrder.ref.opportunity)
       if(d is ModelDataObject){
          val oid= d.getFieldValue(CustomerOrder.ref.id)
           oid?.let {
               var mo = ModelDataObject(model=CrmCustomerOpportunityOrderProductRel.ref)
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
               var mo = ModelDataObject(model=CrmCustomerOpportunityOrderProductRel.ref)
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
        var mo = ModelDataObject(model=this)
        mo.setFieldValue(this.id,id)
        mo.setFieldValue(this.step,step)
        this.rawEdit(mo,criteria = null,partnerCache = pc,useAccessControl = useAccessControl)
    }

}