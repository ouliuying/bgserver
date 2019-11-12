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

import dynamic.model.query.mq.ModelDataObject
import work.bg.server.util.TypeConvert
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.eq
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import java.math.BigInteger

@Model("crmCustomerOpportunityOrderProductRel")
class CrmCustomerOpportunityOrderProductRel:ContextModel("crm_customer_opportunity_order_product_rel","public") {
    companion object : RefSingleton<CrmCustomerOpportunityOrderProductRel> {
        override lateinit var ref: CrmCustomerOpportunityOrderProductRel
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val product = dynamic.model.query.mq.ModelMany2OneField(null,
            "product_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "产品",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val customerOpportunity= dynamic.model.query.mq.ModelMany2OneField(null,
            "customer_opportunity_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val customerOrder= dynamic.model.query.mq.ModelMany2OneField(null,
            "customer_order_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val count = dynamic.model.query.mq.ModelField(null,
            "count",
            dynamic.model.query.mq.FieldType.INT,
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

    override fun afterCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var (result,msg) =  super.afterCreateObject(modelDataObject, useAccessControl, pc)
        if(!result){
            return Pair(result,msg)
        }
        val order = modelDataObject.getFieldValue(this.customerOrder)
        val opportunity = modelDataObject.getFieldValue((this.customerOpportunity))
        val orderID = (if(order is dynamic.model.query.mq.ModelDataObject) order.idFieldValue?.value as BigInteger? else order as BigInteger?)?.toLong()
        val opportunityID = TypeConvert.getLong(if(opportunity is dynamic.model.query.mq.ModelDataObject) opportunity.idFieldValue?.value as Number? else opportunity as Number?)
        var id = modelDataObject.idFieldValue?.value
        id?.let {
            var mo = dynamic.model.query.mq.ModelDataObject(model = this)
            mo.setFieldValue(this.id,id)
            if(orderID==null && opportunityID!=null){
                val opportunityObj=CustomerOpportunity.ref.rawRead(criteria = eq(CustomerOpportunity.ref.id,opportunityID),partnerCache = pc,useAccessControl = useAccessControl)?.firstOrNull()
                val orderObj = opportunityObj?.getFieldValue(CustomerOpportunity.ref.order) as dynamic.model.query.mq.ModelDataObject?
                val orderObjID =TypeConvert.getLong(orderObj?.idFieldValue?.value as Number?)
                if(orderObjID!=null){
                    mo.setFieldValue(this.customerOrder,orderObjID)
                }
            }
            else if(orderID!=null && opportunityID==null){
                val orderObj=CustomerOrder.ref.rawRead(criteria = eq(CustomerOrder.ref.id,orderID),partnerCache = pc,useAccessControl = useAccessControl)?.firstOrNull()
                val opportunityObj = orderObj?.getFieldValue(CustomerOrder.ref.opportunity) as dynamic.model.query.mq.ModelDataObject?
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
    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}