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
import work.bg.server.product.model.Product

@Model("product")
class CrmProduct:Product(){
    companion object : RefSingleton<CrmProduct> {
        override lateinit var ref: CrmProduct
    }

    val customerOpportunities = dynamic.model.query.mq.ModelMany2ManyField(null,
            "customer_opportunities",
            dynamic.model.query.mq.FieldType.BIGINT,
            "商机",
            relationModelTable = "crm_customer_opportunity_product_rel",
            relationModelFieldName = "customer_opportunity_id",
            targetModelTable = "public.crm_customer_opportunity",
            targetModelFieldName = "id")

}