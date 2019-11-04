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

import dynamic.model.query.mq.FieldType
import dynamic.model.query.mq.ModelOne2ManyField
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.BaseCorp
import dynamic.model.web.spring.boot.annotation.Model

@Model("corp", "公司")
class DepartmentCorp :BaseCorp(){
    companion object : RefSingleton<DepartmentCorp> {
        override lateinit var ref: DepartmentCorp
    }

    val departments = ModelOne2ManyField(null,
            "m_corp_id", FieldType.BIGINT,
            "部门",
            targetModelTable = "public.department",
            targetModelFieldName = "corp_id")
}