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

import dynamic.model.query.mq.FieldForeignKey
import dynamic.model.query.mq.ForeignKeyAction
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model(name="productAttributeValue")
class ProductAttributeValue:ContextModel("product_attribute_value","public") {
    companion object : RefSingleton<ProductAttributeValue> {
        override lateinit var ref: ProductAttributeValue
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val attribute= dynamic.model.query.mq.ModelMany2OneField(null,
            "attribute_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "属性",
            targetModelTable = "public.product_attribute",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val attrValue= dynamic.model.query.mq.ModelField(null,
            "attr_value",
            dynamic.model.query.mq.FieldType.STRING,
            "值")
    val attrValueComment= dynamic.model.query.mq.ModelField(null,
            "attr_value_comment",
            dynamic.model.query.mq.FieldType.STRING,
            "值注释")
}