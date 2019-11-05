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

import dynamic.model.query.mq.*
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.model.field.EventLogField

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

    val title= ModelField(null,
            "title",
            FieldType.STRING,
            "标题")

    val price = ModelField(null,
            "price",
            FieldType.NUMBER,
            "金额")

    val customer= ModelMany2OneField(null,
            "customer_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

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
    val eventLogs = EventLogField(null,"event_logs","跟踪日志")
}



