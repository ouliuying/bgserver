/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *it under the terms of the GNU Affero General Public License as published by
t *  *  *he Free Software Foundation, either version 3 of the License.

 *  *  *This program is distributed in the hope that it will be useful,
 *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *GNU Affero General Public License for more details.

 *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  *
  *
  */

package work.bg.server.product.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model(name="productUOM",title = "产品单位")
class ProductUOM :ContextModel("product_uom","public"){
    companion object : RefSingleton<ProductUOM> {
        override lateinit var ref: ProductUOM
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val name = dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            title = "名称",
            defaultValue = "")
    val comment = dynamic.model.query.mq.ModelField(null,
            "c_comment",
            dynamic.model.query.mq.FieldType.STRING,
            title = "注释",
            defaultValue = "")
    val products= dynamic.model.query.mq.ModelOne2ManyField(null,
            "product_umo",
            dynamic.model.query.mq.FieldType.BIGINT,
            "对应产品",
            targetModelTable = "public.product_product",
            targetModelFieldName = "uom_id")

}