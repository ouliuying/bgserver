package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.spring.boot.annotation.Model

@Model("crmCustomerOrderProductRel")
class CrmCustomerOrderProductRel:ContextModel("crm_customer_order_product_rel","public") {
    companion object : RefSingleton<CrmCustomerOrderProductRel> {
        override lateinit var ref: CrmCustomerOrderProductRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
}