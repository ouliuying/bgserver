package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelMany2ManyField
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.product.model.Product

@Model("product")
class CrmProduct:Product(){
    companion object : RefSingleton<CrmProduct> {
        override lateinit var ref: CrmProduct
    }

    val customerOpportunities = ModelMany2ManyField(null,
            "customer_opportunities",
            FieldType.BIGINT,
            "商机",
            relationModelTable = "crm_customer_opportunity_product_rel",
            relationModelFieldName = "customer_opportunity_id",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id")

}