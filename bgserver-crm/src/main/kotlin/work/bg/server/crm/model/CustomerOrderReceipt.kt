package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

//订单收据
@Model("customerOrderReceipt")
class CustomerOrderReceipt:ContextModel("crm_customer_order_receipt","public") {
    companion object : RefSingleton<CustomerOrderReceipt> {
        override lateinit var ref: CustomerOrderReceipt
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
            "from_corp_name",
            FieldType.STRING,
            "受票方")
    val amount= ModelField(null,
            "amount",
            FieldType.STRING,
            "金额")
    val accountPartner = ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "开票人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")
}