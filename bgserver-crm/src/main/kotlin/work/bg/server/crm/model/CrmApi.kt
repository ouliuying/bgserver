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
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.util.Time
import java.util.*

@Model("crmApi")
class CrmApi:ContextModel("crm_api","public") {
    companion object : RefSingleton<CrmApi> {
        override lateinit var ref: CrmApi
    }
    override fun isDynamic(): Boolean {
        return true
    }

    @Action("loadCrmSummaryData")
    fun loadCrmSummaryData(@RequestBody data:JsonObject?,
                           partnerCache: PartnerCache):Any?{
        var ar = ActionResult()
        var startDate = data?.get("startDate")?.asString
        var endDate = data?.get("endDate")?.asString
        val fromDate = Time.getDate(startDate)
        val toDate = Time.getDate(endDate)
        ar.errorCode = ErrorCode.SUCCESS
        ar.bag["data"] = mapOf(
                "eventCount" to this.getEventCount(fromDate, toDate,partnerCache),
                "leadCount" to this.getLeadCount(fromDate, toDate,partnerCache),
                "opportunityCount" to this.getOpportunityCount(fromDate, toDate,partnerCache),
                "customerCount" to this.getCustomerCount(fromDate, toDate,partnerCache),
                "orderCount" to this.getOrderCount(fromDate, toDate,partnerCache)
        )
        return ar
    }
    @Action("loadSalesFunnelData")
    fun loadSalesFunnelData(@RequestBody data:JsonObject?,
                            partnerCache: PartnerCache):Any?{
        var ar  = ActionResult()
        var startDate = data?.get("startDate")?.asString
        var endDate = data?.get("endDate")?.asString
        val fromDate = Time.getDate(startDate)
        val toDate = Time.getDate(endDate)
        //lead
        //customer opportunity
        //客户报价
        //创建客户订单
        //确认订单，
        //合同，开具发票
        //回款 结束
        ar.errorCode =ErrorCode.SUCCESS
        ar.bag["funnelData"]= arrayOf(
//                mapOf(
//                        "step" to "线索",
//                        "count" to this.getLeadCount(fromDate, toDate,partnerCache)
//                ),
                mapOf(
                        "step" to "销售机会",
                        "count" to this.getOpportunityCount(fromDate, toDate,partnerCache)
                )
                ,
                mapOf(
                        "step" to "形成订单/合同/发票",
                        "count" to this.getOrderCount(fromDate, toDate,partnerCache)
                )
                ,
                mapOf(
                        "step" to "回款成功",
                        "count" to this.getOrderInvoiceCount(fromDate, toDate,1,partnerCache)
                )
        )
        return ar
    }
    private fun getOrderInvoiceCount(fromDate: Date?, toDate:Date?,status:Int,partnerCache: PartnerCache):Int{
        val model = CustomerOrderInvoice.ref
        val statusField = model.fields.getField("status")
        var criteria = when{
            fromDate!=null &&  toDate!=null ->{
                and(gtEq(model.createTime,fromDate), ltEq(model.createTime,toDate))
            }
            fromDate!=null-> ltEq(model.createTime,toDate)
            toDate!=null -> gtEq(model.createTime,fromDate)
            else-> null
        }
        statusField?.let {
            criteria = when{
                criteria!=null -> and(criteria!!,eq(it,status))
                else -> eq(it,status)
            }
        }
        val ret=  model.rawCount(criteria = criteria)
        return if(ret>0) ret else 1
    }
    private fun getEventCount(fromDate: Date?, toDate:Date?,partnerCache: PartnerCache):Int{
        return this.getModelDataCount(Event.ref,fromDate, toDate,partnerCache)
    }
    private  fun getLeadCount(fromDate: Date?,toDate: Date?,partnerCache: PartnerCache):Int{
        return this.getModelDataCount(Lead.ref,fromDate, toDate,partnerCache)
    }
    private  fun getOpportunityCount(fromDate: Date?,toDate: Date?,partnerCache: PartnerCache):Int{
        return this.getModelDataCount(CustomerOpportunity.ref,fromDate, toDate,partnerCache)
    }
    private  fun getCustomerCount(fromDate: Date?,toDate: Date?,partnerCache: PartnerCache):Int{
        return this.getModelDataCount(Customer.ref,fromDate, toDate,partnerCache)
    }
    private  fun getOrderCount(fromDate: Date?,toDate: Date?,partnerCache: PartnerCache):Int{
        return this.getModelDataCount(CustomerOrder.ref,fromDate, toDate,partnerCache)
    }
    private  fun getModelDataCount(model:ContextModel,fromDate: Date?,toDate: Date?,partnerCache: PartnerCache):Int{
         var criteria = when{
             fromDate!=null &&  toDate!=null ->{
                  and(gtEq(model.createTime,fromDate), ltEq(model.createTime,toDate))
             }
             fromDate!=null-> ltEq(model.createTime,toDate)
             toDate!=null -> gtEq(model.createTime,fromDate)
             else-> null
         }
         val ret =   model.rawCount(criteria = criteria,partnerCache = partnerCache,useAccessControl = true)
        return if(ret>0) ret else 1
    }
    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}