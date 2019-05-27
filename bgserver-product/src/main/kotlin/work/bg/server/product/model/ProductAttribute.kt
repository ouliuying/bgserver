package work.bg.server.product.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2ManyField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="productAttribute")
class ProductAttribute:ContextModel("product_attribute","public") {
    companion object : RefSingleton<ProductAttribute> {
        override lateinit var ref: ProductAttribute
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name = ModelField(null,"name", FieldType.STRING,title = "名称",defaultValue = "")

    val values = ModelOne2ManyField(null,
            "values",
            FieldType.BIGINT,
            "属性值",
            targetModelTable = "public.product_attribute_value",
            targetModelFieldName = "attribute")

}