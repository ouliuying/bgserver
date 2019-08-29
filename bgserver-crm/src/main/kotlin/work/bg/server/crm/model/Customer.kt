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

import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.crm.field.ModelFullAddressField

@Model(name="customer",title = "客户")
class Customer: ContextModel("crm_customer","public") {
    companion object : RefSingleton<Customer> {
        override lateinit var ref: Customer
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name = ModelField(null,"name",FieldType.STRING,title = "姓名",defaultValue = "")

    val isCorp = ModelField(null,"is_corp",FieldType.INT,title = "公司",defaultValue = -1)

    val event = ModelMany2OneField(null,
            "event_id",FieldType.BIGINT,
            title = "活动",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    //个人属性
    val mobile =  ModelField(null,"mobile",FieldType.STRING,title = "手机",defaultValue = "")
    val sex =  ModelField(null,"sex",FieldType.INT,title = "性别",defaultValue = -1)
    //公司属性
    val fax =  ModelField(null,"fax",FieldType.STRING,title = "传真",defaultValue = "")
    //通用属性
    val telephone =  ModelField(null,"telephone",FieldType.STRING,title = "电话",defaultValue = "")
    val email =  ModelField(null,"email",FieldType.STRING,title = "email",defaultValue = "")
    val province = ModelField(null,"province", FieldType.STRING,title = "省",defaultValue = "")
    val city = ModelField(null,"city", FieldType.STRING,title = "市",defaultValue = "")
    val district = ModelField(null,"district", FieldType.STRING,title = "区/县",defaultValue = "")
    val streetAddress = ModelField(null,"street_address",FieldType.STRING,"详细地址",defaultValue = "")
    val fullAddress by lazy {
        ModelFullAddressField(null,
                "fullAddress",
                "地址",
                this.province,this.city,
                this.district,
                this.streetAddress,
                this.gson)
    }
    val website =  ModelField(null,"website",FieldType.STRING,title = "网址",defaultValue = "")
    val comment =  ModelField(null,"c_comment",FieldType.STRING,title = "注释",defaultValue = "")
    val contactAddresses = ModelOne2ManyField(null,"contact_address_id",FieldType.BIGINT,"联系人",
            targetModelTable = "public.customer_contact_address",targetModelFieldName = "customer_id")

    //对应占有,辅助员工
    val partners = ModelMany2ManyField(null,"own_partner_id",FieldType.BIGINT,title = "占有人",
            relationModelTable = "public.crm_partner_customer_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    //最新交流人
    val commPartner = ModelMany2OneField(null,
            "comm_partner_id",
            FieldType.BIGINT,title = "最新联系人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val communications = ModelOne2ManyField(null,
            "communications",
            FieldType.BIGINT,
            "沟通记录",
            targetModelTable = "public.crm_lead_customer_communication_history",
            targetModelFieldName = "customer_id")

    val opportunities = ModelOne2ManyField(null,
            "opportnities",
            FieldType.BIGINT,
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