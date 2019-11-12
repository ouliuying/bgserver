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

import dynamic.model.query.mq.*
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import dynamic.model.web.spring.boot.annotation.Model
import org.springframework.beans.factory.annotation.Autowired
import work.bg.server.core.acrule.ModelCreateRecordFieldsValueFilterRule
import work.bg.server.core.acrule.ModelEditRecordFieldsValueFilterRule
import work.bg.server.core.model.ContextModel
import work.bg.server.core.model.field.EventLogField
import work.bg.server.product.acrule.bean.ModelCreateProductInnerRecordFieldsValueFilterBean
import work.bg.server.product.acrule.bean.ModelEditProductInnerRecordFieldsValueFilterBean


@Model(name="product",title = "产品")
class Product:ContextModel("product_product","public") {
    @Autowired
    private lateinit var createInnerRecordFieldsValueFilterBean:ModelCreateProductInnerRecordFieldsValueFilterBean
    @Autowired
    private lateinit var editInnerRecordInnerFieldsValueFilterBean:ModelEditProductInnerRecordFieldsValueFilterBean
    companion object : RefSingleton<Product> {
        override lateinit var ref: Product
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
    val cost = dynamic.model.query.mq.ModelField(null,
            "cost",
            dynamic.model.query.mq.FieldType.DECIMAL,
            title = "成本",
            defaultValue = 0)
    val price = dynamic.model.query.mq.ModelField(null,
            "price",
            dynamic.model.query.mq.FieldType.DECIMAL,
            title = "售价",
            defaultValue = 0)
    val uom = dynamic.model.query.mq.ModelMany2OneField(null,
            "uom_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            title = "单位",
            targetModelTable = "public.product_uom",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.SET_NULL))

    val img = dynamic.model.query.mq.ModelField(null,
            "img",
            dynamic.model.query.mq.FieldType.STRING,
            title = "图片",
            defaultValue = 0)

    val attributeMap = dynamic.model.query.mq.ModelOne2ManyField(null,
            "attribute_map",
            dynamic.model.query.mq.FieldType.BIGINT,
            "属性集",
            targetModelTable = "public.product_attribute_value_map",
            targetModelFieldName = "product_id")

    val sku = ModelField(null,"sku", FieldType.STRING,"SKU")

    val skuPattern = ModelMany2OneField(null,
            "sku_pattern_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "Sku生成模式",
            targetModelTable = "public.product_sku_pattern",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.SET_NULL))

    val eventLogs = EventLogField(null,"event_logs","跟踪日志")

    override fun getModelCreateAccessFieldFilterRule(): ModelCreateRecordFieldsValueFilterRule<*>? {
        return this.createInnerRecordFieldsValueFilterBean
    }

    override fun getModelEditAccessFieldFilterRule(): ModelEditRecordFieldsValueFilterRule<*,*>? {
        return this.editInnerRecordInnerFieldsValueFilterBean
    }
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