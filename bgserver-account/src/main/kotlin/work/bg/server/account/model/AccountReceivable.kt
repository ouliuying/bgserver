package work.bg.server.account.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2OneField
import work.bg.server.core.spring.boot.annotation.Model

//应收款
@Model("accountReceivable")
class AccountReceivable:ContextModel("account_receivable","public") {
    companion object: RefSingleton<AccountReceivable> {
        override lateinit var ref: AccountReceivable
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