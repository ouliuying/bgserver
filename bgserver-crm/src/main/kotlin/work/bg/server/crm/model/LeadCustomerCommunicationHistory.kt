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
import java.lang.reflect.Field

@Model(name="leadCustomerCommunicationHistory",title = "沟通记录")
class LeadCustomerCommunicationHistory:ContextModel("crm_lead_customer_communication_history","public") {
    companion object : RefSingleton<LeadCustomerCommunicationHistory> {
        override lateinit var ref: LeadCustomerCommunicationHistory
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val content = ModelField(null,"content",FieldType.TEXT,
            "沟通细节",comment = "json format,client parse and show")

    val partner = ModelMany2OneField(null,"partner_id",FieldType.BIGINT,"员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val lead = ModelMany2OneField(null,"lead_id",FieldType.BIGINT,"线索",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val customer = ModelMany2OneField(null,"customer_id",FieldType.BIGINT,"客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))
}