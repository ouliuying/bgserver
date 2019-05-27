package work.bg.server.account.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.BaseModelLog
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelOne2OneField
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.crm.model.CustomerOrderReceipt

@Model("customerOrderReceipt")
class AccountCustomerOrderReceipt:CustomerOrderReceipt() {
    companion object: RefSingleton<AccountCustomerOrderReceipt> {
        override lateinit var ref: AccountCustomerOrderReceipt
    }

    val payable = ModelOne2OneField(null,
            "orderReceipt",
            FieldType.BIGINT,
            "应付款",
            targetModelTable = "public.account_payable",
            targetModelFieldName = "orderInvoice",
            isVirtualField = true)

    val receivable = ModelOne2OneField(null,
            "orderReceipt",
            FieldType.BIGINT,
            "应付款",
            targetModelTable = "public.account_receivable",
            targetModelFieldName = "orderInvoice",
            isVirtualField = true)
}