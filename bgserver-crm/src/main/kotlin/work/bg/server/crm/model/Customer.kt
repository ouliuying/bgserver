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

package work.bg.server.crm.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.crm.field.ModelFullAddressField

@Model(name="customer",title = "客户")
class Customer: ContextModel("crm_customer","public") {
    companion object : RefSingleton<Customer> {
        override lateinit var ref: Customer
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val name = dynamic.model.query.mq.ModelField(null, "name", dynamic.model.query.mq.FieldType.STRING, title = "姓名", defaultValue = "")

    val isCorp = dynamic.model.query.mq.ModelField(null, "is_corp", dynamic.model.query.mq.FieldType.INT, title = "公司", defaultValue = -1)

    val event = dynamic.model.query.mq.ModelMany2OneField(null,
            "event_id", dynamic.model.query.mq.FieldType.BIGINT,
            title = "活动",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.SET_NULL))

    //个人属性
    val mobile = dynamic.model.query.mq.ModelField(null, "mobile", dynamic.model.query.mq.FieldType.STRING, title = "手机", defaultValue = "")
    val sex = dynamic.model.query.mq.ModelField(null, "sex", dynamic.model.query.mq.FieldType.INT, title = "性别", defaultValue = -1)
    //公司属性
    val fax = dynamic.model.query.mq.ModelField(null, "fax", dynamic.model.query.mq.FieldType.STRING, title = "传真", defaultValue = "")
    //通用属性
    val telephone = dynamic.model.query.mq.ModelField(null, "telephone", dynamic.model.query.mq.FieldType.STRING, title = "电话", defaultValue = "")
    val email = dynamic.model.query.mq.ModelField(null, "email", dynamic.model.query.mq.FieldType.STRING, title = "email", defaultValue = "")
    val province = dynamic.model.query.mq.ModelField(null, "province", dynamic.model.query.mq.FieldType.STRING, title = "省", defaultValue = "")
    val city = dynamic.model.query.mq.ModelField(null, "city", dynamic.model.query.mq.FieldType.STRING, title = "市", defaultValue = "")
    val district = dynamic.model.query.mq.ModelField(null, "district", dynamic.model.query.mq.FieldType.STRING, title = "区/县", defaultValue = "")
    val streetAddress = dynamic.model.query.mq.ModelField(null, "street_address", dynamic.model.query.mq.FieldType.STRING, "详细地址", defaultValue = "")
    val fullAddress by lazy {
        ModelFullAddressField(null,
                "fullAddress",
                "地址",
                this.province,this.city,
                this.district,
                this.streetAddress,
                this.gson)
    }
    val website = dynamic.model.query.mq.ModelField(null, "website", dynamic.model.query.mq.FieldType.STRING, title = "网址", defaultValue = "")
    val comment = dynamic.model.query.mq.ModelField(null, "c_comment", dynamic.model.query.mq.FieldType.STRING, title = "注释", defaultValue = "")
    val contactAddresses = dynamic.model.query.mq.ModelOne2ManyField(null, "contact_address_id", dynamic.model.query.mq.FieldType.BIGINT, "联系人",
            targetModelTable = "public.customer_contact_address", targetModelFieldName = "customer_id")

    //对应占有,辅助员工
    val partners = dynamic.model.query.mq.ModelMany2ManyField(null, "own_partner_id", dynamic.model.query.mq.FieldType.BIGINT, title = "占有人",
            relationModelTable = "public.crm_partner_customer_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.SET_NULL))

    //最新交流人
    val commPartner = dynamic.model.query.mq.ModelMany2OneField(null,
            "comm_partner_id",
            dynamic.model.query.mq.FieldType.BIGINT, title = "最新联系人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.SET_NULL))

    val communications = dynamic.model.query.mq.ModelOne2ManyField(null,
            "communications",
            dynamic.model.query.mq.FieldType.BIGINT,
            "沟通记录",
            targetModelTable = "public.crm_lead_customer_communication_history",
            targetModelFieldName = "customer_id")

    val opportunities = dynamic.model.query.mq.ModelOne2ManyField(null,
            "opportnities",
            dynamic.model.query.mq.FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "customer_id")


    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "名称必填"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空")
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return  arrayOf(
                ModelFieldRequired(this.name,advice = "名称必填"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空")
        )
    }
}