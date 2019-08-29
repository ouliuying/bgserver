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

package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField
import work.bg.server.core.mq.ModelOne2ManyField
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="customerOpportunityOrderQuotationTemplate")
class CustomerOpportunityOrderQuotationTemplate:  ContextModel("crm_customer_opportunity_order_quotation_template","public")  {
    companion object : RefSingleton<CustomerOpportunityOrderQuotationTemplate> {
        override lateinit var ref: CustomerOpportunityOrderQuotationTemplate
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name= ModelField(null,
            "name",
            FieldType.STRING,
            "名字")

    val tpl= ModelField(null,
            "tpl",
            FieldType.TEXT,
            "模板")

    val quotations = ModelOne2ManyField(null,"quotations",FieldType.BIGINT,
            "询价单",
            targetModelTable = "public.crm_customer_opportunity_order_quotation",
            targetModelFieldName = "tpl_id")

    val comment= ModelField(null,
            "comment",
            FieldType.TEXT,
            "注释")
}