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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.query.mq.aggregation.SumExpression
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartner
import work.bg.server.core.model.ContextModel
import work.bg.server.crm.model.CustomerOrderInvoice
import work.bg.server.util.Time
import java.math.BigDecimal
import java.sql.ResultSet

@Model("accountApi")
class AccountApi:ContextModel("account_api","public") {
    companion object: RefSingleton<AccountApi> {
        override lateinit var ref: AccountApi
    }

    override fun isDynamic(): Boolean {
        return true
    }

    @Action("loadTopListData")
    fun loadTopListData(@RequestBody data: JsonObject?,
                        partnerCache: PartnerCache):Any?{
        var ar=ActionResult()
        val model  = CustomerOrderInvoice.ref
        var startDate = data?.get("startDate")?.asString
        var endDate = data?.get("endDate")?.asString
        val fromDate = Time.getDate(startDate)
        val toDate = Time.getDate(endDate)

        var criteria = when{
            fromDate!=null &&  toDate!=null ->{
                and(gtEq(model.createTime,fromDate), ltEq(model.createTime,toDate))
            }
            fromDate!=null-> ltEq(model.createTime,toDate)
            toDate!=null -> gtEq(model.createTime,fromDate)
            else-> null
        }

        criteria = when{
                criteria!=null -> and(criteria,eq(AccountCustomerOrderInvoice.ref.status,1))
                else -> eq(AccountCustomerOrderInvoice.ref.status,1)
        }

        //TODO ADD sum field and as keyword
        val select  = select(model.createPartnerID,fromModel = model).where(criteria).sum(SumExpression(model.amount)).groupBy(GroupBy(model.createPartnerID))
        var topList = arrayListOf<Pair<Long,BigDecimal?>>()
        this.query(select, RowCallbackHandler {
            topList.add(Pair(it.getLong(2),it.getBigDecimal(1)))
        })
        topList.sortBy {
            it.second
        }
        topList.reverse()
        var arr =JsonArray()
        var starCount = 5
        topList.forEach {topIt->
            val partner = BasePartner.ref.rawRead(BasePartner.ref.name,BasePartner.ref.userIcon,criteria = eq(BasePartner.ref.id,topIt.first))?.firstOrNull()
            partner?.let {
                var jo = JsonObject()
                jo.addProperty("totalAmount",topIt.second)
                jo.addProperty("name",partner.getFieldValue(BasePartner.ref.name) as String?)
                jo.addProperty("icon",partner.getFieldValue(BasePartner.ref.userIcon) as String?)
                jo.addProperty("starCount",starCount)
                starCount--
                arr.add(jo)
            }
        }
        ar.bag["topList"] = arr
        return ar
    }
}