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

package work.bg.server.corp.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import work.bg.server.core.spring.boot.annotation.Model

@Model("departmentPartnerRel", "部门")
class DepartmentPartnerRel(table:String,schema:String): ContextModel(table,schema) {
    constructor():this("department_partner_rel","public")
    companion object : RefSingleton<DepartmentPartnerRel> {
        override lateinit var ref: DepartmentPartnerRel
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val department= ModelMany2OneField(null,
            "department_id",
             FieldType.BIGINT,
            "公司",
            "public.corp_department",
            "id",
            foreignKey= FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val partner= ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "用户",
            "public.base_partner",
            "id",
            defaultValue = CurrPartnerBillboard(),
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldUnique(partner,advice = "用戶只能加入一个部门",isolationType = ModelFieldUnique.IsolationType.IN_CORP))
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldNotNullOrEmpty(department,advice = "部门必须选择"))
    }
}