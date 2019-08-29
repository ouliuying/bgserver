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

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelMany2OneField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="productAttributeValue")
class ProductAttributeValue:ContextModel("product_attribute_value","public") {
    companion object : RefSingleton<ProductAttributeValue> {
        override lateinit var ref: ProductAttributeValue
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