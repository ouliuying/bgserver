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