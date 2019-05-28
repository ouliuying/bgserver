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

@Model(name="customerContactAddress",title = "客户联系人")
class CustomerContactAddress:ContextModel("customer_contact_address","public"){
    companion object : RefSingleton<CustomerContactAddress> {
        override lateinit var ref: CustomerContactAddress
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null,"name", FieldType.STRING,title = "姓名",defaultValue = "")

    val customer = ModelMany2OneField(null,"customer_id",FieldType.BIGINT,"客户",
            targetModelTable = "public.customer",
            targetModelFieldName = "id",
            foreignKey=FieldForeignKey(action=ForeignKeyAction.CASCADE))
    val mobile = ModelField(null,"mobile",FieldType.STRING,title = "手机",defaultValue = "")
    val telephone = ModelField(null,"telephone",FieldType.STRING,title = "电话",defaultValue = "")
    val email = ModelField(null,"email",FieldType.STRING,title = "email",defaultValue = "")
    val department = ModelField(null,"department",FieldType.STRING,title = "部门",defaultValue = "")
    val province = ModelField(null,"province", FieldType.STRING,title = "省",defaultValue = "")
    val city = ModelField(null,"city", FieldType.STRING,title = "市",defaultValue = "")
    val district = ModelField(null,"district", FieldType.STRING,title = "区/县",defaultValue = "")
    val streetAddress = ModelField(null,"street_address",FieldType.STRING,"详细地址",defaultValue = "")
    val sex = ModelField(null,"sex",FieldType.INT,title = "性别",defaultValue = "")
    val birthday = ModelField(null,"birthday",FieldType.DATE,title = "生日",defaultValue = "")
    val comment = ModelField(null,"c_comment",FieldType.DATE,title = "注释",defaultValue = "")

}