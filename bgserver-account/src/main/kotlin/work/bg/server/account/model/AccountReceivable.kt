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

package work.bg.server.account.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

//应收款
@Model("accountReceivable")
class AccountReceivable:ContextModel("account_receivable","public") {
    companion object: RefSingleton<AccountReceivable> {
        override lateinit var ref: AccountReceivable
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val orderInvoice= dynamic.model.query.mq.ModelOne2OneField(null,
            "order_invoice_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "发票",
            targetModelTable = "public.crm_customer_order_invoice",
            targetModelFieldName = "id")
}