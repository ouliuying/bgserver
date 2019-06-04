/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.product.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2ManyField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="productUOM")
class ProductUOM :ContextModel("product_uom","public"){
    companion object : RefSingleton<ProductUOM> {
        override lateinit var ref: ProductUOM
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
    val comment = ModelField(null,
            "c_comment",
            FieldType.STRING,
            title = "注释",
            defaultValue = "")
    val products=ModelOne2ManyField(null,
            "product_umo",
            FieldType.BIGINT,
            "对应产品",
            targetModelTable = "public.product_product",
            targetModelFieldName = "uom_id")

}