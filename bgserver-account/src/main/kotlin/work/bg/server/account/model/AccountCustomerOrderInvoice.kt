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

package work.bg.server.account.model

import dynamic.model.query.mq.FieldType
import dynamic.model.query.mq.ModelField
import dynamic.model.query.mq.ModelOne2OneField
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.cache.PartnerCache
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.crm.model.CustomerOrderInvoice

@Model("customerOrderInvoice")
class AccountCustomerOrderInvoice:CustomerOrderInvoice() {
    companion object: RefSingleton<AccountCustomerOrderInvoice> {
        override lateinit var ref: AccountCustomerOrderInvoice
    }
    val payTyp = ModelField(null,
            "pay_type",
            FieldType.INT,
            "应付/应收",
            defaultValue = 0)


    //-1 正在处理，0 取消 1 成功
    val status = ModelField(null,
            "status",
            FieldType.INT,
            "状态",
            defaultValue = -1)

    val payable = ModelOne2OneField(null,
            "payable",
            FieldType.BIGINT,
            "应付款",
            targetModelTable = "public.account_payable",
            targetModelFieldName = "order_invoice_id",
            isVirtualField = true)

    val receivable = ModelOne2OneField(null,
            "receivable",
            FieldType.BIGINT,
            "应付款",
            targetModelTable = "public.account_receivable",
            targetModelFieldName = "order_invoice_id",
            isVirtualField = true)

    override fun afterCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var ret= super.afterCreateObject(modelDataObject, useAccessControl, pc)
        if(!ret.first){
            return ret
        }
        val payTyp = modelDataObject.getFieldValue(this.payTyp) as Int?
        //payType=1 应付， payType=0 应收
        if(payTyp==1){
            var ar = dynamic.model.query.mq.ModelDataObject(model = AccountPayable.ref)
            ar.setFieldValue(AccountPayable.ref.orderInvoice,modelDataObject)
            AccountPayable.ref.rawCreate(ar,useAccessControl,pc)
        }
        else{
            var ar = dynamic.model.query.mq.ModelDataObject(model = AccountReceivable.ref)
            ar.setFieldValue(AccountReceivable.ref.orderInvoice,modelDataObject)
            AccountReceivable.ref.rawCreate(ar,useAccessControl,pc)
        }
        return Pair(true,null)
    }
}