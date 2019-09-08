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
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.crm.field.ModelFullAddressField

@Model(name="lead",title = "线索")
class Lead:ContextModel("crm_lead","public") {
    companion object : RefSingleton<Lead> {
        override lateinit var ref: Lead
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val name = dynamic.model.query.mq.ModelField(null, "name", dynamic.model.query.mq.FieldType.STRING, title = "姓名", defaultValue = "")
    val corpName = dynamic.model.query.mq.ModelField(null, "corp_name", dynamic.model.query.mq.FieldType.STRING, title = "公司名称", defaultValue = "")
    val mobile = dynamic.model.query.mq.ModelField(null, "mobile", dynamic.model.query.mq.FieldType.STRING, title = "手机", defaultValue = "")
    val telephone = dynamic.model.query.mq.ModelField(null, "telephone", dynamic.model.query.mq.FieldType.STRING, title = "电话", defaultValue = "")
    val fax = dynamic.model.query.mq.ModelField(null, "fax", dynamic.model.query.mq.FieldType.STRING, title = "传真", defaultValue = "")
    val province = dynamic.model.query.mq.ModelField(null, "province", dynamic.model.query.mq.FieldType.STRING, title = "省", defaultValue = "")
    val city = dynamic.model.query.mq.ModelField(null, "city", dynamic.model.query.mq.FieldType.STRING, title = "市", defaultValue = "")
    val district = dynamic.model.query.mq.ModelField(null, "district", dynamic.model.query.mq.FieldType.STRING, title = "区/县", defaultValue = "")
    val streetAddress = dynamic.model.query.mq.ModelField(null, "street_address", dynamic.model.query.mq.FieldType.STRING, "详细地址", defaultValue = "")

    val partners = dynamic.model.query.mq.ModelMany2ManyField(null,
            "own_partner_id",
            dynamic.model.query.mq.FieldType.BIGINT, title = "占有人",
            relationModelTable = "public.crm_partner_lead_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.SET_NULL))

    //最新交流人
    val commPartner = dynamic.model.query.mq.ModelMany2OneField(null,
            "comm_partner_id",
            dynamic.model.query.mq.FieldType.BIGINT, title = "联系人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.SET_NULL))

    val event = dynamic.model.query.mq.ModelMany2OneField(null,
            "event_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "活动",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id")

    val communications = dynamic.model.query.mq.ModelOne2ManyField(null,
            "communications",
            dynamic.model.query.mq.FieldType.BIGINT,
            "沟通记录",
            targetModelTable = "public.crm_lead_customer_communication_history",
            targetModelFieldName = "lead_id")

    val interactionStatus= dynamic.model.query.mq.ModelMany2OneField(null,
            "interaction_status_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "最近状态",
            targetModelTable = "public.crm_lead_interaction_status",
            targetModelFieldName = "id")

    val fullAddress by lazy {
            ModelFullAddressField(null,
                    "fullAddress",
                    "地址",
                    this.province,this.city,
                    this.district,
                    this.streetAddress,
                    this.gson)
    }

}