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

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model("crmPartnerLeadRel")
class CrmPartnerLeadRel:ContextModel("crm_partner_lead_rel","public") {
    companion object : RefSingleton<CrmPartnerLeadRel> {
        override lateinit var ref: CrmPartnerLeadRel
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val partner= dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "用户",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id", foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val lead= dynamic.model.query.mq.ModelMany2OneField(null,
            "lead_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))
    val ownFlag = dynamic.model.query.mq.ModelField(null,
            "ownFlag",
            dynamic.model.query.mq.FieldType.INT,
            "占有",
            defaultValue = 0, comment = "0,默认不占有，1占有")
}