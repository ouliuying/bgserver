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

@Model(name="customerContactAddress",title = "客户联系人")
class CustomerContactAddress:ContextModel("customer_contact_address","public"){
    companion object : RefSingleton<CustomerContactAddress> {
        override lateinit var ref: CustomerContactAddress
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val name = dynamic.model.query.mq.ModelField(null, "name", dynamic.model.query.mq.FieldType.STRING, title = "姓名", defaultValue = "")

    val customer = dynamic.model.query.mq.ModelMany2OneField(null, "customer_id", dynamic.model.query.mq.FieldType.BIGINT, "客户",
            targetModelTable = "public.customer",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))
    val mobile = dynamic.model.query.mq.ModelField(null, "mobile", dynamic.model.query.mq.FieldType.STRING, title = "手机", defaultValue = "")
    val telephone = dynamic.model.query.mq.ModelField(null, "telephone", dynamic.model.query.mq.FieldType.STRING, title = "电话", defaultValue = "")
    val email = dynamic.model.query.mq.ModelField(null, "email", dynamic.model.query.mq.FieldType.STRING, title = "email", defaultValue = "")
    val department = dynamic.model.query.mq.ModelField(null, "department", dynamic.model.query.mq.FieldType.STRING, title = "部门", defaultValue = "")
    val province = dynamic.model.query.mq.ModelField(null, "province", dynamic.model.query.mq.FieldType.STRING, title = "省", defaultValue = "")
    val city = dynamic.model.query.mq.ModelField(null, "city", dynamic.model.query.mq.FieldType.STRING, title = "市", defaultValue = "")
    val district = dynamic.model.query.mq.ModelField(null, "district", dynamic.model.query.mq.FieldType.STRING, title = "区/县", defaultValue = "")
    val streetAddress = dynamic.model.query.mq.ModelField(null, "street_address", dynamic.model.query.mq.FieldType.STRING, "详细地址", defaultValue = "")
    val sex = dynamic.model.query.mq.ModelField(null, "sex", dynamic.model.query.mq.FieldType.INT, title = "性别", defaultValue = "")
    val birthday = dynamic.model.query.mq.ModelField(null, "birthday", dynamic.model.query.mq.FieldType.DATE, title = "生日", defaultValue = "")
    val comment = dynamic.model.query.mq.ModelField(null, "c_comment", dynamic.model.query.mq.FieldType.DATE, title = "注释", defaultValue = "")

}