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

import dynamic.model.query.mq.FieldForeignKey
import dynamic.model.query.mq.ForeignKeyAction
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model(name="customerOpportunityOrderQuotation",title = "报价单")
class CustomerOpportunityOrderQuotation:
        ContextModel("crm_customer_opportunity_order_quotation","public") {
    companion object : RefSingleton<CustomerOpportunityOrderQuotation> {
        override lateinit var ref: CustomerOpportunityOrderQuotation
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())


    val opportunity= dynamic.model.query.mq.ModelOne2OneField(null,
            "opportunity_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "商机",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id"
    )

    val order= dynamic.model.query.mq.ModelOne2OneField(null,
            "order_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "订单",
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "id"
    )

    var tpl = dynamic.model.query.mq.ModelMany2OneField(null,
            "tpl_id", dynamic.model.query.mq.FieldType.BIGINT,
            "模板",
            targetModelTable = "public.crm_customer_opportunity_order_quotation_template",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    val comment= dynamic.model.query.mq.ModelField(null,
            "comment",
            dynamic.model.query.mq.FieldType.TEXT,
            "注释")
}