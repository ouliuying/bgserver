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

    val type = ModelField(null,"c_type",FieldType.INT,title = "类型",defaultValue = 0)

    val status = ModelMany2OneField(null,
            "c_status",
            FieldType.INT,title = "状态",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val event = ModelMany2OneField(null,
            "event_id",FieldType.BIGINT,
            title = "活动",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    //个人属性
    val mobile =  ModelField(null,"mobile",FieldType.STRING,title = "手机",defaultValue = "")
    //公司属性
    val fax =  ModelField(null,"fax",FieldType.STRING,title = "传真",defaultValue = "")
    //通用属性
    val telephone =  ModelField(null,"telephone",FieldType.STRING,title = "电话",defaultValue = "")
    val email =  ModelField(null,"email",FieldType.STRING,title = "email",defaultValue = "")
    val address =  ModelField(null,"address",FieldType.STRING,title = "地址",defaultValue = "")
    val website =  ModelField(null,"website",FieldType.STRING,title = "网址",defaultValue = "")
    val comment =  ModelField(null,"c_comment",FieldType.STRING,title = "注释",defaultValue = "")
    val contactAddresses = ModelOne2ManyField(null,"contact_address_id",FieldType.BIGINT,"联系人",
            targetModelTable = "public.customer_contact_address",targetModelFieldName = "customer")

    //对应占有,辅助员工
    val partners = ModelMany2ManyField(null,"own_partner_id",FieldType.BIGINT,title = "占有人",
            relationModelTable = "public.crmPartnerCustomerRel",
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
            targetModelFieldName = "customer")

    val opportunities = ModelOne2ManyField(null,
            "opportnities",
            FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "customers")
}