package work.bg.server.product.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView

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
    val comment = ModelField(null,"c_comment", FieldType.STRING,title = "注释",defaultValue = "")
    val values = ModelOne2ManyField(null,
            "values",
            FieldType.BIGINT,
            "属性值",
            targetModelTable = "public.product_attribute_value",
            targetModelFieldName = "attribute_id")


    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "属性名称已经存在",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldNotNullOrEmpty(this.name,advice = "属性名称不能为空！")
        )
    }
}