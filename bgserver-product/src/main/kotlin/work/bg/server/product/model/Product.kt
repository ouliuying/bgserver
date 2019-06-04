package work.bg.server.product.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.ui.ModelView


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
    val uom =  ModelMany2OneField(null,
            "uom_id",
            FieldType.BIGINT,
            title = "单位",
            targetModelTable ="public.product_uom",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

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
            targetModelFieldName = "product_id")

    val skuPattern = ModelOne2OneField(null,
            "sku_pattern",
            FieldType.BIGINT,
            "Sku生成模式",
            targetModelTable = "public.product_sku_pattern",
            targetModelFieldName = "product_id",
            isVirtualField = true)

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "产品名称已经存在",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldNotNullOrEmpty(this.name,advice = "产品名称不能为空！")
        )
    }

}