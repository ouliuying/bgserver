package work.bg.server.product.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelMany2OneField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="productAttributeValueMap")
class ProductAttributeValueMap:ContextModel("product_attribute_value_map",
        "public") {
    companion object : RefSingleton<ProductAttributeValueMap> {
        override lateinit var ref: ProductAttributeValueMap
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
            targetModelTable = "public.product_product",
            targetModelFieldName = "id")

    val productAttribute=ModelMany2OneField(null,
            "product_attribute_id",
            FieldType.BIGINT,
            "属性",
            targetModelTable = "public.product_attribute",
            targetModelFieldName = "id")

    val productAttributeValue = ModelMany2OneField(null,
            "product_attribute_value_id",
            FieldType.BIGINT,
            "属性值",
            targetModelTable = "public.product_attribute_value",
            targetModelFieldName = "id")

}