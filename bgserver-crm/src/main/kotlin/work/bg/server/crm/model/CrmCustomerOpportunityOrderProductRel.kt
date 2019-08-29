/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.crm.model

import work.bg.server.util.TypeConvert
import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
import java.math.BigInteger

@Model("crmCustomerOpportunityOrderProductRel")
class CrmCustomerOpportunityOrderProductRel:ContextModel("crm_customer_opportunity_order_product_rel","public") {
    companion object : RefSingleton<CrmCustomerOpportunityOrderProductRel> {
        override lateinit var ref: CrmCustomerOpportunityOrderProductRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val product = ModelMany2OneField(null,
            "product_id",
            FieldType.BIGINT,
            "产品",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val customerOpportunity=ModelMany2OneField(null,
            "customer_opportunity_id",
            FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val customerOrder=ModelMany2OneField(null,
            "customer_order_id",
            FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val count = ModelField(null,
            "count",
            FieldType.INT,
            "产品数量")

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.product,this.customerOrder,advice = "订单中商品必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP),
                ModelFieldUnique(this.product,this.customerOpportunity,advice = "商机中商品必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.product,this.customerOrder,advice = "订单中商品必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP),
                ModelFieldUnique(this.product,this.customerOpportunity,advice = "商机中商品必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun afterCreateObject(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var (result,msg) =  super.afterCreateObject(modelDataObject, useAccessControl, pc)
        if(!result){
            return Pair(result,msg)
        }
        val order = modelDataObject.getFieldValue(this.customerOrder)
        val opportunity = modelDataObject.getFieldValue((this.customerOpportunity))
        val orderID = (if(order is ModelDataObject) order?.idFieldValue?.value as BigInteger? else order as BigInteger?)?.toLong()
        val opportunityID = (if(opportunity is ModelDataObject) opportunity?.idFieldValue?.value as BigInteger? else opportunity as BigInteger?)?.toLong()
        var id = modelDataObject.idFieldValue?.value
        id?.let {
            var mo = ModelDataObject(model=this)
            mo.setFieldValue(this.id,id)
            if(orderID==null && opportunityID!=null){
                val opportunityObj=CustomerOpportunity.ref.rawRead(criteria = eq(CustomerOpportunity.ref.id,opportunityID),partnerCache = pc,useAccessControl = useAccessControl)?.firstOrNull()
                val orderObj = opportunityObj?.getFieldValue(CustomerOpportunity.ref.order) as ModelDataObject?
                val orderObjID =TypeConvert.getLong(orderObj?.idFieldValue?.value as Number?)
                if(orderObjID!=null){
                    mo.setFieldValue(this.customerOrder,orderObjID)
                }
            }
            else if(orderID!=null && opportunityID==null){
                val orderObj=CustomerOrder.ref.rawRead(criteria = eq(CustomerOrder.ref.id,orderID),partnerCache = pc,useAccessControl = useAccessControl)?.firstOrNull()
                val opportunityObj = orderObj?.getFieldValue(CustomerOrder.ref.opportunity) as ModelDataObject?
                val opportunityObjID = TypeConvert.getLong(opportunityObj?.idFieldValue?.value as Number?)
                if(opportunityObjID!=null){
                    mo.setFieldValue(this.customerOpportunity,opportunityObjID)
                }
            }
            if(mo.data.count()>1){
                this.rawEdit(mo,useAccessControl = useAccessControl,partnerCache = pc,criteria = null)
            }
        }
        return Pair(true,null)
    }

}