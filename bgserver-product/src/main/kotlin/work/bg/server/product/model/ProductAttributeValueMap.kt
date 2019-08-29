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
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView

@Model(name="productAttributeValueMap")
class ProductAttributeValueMap:ContextModel("product_attribute_value_map",
        "public") {
    companion object : RefSingleton<ProductAttributeValueMap> {
        override lateinit var ref: ProductAttributeValueMap
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val product = ModelMany2OneField(null,
            "product_id",
            FieldType.BIGINT,
            "产品",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id")

    val productAttribute=ModelMany2OneField(null,
            "product_attribute_id",
            FieldType.BIGINT,
            "属性",
            targetModelTable = "public.product_attribute",
            targetModelFieldName = "id")

    val productAttributeValue = ModelMany2OneField(null,
            "product_attribute_value_id",
            FieldType.BIGINT,
            "属性值",
            targetModelTable = "public.product_attribute_value",
            targetModelFieldName = "id")
    override fun fillCreateModelViewMeta(mv: ModelView,
                                         modelData: ModelData?,
                                         viewData: MutableMap<String, Any>,
                                         pc: PartnerCache,
                                         ownerFieldValue: FieldValue?,
                                         toField: FieldBase?,
                                         reqData: JsonObject?): ModelView {
        mv.fields.forEach {
            if (it.name == this.productAttribute.propertyName) {
                if (it.relationData != null &&
                        it.meta == null &&
                        it.style != ModelView.Field.Style.relation) {
                    var tModel = this.appModel.getModel(it.relationData!!.targetApp, it.relationData!!.targetModel)
                    if (tModel != null) {
                        var idField = tModel.fields.getIdField()
                        var toField = tModel.getFieldByPropertyName(it.relationData!!.toName!!)
                        if (idField != null && toField != null) {
                            var dataArray = (tModel as ContextModel).acRead(partnerCache = pc,
                                    criteria = null,
                                    pageIndex = 1,
                                    pageSize = 10,
                                    attachedFields = arrayOf(AttachedField(ProductAttribute.ref.values)))
                            if (dataArray != null) {
                                var jArr = JsonArray()
                                dataArray.toModelDataObjectArray().forEach { it ->
                                    jArr.add(this.gson.toJsonTree(it))
                                }
                                var metaObj = JsonObject()
                                metaObj.add("options", jArr)
                                it.meta = metaObj
                            }
                        }
                    }
                }
            }
        }
        return mv
    }
}