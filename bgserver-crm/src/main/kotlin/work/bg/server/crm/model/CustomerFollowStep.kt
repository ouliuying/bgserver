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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.apache.commons.logging.LogFactory
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.util.TypeConvert

@Model("customerFollowStep","客户跟进阶段")
class CustomerFollowStep:ContextModel("crm_customer_follow_step","public") {
    private  val logger = LogFactory.getLog(javaClass)
    companion object : RefSingleton<CustomerFollowStep> {
        override lateinit var ref: CustomerFollowStep
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name = ModelField(null,
            "name",
            FieldType.STRING,
            title = "名称", defaultValue = "")
    val seqIndex = ModelField(null,
            "seq_index",
            FieldType.INT,
            title = "顺序", defaultValue = 0)

    val customers = ModelMany2ManyField(null,
            "customers",
            FieldType.BIGINT,
            title = "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            relationModelTable = "public.crm_customer_follow_step_customer_rel",
            relationModelFieldName = "customer_id")


    @Action("loadAllStepsViewData")
    fun loadAllStepsViewData(partnerCache: PartnerCache):ActionResult{
        var fields = arrayListOf(this.id,this.name,this.seqIndex)
        val datas = this.rawRead(*fields.toTypedArray(),
                criteria = null,
                partnerCache = partnerCache,
                orderBy = OrderBy(OrderBy.OrderField(this.seqIndex,orderType = OrderBy.Companion.OrderType.DESC)))?.toModelDataObjectArray()
        var ar = ActionResult()
        ar.errorCode = ErrorCode.SUCCESS
        ar.bag["viewData"]= mapOf(
                "fields" to fields,
                "datas" to datas
        )
        return ar
    }
    @Action("addOrEdit")
    fun addOrEdit(@RequestBody data:JsonObject?,
                  partnerCache: PartnerCache):ActionResult{
        var ar  = ActionResult()
        ar.errorCode = ErrorCode.SUCCESS
        var jObj = data?.get("data")?.asJsonObject
        val id = jObj?.get("id")?.asLong
        val name = jObj?.get("name")?.asString
        var obj = ModelDataObject(model = this)
        obj.setFieldValue(this.name,name)
        id?.let {
            obj.setFieldValue(this.id,id)
        }
        if(id!=null){
            val ret = this.rawEdit(obj,
                    useAccessControl = true,
                    partnerCache = partnerCache)
            if(ret.first!=null && ret.first!!>0){
                return ar
            }
            ar.description = ret.second
        }
        else{
            var maxSeq = this.rawMax(this.seqIndex,partnerCache = partnerCache,useAccessControl = true)?:0
            obj.setFieldValue(this.seqIndex,maxSeq+1)
            val ret = this.rawCreate(obj,
                    useAccessControl = true,
                    partnerCache = partnerCache)
            if(ret.first!=null && ret.first!!>0){
                return ar
            }
            ar.description = ret.second
        }
        ar.errorCode = ErrorCode.UNKNOW
        return ar
    }
    @Action("saveFollowStepSeq")
    fun saveFollowStepSeq(@RequestBody data:JsonObject?,
                          partnerCache: PartnerCache):ActionResult{
        var ar = ActionResult()
        var ids = data?.get("ids")?.asJsonArray
        var seqIndex = 1 as Int
        ids?.reversed()?.forEach {
            var mo = ModelDataObject(model = this)
            mo.setFieldValue(this.seqIndex,seqIndex)
            mo.setFieldValue(this.id,it.asLong)
            this.rawEdit(mo)
            seqIndex++
        }
        ar.errorCode = ErrorCode.SUCCESS
        return ar
    }

    @Action("loadFollowStepCustomers")
    fun loadFollowStepCustomers(partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        var fields = arrayListOf(this.id,this.name,this.seqIndex)
        val datas = this.rawRead(*fields.toTypedArray(),
                criteria = null,
                partnerCache = partnerCache,
                orderBy = OrderBy(OrderBy.OrderField(this.seqIndex,orderType = OrderBy.Companion.OrderType.DESC)))?.toModelDataObjectArray()
        datas?.forEach {moIt->
            var stepID = TypeConvert.getLong(moIt.idFieldValue?.value as Number?)
            stepID?.let {
                val customers = CustomerFollowStepCustomerRel.ref.rawRead(
                        criteria = and(eq(CustomerFollowStepCustomerRel.ref.customerFollowStep,stepID),eq(
                        CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID)),
                        orderBy = OrderBy(OrderBy.OrderField(CustomerFollowStepCustomerRel.ref.seqIndex,orderType=OrderBy.Companion.OrderType.DESC)),
                        useAccessControl = true,
                        partnerCache = partnerCache)?.toModelDataObjectArray()
                moIt.setFieldValue(this.customers,customers)
            }
        }
        if(datas!=null){
            ar.bag["steps"] = datas
        }
        else{
            ar.bag["steps"]= arrayOf<ModelDataObject>()
        }
        return ar
    }
//    customerID,
//    rate,
//    followStepID
    @Action("addOrEditCustomerToFollowStep")
    fun addOrEditCustomerToFollowStep(@RequestBody data:JsonObject?,
                                partnerCache: PartnerCache):ActionResult{
        var ar = ActionResult()
        val customerID = data?.get("customerID")?.asLong
        var rate = data?.get("rate")?.asBigDecimal
        val followStepID = data?.get("followStepID")?.asLong
        val id = data?.get("id")?.asLong
        if(customerID==null || rate == null || followStepID==null){
            ar.errorCode=ErrorCode.UNKNOW
            ar.description="参数错误"
            return ar
        }
        if(rate<=0.toBigDecimal()){
            rate = 2.5.toBigDecimal()
        }

        if(id!=null && id>0){
            val count = CustomerFollowStepCustomerRel.ref.rawCount(criteria = and(eq(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID),
                    eq(CustomerFollowStepCustomerRel.ref.customer,customerID),
                    notEq(CustomerFollowStepCustomerRel.ref.id,id)))
            if(count>0){
                ar.errorCode=ErrorCode.UNKNOW
                ar.description="已经加到跟踪列表"
                return ar
            }
            val mo = ModelDataObject(model =CustomerFollowStepCustomerRel.ref)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.id,id)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.customerFollowStep,followStepID)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.customer,customerID)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.rate,rate)
            var ret = CustomerFollowStepCustomerRel.ref.rawEdit(mo,
                    partnerCache = partnerCache,useAccessControl = true)
            if(ret.first!=null && ret.first!!>0){
                ar.errorCode =ErrorCode.SUCCESS
                return ar
            }
            ar.errorCode=ErrorCode.UNKNOW
            ar.description = ret.second
        }
        else{
            val count = CustomerFollowStepCustomerRel.ref.rawCount(criteria =
            and(eq(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID),
                    eq(CustomerFollowStepCustomerRel.ref.customer,customerID))
            )
            if(count>0){
                ar.errorCode=ErrorCode.UNKNOW
                ar.description="已经加到跟踪列表"
                return ar
            }
            val maxSeq = CustomerFollowStepCustomerRel.ref.rawMax(CustomerFollowStepCustomerRel.ref.seqIndex,criteria = eq(CustomerFollowStepCustomerRel.ref.customerFollowStep,followStepID))?:0
            val mo = ModelDataObject(model =CustomerFollowStepCustomerRel.ref)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.customerFollowStep,followStepID)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.customer,customerID)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.rate,rate)
            mo.setFieldValue(CustomerFollowStepCustomerRel.ref.seqIndex,maxSeq+1)
            var ret = CustomerFollowStepCustomerRel.ref.rawCreate(mo,partnerCache = partnerCache,useAccessControl = true)
            if(ret.first!=null && ret.first!!>0){
                ar.errorCode =ErrorCode.SUCCESS
                return ar
            }
            ar.errorCode=ErrorCode.UNKNOW
            ar.description = ret.second
        }

        return ar
    }
    @Action("deleteCustomerFollowStepCustomer")
    fun deleteCustomerFollowStepCustomer(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        var id = data?.get("id")?.asLong
        id?.let {
           var ret =  CustomerFollowStepCustomerRel.ref.rawDelete(criteria = eq(CustomerFollowStepCustomerRel.ref.id,id),
                   useAccessControl = true,
                   partnerCache = partnerCache)
            if(ret.first!=null && ret.first!!>0){
                ar.errorCode = ErrorCode.SUCCESS
                return ar
            }
            ar.description = ret.second?:"删除失败"
            ar.errorCode = ErrorCode.UNKNOW
        }
        return ar
    }
    @Action("switchFollowStepCustomerSeq")
    fun switchFollowStepCustomerSeq(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar = ActionResult()
        //this.logger.info(data?.toString())
        val stepCustomerID = data?.get("stepCustomerID")?.asLong
        val fromStepObj = data?.get("fromStep")?.asJsonObject
        val toStepObj = data?.get("toStep")?.asJsonObject

        val fromStepIndex= fromStepObj?.get("index")?.asInt
        val toStepIndex = toStepObj?.get("index")?.asInt

        val fromStepID = fromStepObj?.get("stepID")?.asLong
        val toStepID = toStepObj?.get("stepID")?.asLong
        if(fromStepID!=null && toStepID!=null && fromStepIndex!=null && toStepIndex!=null && stepCustomerID!=null){
            if(fromStepID==toStepID){
               if(fromStepIndex!=toStepIndex){
                   var datas =  CustomerFollowStepCustomerRel.ref.rawRead(CustomerFollowStepCustomerRel.ref.id,
                           criteria = and(eq(CustomerFollowStepCustomerRel.ref.customerFollowStep,fromStepID),
                            eq(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID)),
                            orderBy = OrderBy(OrderBy.OrderField(CustomerFollowStepCustomerRel.ref.seqIndex,OrderBy.Companion.OrderType.DESC)))?.toModelDataObjectArray()
                    var ids = arrayListOf<Long>()
                   datas?.forEach {
                       var id = TypeConvert.getLong(it.idFieldValue?.value as Number?)
                       id?.let {
                           ids.add(id!!)
                       }
                   }
                   ids.remove(stepCustomerID)
                   ids.add(toStepIndex,stepCustomerID)
                   ids.reversed().forEachIndexed { index, l ->
                       var mo = ModelDataObject(model = CustomerFollowStepCustomerRel.ref)
                       mo.setFieldValue(CustomerFollowStepCustomerRel.ref.id,l)
                       mo.setFieldValue(CustomerFollowStepCustomerRel.ref.seqIndex,index+1)
                       CustomerFollowStepCustomerRel.ref.rawEdit(mo,partnerCache = partnerCache,useAccessControl = true)
                   }
               }
            }
            else{
//                var stepCustomerObj = CustomerFollowStepCustomerRel.ref.rawRead(criteria = and(eq(CustomerFollowStepCustomerRel.ref.id,stepCustomerID),
//                        eq(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID)))?.firstOrNull()
//                if(stepCustomerObj!=null){
//                    return ar
//                }
//                CustomerFollowStepCustomerRel.ref.rawDelete(criteria = and(eq(CustomerFollowStepCustomerRel.ref.id,stepCustomerID),
//                        eq(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID)))
                var datas =  CustomerFollowStepCustomerRel.ref.rawRead(CustomerFollowStepCustomerRel.ref.id,
                        criteria = and(eq(CustomerFollowStepCustomerRel.ref.customerFollowStep,toStepID),
                                eq(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerCache.partnerID)),
                        orderBy = OrderBy(OrderBy.OrderField(CustomerFollowStepCustomerRel.ref.seqIndex,OrderBy.Companion.OrderType.DESC)))?.toModelDataObjectArray()
                var ids = arrayListOf<Long>()
                datas?.forEach {
                    var id = TypeConvert.getLong(it.idFieldValue?.value as Number?)
                    id?.let {
                        ids.add(id!!)
                    }
                }
                ids.add(toStepIndex,stepCustomerID)
                ids.reversed().forEachIndexed { index, l ->
                    var mo = ModelDataObject(model = CustomerFollowStepCustomerRel.ref)
                    if(l!=stepCustomerID){
                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.id,l)
                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.seqIndex,index+1)
                        CustomerFollowStepCustomerRel.ref.rawEdit(mo,partnerCache = partnerCache,useAccessControl = true)
                    }
                    else{
                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.id,l)
                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.seqIndex,index+1)
                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.customerFollowStep,toStepID)
//                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.rate,stepCustomerObj?.getFieldValue(CustomerFollowStepCustomerRel.ref.rate))
//                        mo.setFieldValue(CustomerFollowStepCustomerRel.ref.rate,stepCustomerObj?.getFieldValue(CustomerFollowStepCustomerRel.ref.rate))
                        CustomerFollowStepCustomerRel.ref.rawEdit(mo,partnerCache = partnerCache,useAccessControl = true)
                    }
                }
            }
        }
        return ar
    }
}