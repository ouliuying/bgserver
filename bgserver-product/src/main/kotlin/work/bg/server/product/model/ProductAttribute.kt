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

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model(name="productAttribute")
class ProductAttribute:ContextModel("product_attribute","public") {
    companion object : RefSingleton<ProductAttribute> {
        override lateinit var ref: ProductAttribute
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val name = dynamic.model.query.mq.ModelField(null, "name", dynamic.model.query.mq.FieldType.STRING, title = "名称", defaultValue = "")
    val comment = dynamic.model.query.mq.ModelField(null, "c_comment", dynamic.model.query.mq.FieldType.STRING, title = "注释", defaultValue = "")
    val values = dynamic.model.query.mq.ModelOne2ManyField(null,
            "values",
            dynamic.model.query.mq.FieldType.BIGINT,
            "属性值",
            targetModelTable = "public.product_attribute_value",
            targetModelFieldName = "attribute_id")


    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "属性名称已经存在",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "属性名称已经存在",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldNotNullOrEmpty(this.name,advice = "属性名称不能为空！")
        )
    }
    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldNotNullOrEmpty(this.name,advice = "属性名称不能为空！")
        )
    }
}