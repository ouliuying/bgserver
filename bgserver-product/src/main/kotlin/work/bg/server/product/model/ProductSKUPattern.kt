package work.bg.server.product.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="productSKUPattern")
class ProductSKUPattern:ContextModel("public.product_sku_pattern","public") {
    companion object : RefSingleton<ProductSKUPattern> {
        override lateinit var ref: ProductSKUPattern
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name=ModelField(null,
            "name",
            FieldType.STRING,
            "名称")

    val skuPattern = ModelField(null,
            "sku_pattern",
            FieldType.STRING,
            "Sku模式")

    val skuCount= ModelField(null,
            "sku_count",
            FieldType.INT,
            "Sku数量")
}