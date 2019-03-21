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
import work.bg.server.core.model.BaseCorp
import work.bg.server.core.mq.FieldForeignKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ForeignKeyAction
import work.bg.server.core.mq.ModelOne2ManyField
import work.bg.server.core.spring.boot.annotation.Model

@Model("departmentCorp", "公司")
class DepartmentCorp:BaseCorp() {
        companion object : RefSingleton<DepartmentCorp> {
                override lateinit var ref: DepartmentCorp
        }
        val departments = ModelOne2ManyField(null,
                "department_corp_id",
                FieldType.BIGINT,
                "部门",
                targetModelTable = "public.corp_department",
                targetModelFieldName = "corp_id",
                foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))
}