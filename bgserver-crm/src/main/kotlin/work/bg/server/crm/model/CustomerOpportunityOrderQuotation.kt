package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="customerOpportunityOrderQuotation")
class CustomerOpportunityOrderQuotation:
        ContextModel("crm_customer_opportunity_order_quotation","public") {
    companion object : RefSingleton<CustomerOpportunityOrderQuotation> {
        override lateinit var ref: CustomerOpportunityOrderQuotation
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())


    val opportunity=ModelOne2OneField(null,
            "opportunity_id",
            FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id"
            )

    val order=ModelOne2OneField(null,
            "order_id",
            FieldType.BIGINT,
            "订单",
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "id"
    )

    var tpl = ModelMany2OneField(null,
            "tpl_id",FieldType.BIGINT,
            "模板",
            targetModelTable = "public.crm_customer_opportunity_order_quotation_template",
            targetModelFieldName = "id")

    val comment= ModelField(null,
            "comment",
            FieldType.TEXT,
            "注释")
}