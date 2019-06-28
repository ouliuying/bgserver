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

package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
//商机
@Model(name="customerOpportunity",title = "商机")
class CustomerOpportunity:ContextModel("crm_customer_opportunity","public") {
    companion object : RefSingleton<CustomerOpportunity> {
        override lateinit var ref: CustomerOpportunity
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val title=ModelField(null,
            "title",
            FieldType.STRING,
            "标题")

    val price = ModelField(null,
            "price",
            FieldType.NUMBER,
            "金额")

    val customer=ModelMany2OneField(null,
            "customer_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val products = ModelMany2ManyField(null,
            "products",
            FieldType.BIGINT,
            "产品",
            relationModelTable = "public.crm_customer_opportunity_order_product_rel",
            relationModelFieldName = "product_id",
            targetModelTable = "public.product_product",
            targetModelFieldName = "id")

    val quotation = ModelOne2OneField(null,
            "quotation",
            FieldType.BIGINT,
            "报价单",
            isVirtualField = true,
            targetModelTable = "public.crm_customer_opportunity_order_quotation",
            targetModelFieldName = "opportunity_id"
            )

    val order = ModelOne2OneField(null,
            "opportunity",
            FieldType.BIGINT,
            "订单",
            isVirtualField = true,
            targetModelTable = "public.crm_customer_order",
            targetModelFieldName = "opportunity_id"
            )

}



