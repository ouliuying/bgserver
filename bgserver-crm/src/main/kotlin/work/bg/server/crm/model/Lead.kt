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
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.crm.field.ModelFullAddressField

@Model(name="lead",title = "线索")
class Lead:ContextModel("crm_lead","public") {
    companion object : RefSingleton<Lead> {
        override lateinit var ref: Lead
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null,"name", FieldType.STRING,title = "姓名",defaultValue = "")
    val corpName = ModelField(null,"corp_name", FieldType.STRING,title = "公司名称",defaultValue = "")
    val mobile = ModelField(null,"mobile", FieldType.STRING,title = "手机",defaultValue = "")
    val telephone = ModelField(null,"telephone", FieldType.STRING,title = "电话",defaultValue = "")
    val fax = ModelField(null,"fax", FieldType.STRING,title = "传真",defaultValue = "")
    val province = ModelField(null,"province", FieldType.STRING,title = "省",defaultValue = "")
    val city = ModelField(null,"city", FieldType.STRING,title = "市",defaultValue = "")
    val district = ModelField(null,"district", FieldType.STRING,title = "区/县",defaultValue = "")
    val streetAddress = ModelField(null,"street_address",FieldType.STRING,"详细地址",defaultValue = "")

    val partners = ModelMany2ManyField(null,
            "own_partner_id",
            FieldType.BIGINT,title = "占有人",
            relationModelTable = "public.crm_partner_lead_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    //最新交流人
    val commPartner = ModelMany2OneField(null,
            "comm_partner_id",
            FieldType.BIGINT,title = "联系人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val event = ModelMany2OneField(null,
            "event_id",
            FieldType.BIGINT,
            "活动",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id")

    val communications = ModelOne2ManyField(null,
            "communications",
            FieldType.BIGINT,
            "沟通记录",
            targetModelTable = "public.crm_lead_customer_communication_history",
            targetModelFieldName = "lead_id")

    val interactionStatus=ModelMany2OneField(null,
            "interaction_status_id",
            FieldType.BIGINT,
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