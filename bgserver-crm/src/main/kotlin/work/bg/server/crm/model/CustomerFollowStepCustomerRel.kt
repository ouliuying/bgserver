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

import dynamic.model.query.mq.*
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.model.ContextModel

@Model("customerFollowStepCustomerRel")
class CustomerFollowStepCustomerRel:ContextModel("crm_customer_follow_step_customer_rel",
        "public") {
    companion object : RefSingleton<CustomerFollowStepCustomerRel> {
        override lateinit var ref: CustomerFollowStepCustomerRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val customer = ModelMany2OneField(null,
            "customer_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id")

    val customerFollowStep = ModelMany2OneField(null,
            "customer_follow_step_id",
            FieldType.BIGINT,
            "客户跟进阶段",
            targetModelTable = "public.crm_customer_follow_step",
            targetModelFieldName = "id")

    val seqIndex = ModelField(null,
            "seq_index",
            FieldType.INT,
            "顺序",
            defaultValue = 0)

    val rate = ModelField(null,"rate",FieldType.DECIMAL,"满意度",defaultValue = 2.5)

}