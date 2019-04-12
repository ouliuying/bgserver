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
import work.bg.server.core.model.BasePartner
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("partner", "员工")
class DepartmentPartner:BasePartner() {
    companion object : RefSingleton<BasePartner> {
        override lateinit var ref: BasePartner
    }
    val departments = ModelMany2ManyField(null,
            "department_id",
            FieldType.BIGINT,
            "部门",
            targetModelTable = "public.corp_department",
            targetModelFieldName = "id",
            relationModelTable = "public.department_partner_rel",
            relationModelFieldName = "department_id")
}