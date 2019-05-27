package work.bg.server.product.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2ManyField


@Model(name="product")
class Product:ContextModel("product_product","public") {
    companion object : RefSingleton<Product> {
        override lateinit var ref: Product
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null,
            "name",
            FieldType.STRING,
            title = "名称",
            defaultValue = "")
    val cost = ModelField(null,
            "cost",
            FieldType.DECIMAL,
            title = "成本",
            defaultValue = 0)
    val price = ModelField(null,
            "price",
            FieldType.DECIMAL,
            title = "售价",
            defaultValue = 0)
    val count =  ModelField(null,
            "count",
            FieldType.BIGINT,
            title = "数量",
            defaultValue = 0)
    val img =  ModelField(null,
            "img",
            FieldType.STRING,
            title = "图片",
            defaultValue = 0)
    val attributeMap = ModelOne2ManyField(null,
            "attribute_map",
            FieldType.BIGINT,
            "属性集",
            targetModelTable = "public.product_attribute_value_map",
            targetModelFieldName = "product")
}