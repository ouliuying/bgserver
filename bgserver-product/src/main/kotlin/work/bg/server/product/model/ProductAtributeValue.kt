package work.bg.server.product.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelMany2OneField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="productAttributeValue")
class ProductAtributeValue:ContextModel("product_attribute_value","public") {
    companion object : RefSingleton<ProductAttribute> {
        override lateinit var ref: ProductAttribute
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val attribute=ModelMany2OneField(null,
            "atrribute_id",
            FieldType.BIGINT,
            "属性",
            targetModelTable = "public.product_attribute",
            targetModelFieldName = "id")

    val attrValue=ModelField(null,
            "attr_value",
            FieldType.STRING,
            "值")
}