package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("crmCustomerOpportunityProductRel")
class CrmCustomerOpportunityProductRel:ContextModel("crm_customer_opportunity_product_rel","public") {
    companion object : RefSingleton<CrmCustomerOpportunityProductRel> {
        override lateinit var ref: CrmCustomerOpportunityProductRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val product = ModelMany2OneField(null,
            "product_id",
            FieldType.BIGINT,
            "产品",
            targetModelTable = "public.crm_product",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val customerOpportunity=ModelMany2OneField(null,
            "customer_opportunity_id",
            FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val count = ModelField(null,
            "count",
            FieldType.INT,
            "产品数量")
}