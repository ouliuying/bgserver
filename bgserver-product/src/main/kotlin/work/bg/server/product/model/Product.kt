/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

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

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "产品名称已经存在",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldNotNullOrEmpty(this.name,advice = "产品名称不能为空！")
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldNotNullOrEmpty(this.name,advice = "产品名称不能为空！")
        )
    }

}