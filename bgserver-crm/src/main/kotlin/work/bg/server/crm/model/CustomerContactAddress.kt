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

import dynamic.model.query.mq.*
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.model.field.EventLogField

@Model(name="customerContactAddress",title = "客户联系人")
class CustomerContactAddress:ContextModel("crm_customer_contact_address","public"){
    companion object : RefSingleton<CustomerContactAddress> {
        override lateinit var ref: CustomerContactAddress
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null, "name", FieldType.STRING, title = "姓名", defaultValue = "")

    val customer = ModelMany2OneField(null, "customer_id", FieldType.BIGINT, "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))
    val mobile = ModelField(null, "mobile", FieldType.STRING, title = "手机", defaultValue = "")
    val telephone = ModelField(null, "telephone", FieldType.STRING, title = "电话", defaultValue = "")
    val email = ModelField(null, "email", FieldType.STRING, title = "email", defaultValue = "")
    val department = ModelField(null, "department", FieldType.STRING, title = "部门", defaultValue = "")
    val province = ModelField(null, "province", FieldType.STRING, title = "省", defaultValue = "")
    val city = ModelField(null, "city", FieldType.STRING, title = "市", defaultValue = "")
    val district = ModelField(null, "district", FieldType.STRING, title = "区/县", defaultValue = "")
    val streetAddress = ModelField(null, "street_address", FieldType.STRING, "详细地址", defaultValue = "")
    val sex = ModelField(null, "sex", FieldType.INT, title = "性别", defaultValue = "")
    val birthday = ModelField(null, "birthday", FieldType.DATE, title = "生日")
    val comment = ModelField(null, "c_comment", FieldType.STRING, title = "注释", defaultValue = "")
    val eventLogs = EventLogField(null,"event_logs","跟踪日志")
}