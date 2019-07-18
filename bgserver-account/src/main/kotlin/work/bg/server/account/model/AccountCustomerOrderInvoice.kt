package work.bg.server.account.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelDataObject
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2OneField
import work.bg.server.core.spring.boot.annotation.Model
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

    override fun afterCreateObject(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?): Pair<Boolean, String?> {
        var ret= super.afterCreateObject(modelDataObject, useAccessControl, pc)
        if(!ret.first){
            return ret
        }
        val payTyp = modelDataObject.getFieldValue(this.payTyp) as Int?
        //payType=1 应付， payType=0 应收
        if(payTyp==1){
            var ar =ModelDataObject(model=AccountPayable.ref)
            ar.setFieldValue(AccountPayable.ref.orderInvoice,modelDataObject)
            AccountPayable.ref.rawCreate(ar,useAccessControl,pc)
        }
        else{
            var ar =ModelDataObject(model=AccountReceivable.ref)
            ar.setFieldValue(AccountReceivable.ref.orderInvoice,modelDataObject)
            AccountReceivable.ref.rawCreate(ar,useAccessControl,pc)
        }
        return Pair(true,null)
    }
}