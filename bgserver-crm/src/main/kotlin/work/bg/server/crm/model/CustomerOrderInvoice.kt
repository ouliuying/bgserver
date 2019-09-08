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

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import dynamic.model.web.spring.boot.annotation.Model
import java.math.BigInteger

//订单发票

@Model("customerOrderInvoice")
open class CustomerOrderInvoice:ContextModel("crm_customer_order_invoice","public") {
    companion object : RefSingleton<CustomerOrderInvoice> {
        override lateinit var ref: CustomerOrderInvoice
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val order = dynamic.model.query.mq.ModelOne2OneField(null,
            "order_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "订单",
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "id")

    val fromCorpName = dynamic.model.query.mq.ModelField(null,
            "from_corp_name",
            dynamic.model.query.mq.FieldType.STRING,
            "开票方")

    val toCorpName = dynamic.model.query.mq.ModelField(null,
            "to_corp_name",
            dynamic.model.query.mq.FieldType.STRING,
            "受票方")

    val amount= dynamic.model.query.mq.ModelField(null,
            "amount",
            dynamic.model.query.mq.FieldType.NUMBER,
            "金额")

    val accountPartner = dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "开票人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            defaultValue = CurrPartnerBillboard())


    //0 普通发票，1 增值税发票 2 收据
    val typ = dynamic.model.query.mq.ModelField(null,
            "typ",
            dynamic.model.query.mq.FieldType.INT,
            "类型",
            defaultValue = 0)
    val comment = dynamic.model.query.mq.ModelField(null, "comment", dynamic.model.query.mq.FieldType.TEXT, "附加说明")




    override fun afterCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                   useAccessControl: Boolean,
                                   pc: PartnerCache?): Pair<Boolean, String?> {

        modelDataObject.getFieldValue(this.order)?.let {
            when(it){
                is dynamic.model.query.mq.ModelDataObject ->{
                    it.idFieldValue?.value?.let {
                        CustomerOrder.ref.setStep((it as BigInteger).toLong(),CustomerOrderStep.INVOICE_STEP.step,pc,useAccessControl)
                    }
                }
                else->{
                    CustomerOrder.ref.setStep((it as BigInteger).toLong(),CustomerOrderStep.INVOICE_STEP.step,pc,useAccessControl)
                }
            }
        }



        return Pair(true,null)
    }
}