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

import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.model.billboard.CurrCorpBillboard
import work.bg.server.core.spring.boot.annotation.Model

@Model("department", "部门")
class Department:ContextModel("corp_department",
        "public") {
    companion object : RefSingleton<Department> {
        override lateinit var ref: Department
    }
    val id=ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name=ModelField(null,
            "name",
            FieldType.STRING,
            "名称")
    val comment = ModelField(null,
            "comment",
            FieldType.TEXT,
            "注释")
    val corp = ModelMany2OneField(null,
            "corp_id",
            FieldType.BIGINT,
            null,
            targetModelTable = "public.base_corp",
            targetModelFieldName = "id",
            defaultValue = CurrCorpBillboard(),
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))
    val partners=ModelMany2ManyField(null,
            "partner_id",
            FieldType.BIGINT,"员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            relationModelTable = "public.department_partner_rel",
            relationModelFieldName = "partner_id")

    val parent=ModelMany2OneField(null,"parent_id",
            FieldType.BIGINT,
            "上级部门",
            targetModelTable = "public.corp_department",
            targetModelFieldName = "id",
            defaultValue = null,
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val children = ModelOne2ManyField(null,
            "m_parent_id",
            FieldType.BIGINT,
            title = "下级部门",
            targetModelTable = "public.corp_department",
            targetModelFieldName = "parent_id")


    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.parent,this.name,advice = "同级部门不能重名",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.parent,this.name,advice = "同级部门不能重名",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "必须输入部门名称"),
                ModelFieldNotNullOrEmpty(this.name,advice = "部门名称不能为空")
        )
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "必须输入部门名称"),
                ModelFieldNotNullOrEmpty(this.name,advice = "部门名称不能为空")
        )
    }
}