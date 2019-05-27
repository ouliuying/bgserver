/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  * GNU Lesser General Public License Usage
 *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  * General Public License version 3 as published by the Free Software
 *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  * project of this file. Please review the following information to
 *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *
 *
 */

package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

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

    val partners = ModelMany2ManyField(null,"own_partner_id",FieldType.BIGINT,title = "占有人",
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
            targetModelFieldName = "lead")

    val interactionStatus=ModelMany2OneField(null,
            "interaction_status_id",
            FieldType.BIGINT,
            "更近状态",
            targetModelTable = "public.crm_lead_interaction_status",
            targetModelFieldName = "id")
}