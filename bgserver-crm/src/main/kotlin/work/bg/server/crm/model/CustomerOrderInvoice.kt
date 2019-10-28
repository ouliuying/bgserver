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

import dynamic.model.query.mq.*
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

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val order = ModelOne2OneField(null,
            "order_id",
            FieldType.BIGINT,
            "订单",
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "id")

    val fromCorpName = ModelField(null,
            "from_corp_name",
            FieldType.STRING,
            "开票方")

    val toCorpName = ModelField(null,
            "to_corp_name",
            FieldType.STRING,
            "受票方")

    val amount= ModelField(null,
            "amount",
            FieldType.NUMBER,
            "金额")

    val accountPartner = ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "开票人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            defaultValue = CurrPartnerBillboard())


    //0 普通发票，1 增值税发票 2 收据
    val typ = ModelField(null,
            "typ",
            FieldType.INT,
            "类型",
            defaultValue = 0)

//    //-1 正在处理，0 取消 1 成功
//    val status = ModelField(null,
//            "status",
//            FieldType.INT,
//            "状态",
//            defaultValue = -1)


    val comment = ModelField(null, "comment", FieldType.TEXT, "附加说明")




    override fun afterCreateObject(modelDataObject: ModelDataObject,
                                   useAccessControl: Boolean,
                                   pc: PartnerCache?): Pair<Boolean, String?> {

        modelDataObject.getFieldValue(this.order)?.let {
            when(it){
                is ModelDataObject ->{
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