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
import dynamic.model.query.mq.FieldForeignKey
import dynamic.model.query.mq.ForeignKeyAction
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView

@Model(name="productAttributeValueMap")
class ProductAttributeValueMap:ContextModel("product_attribute_value_map",
        "public") {
    companion object : RefSingleton<ProductAttributeValueMap> {
        override lateinit var ref: ProductAttributeValueMap
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val product = dynamic.model.query.mq.ModelMany2OneField(null,
            "product_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "产品",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val productAttribute= dynamic.model.query.mq.ModelMany2OneField(null,
            "product_attribute_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "属性",
            targetModelTable = "public.product_attribute",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val productAttributeValue = dynamic.model.query.mq.ModelMany2OneField(null,
            "product_attribute_value_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "属性值",
            targetModelTable = "public.product_attribute_value",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    override fun fillCreateModelViewMeta(mv: ModelView,
                                         modelData: dynamic.model.query.mq.ModelData?,
                                         viewData: MutableMap<String, Any>,
                                         pc: PartnerCache,
                                         ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                         toField: dynamic.model.query.mq.FieldBase?,
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
                            var dataArray = (tModel as ContextModel).rawRead(partnerCache = pc,
                                    useAccessControl = true,
                                    criteria = null,
                                    pageIndex = 1,
                                    pageSize = 10,
                                    attachedFields = arrayOf(dynamic.model.query.mq.AttachedField(ProductAttribute.ref.values)))
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
    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}