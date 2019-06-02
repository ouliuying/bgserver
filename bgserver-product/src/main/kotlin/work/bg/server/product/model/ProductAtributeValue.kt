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
    companion object : RefSingleton<ProductAtributeValue> {
        override lateinit var ref: ProductAtributeValue
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val attribute=ModelMany2OneField(null,
            "attribute_id",
            FieldType.BIGINT,
            "属性",
            targetModelTable = "public.product_attribute",
            targetModelFieldName = "id")

    val attrValue=ModelField(null,
            "attr_value",
            FieldType.STRING,
            "值")
    val attrValueComment=ModelField(null,
            "attr_value_comment",
            FieldType.STRING,
            "值注释")
}