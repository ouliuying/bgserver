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

package work.bg.server.corp.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import dynamic.model.web.spring.boot.annotation.Model

@Model("departmentPartnerRel", "部门")
class DepartmentPartnerRel(table:String,schema:String): ContextModel(table,schema) {
    constructor():this("department_partner_rel","public")
    companion object : RefSingleton<DepartmentPartnerRel> {
        override lateinit var ref: DepartmentPartnerRel
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val department= dynamic.model.query.mq.ModelMany2OneField(null,
            "department_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "公司",
            "public.corp_department",
            "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val partner= dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "用户",
            "public.base_partner",
            "id",
            defaultValue = CurrPartnerBillboard(),
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldUnique(partner,advice = "用戶只能加入一个部门",isolationType = ModelFieldUnique.IsolationType.IN_CORP))
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldUnique(partner,advice = "用戶只能加入一个部门",isolationType = ModelFieldUnique.IsolationType.IN_CORP))
    }
    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldNotNullOrEmpty(department,advice = "部门必须选择"))
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldNotNullOrEmpty(department,advice = "部门必须选择"))
    }
}