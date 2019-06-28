/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
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