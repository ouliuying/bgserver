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

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model(name="customerOpportunityOrderQuotationTemplate")
class CustomerOpportunityOrderQuotationTemplate:  ContextModel("crm_customer_opportunity_order_quotation_template","public")  {
    companion object : RefSingleton<CustomerOpportunityOrderQuotationTemplate> {
        override lateinit var ref: CustomerOpportunityOrderQuotationTemplate
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val name= dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            "名字")

    val tpl= dynamic.model.query.mq.ModelField(null,
            "tpl",
            dynamic.model.query.mq.FieldType.TEXT,
            "模板")

    val quotations = dynamic.model.query.mq.ModelOne2ManyField(null, "quotations", dynamic.model.query.mq.FieldType.BIGINT,
            "询价单",
            targetModelTable = "public.crm_customer_opportunity_order_quotation",
            targetModelFieldName = "tpl_id")

    val comment= dynamic.model.query.mq.ModelField(null,
            "comment",
            dynamic.model.query.mq.FieldType.TEXT,
            "注释")
}