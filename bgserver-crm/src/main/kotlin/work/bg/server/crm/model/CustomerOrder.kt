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

package work.bg.server.crm.model

import com.google.gson.JsonObject
import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView
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
}