package work.bg.server.account.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2OneField
import work.bg.server.core.spring.boot.annotation.Model

//应付款
@Model("accountPayable")
class AccountPayable:ContextModel("account_payable","public") {
    companion object: RefSingleton<AccountPayable> {
        override lateinit var ref: AccountPayable
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val orderInvoice= ModelOne2OneField(null,
            "order_invoice_id",
            FieldType.BIGINT,
            "发票",
            targetModelTable = "public.crm_customer_order_invoice",
            targetModelFieldName = "id")

}