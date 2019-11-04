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
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.corp.model.DepartmentPartner

@Model("partner")
class CrmPartner:DepartmentPartner() {
    companion object : RefSingleton<CrmPartner> {
        override lateinit var ref: CrmPartner
    }

    val events = dynamic.model.query.mq.ModelMany2ManyField(null,
            "crm_event_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "促销活动",
            relationModelTable = "public.crm_partner_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id")

    val  customers = dynamic.model.query.mq.ModelMany2ManyField(null,
            "crm_customer_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "我的客户",
            relationModelTable = "public.crm_partner_customer_rel",
            relationModelFieldName = "customer_id",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id")

    val  leads = dynamic.model.query.mq.ModelMany2ManyField(null,
            "crm_lead_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "我的线索",
            relationModelTable = "public.crm_partner_lead_rel",
            relationModelFieldName = "lead_id",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "id")
    val orderInvoices = dynamic.model.query.mq.ModelOne2ManyField(null,
            "invoice",
            dynamic.model.query.mq.FieldType.BIGINT,
            "发票",
            targetModelTable = "public.crm_customer_order_invoice",
            targetModelFieldName = "accountPartner")

}