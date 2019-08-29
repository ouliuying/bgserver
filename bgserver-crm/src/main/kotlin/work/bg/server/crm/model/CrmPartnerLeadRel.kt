/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("crmPartnerLeadRel")
class CrmPartnerLeadRel:ContextModel("crm_partner_lead_rel","public") {
    companion object : RefSingleton<CrmPartnerLeadRel> {
        override lateinit var ref: CrmPartnerLeadRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val partner= ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "用户",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))

    val lead= ModelMany2OneField(null,
            "lead_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))
    val ownFlag = ModelField(null,
            "ownFlag",
            FieldType.INT,
            "占有",
            defaultValue = 0,comment = "0,默认不占有，1占有")
}