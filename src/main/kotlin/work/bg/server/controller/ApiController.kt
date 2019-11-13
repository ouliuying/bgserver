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

package work.bg.server.controller

import com.google.gson.JsonObject
import com.rometools.rome.feed.rss.Guid
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.model.AppModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import work.bg.server.chat.model.ChatChannel
import work.bg.server.chat.model.ChatPartner
import work.bg.server.core.model.*
import work.bg.server.crm.model.Customer
import work.bg.server.crm.model.CustomerFollowStep
import work.bg.server.crm.model.CustomerFollowStepCustomerRel
import work.bg.server.util.GUID
import work.bg.server.util.MD5
import java.util.*

@RestController
class ApiController {
        @Autowired
        protected lateinit var txManager: PlatformTransactionManager
        @RequestMapping("/xyApi/doReg")
        fun doRegSuperPartner(@RequestBody data:JsonObject?):Any{
            var retObj = JsonObject()
            val userName = data?.get("userName")?.asString
            var password = data?.get("password")?.asString
            val confirmPassword = data?.get("confirmPassword")?.asString
            val name = data?.get("name")?.asString
            val corpName = data?.get("corpName")?.asString

            if(userName.isNullOrEmpty() || userName.isNullOrBlank() || password.isNullOrBlank() || password.isNullOrEmpty()){
                retObj.addProperty("errorCode",1)
                retObj.addProperty("message","用户名货密码为空！")
                return retObj
            }

            if(confirmPassword != password){
                retObj.addProperty("errorCode",2)
                retObj.addProperty("message","两次输入的密码不一致！")
                return retObj
            }
            var count = BasePartner.ref.rawCount(criteria = eq(BasePartner.ref.userName,userName))
            if(count>0){
                retObj.addProperty("errorCode",5)
                retObj.addProperty("message","用户名已经存在！")
                return retObj
            }
            password = MD5.hash(password!!)
            val corpModel =  BaseCorp.ref
            var corp = ModelDataObject(model = BaseCorp.ref)
            corp.setFieldValue(corpModel.name,corpName)
            corp.setFieldValue(corpModel.address,"公司地址")
            corp.setFieldValue(corpModel.comment,"公司名称")
            corp.setFieldValue(corpModel.fax,"021-65720566")
            corp.setFieldValue(corpModel.telephone,"021-65720566")
            corp.setFieldValue(corpModel.website,"https://bg.work")



            val partnerModel = BasePartner.ref
            var partner = ModelDataObject(model = partnerModel)
            partner.setFieldValue(partnerModel.userName,userName)
            partner.setFieldValue(partnerModel.password,password)
            partner.setFieldValue(partnerModel.userIcon,"super_admin_icon")
            partner.setFieldValue(partnerModel.email,"example@example.com")
            partner.setFieldValue(partnerModel.name,name?:"Administrator")
            partner.setFieldValue((partnerModel as ChatPartner).chatUUID,GUID.randString())
            partner.setFieldValue(partnerModel.birthday, Date())
            partner.setFieldValue(partnerModel.createTime,Date())
            partner.setFieldValue(partnerModel.lastModifyTime,Date())
            partner.setFieldValue(ChatPartner.ref.chatUUID,GUID.randString())

            val partnerRoleModel = BasePartnerRole.ref

            val partnerRole = ModelDataObject(model = partnerRoleModel)
            partnerRole.setFieldValue(partnerRoleModel.isSuper,1)
            partnerRole.setFieldValue(partnerRoleModel.name,"超级管理员")
            partnerRole.setFieldValue(partnerRoleModel.lastModifyTime,Date())
            partnerRole.setFieldValue(partnerRoleModel.createTime,Date())
            //partnerRole.setFieldValue(partnerRoleModel)

            val apps = arrayListOf<ModelDataObject>()
            AppModel.ref.appPackageManifests.forEach { t, u ->
                var app = ModelDataObject(model = BaseApp.ref)
                app.setFieldValue(BaseApp.ref.name,u.name)
                app.setFieldValue(BaseApp.ref.defaultFlag,0)
                app.setFieldValue(BaseApp.ref.title,u.title)
                app.setFieldValue(BaseApp.ref.lastModifyTime,Date())
                app.setFieldValue(BaseApp.ref.createTime,Date())
                apps.add(app)
            }
            val partnerCorpRoleRel = ModelDataObject(model = BaseCorpPartnerRel.ref)
            partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.lastModifyTime,Date())
            partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.createTime,Date())
            partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.isDefaultCorp,1)
            //start serialize to database
            val def = DefaultTransactionDefinition()
            def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
            val status = txManager.getTransaction(def)
            try {

                var retCorp = BaseCorp.ref.rawCreate(corp)
                if(retCorp.first!=null && retCorp.first!!>0){
                    partner.setFieldValue(partnerModel.lastModifyCorpID,retCorp.first)
                    partner.setFieldValue(partnerModel.createCorpID,retCorp.first)
                    val retPartner = BasePartner.ref.rawCreate(partner)
                    if(retPartner.first!=null && retPartner.first!!>0){
                        partnerRole.setFieldValue(partnerRoleModel.createPartnerID,retPartner.first)
                        partnerRole.setFieldValue(partnerRoleModel.lastModifyPartnerID,retPartner.first)
                        partnerRole.setFieldValue(partnerRoleModel.createCorpID,retCorp.first)
                        partnerRole.setFieldValue(partnerRoleModel.lastModifyCorpID,retCorp.first)
                        partnerRole.setFieldValue(partnerRoleModel.corp,retCorp.first)
                        val retPartnerRole = BasePartnerRole.ref.rawCreate(partnerRole)
                        if(retPartnerRole.first!=null && retPartnerRole.first!!>0){
                            apps.forEach {
                                it.setFieldValue(BaseApp.ref.createPartnerID,retPartner.first)
                                it.setFieldValue(BaseApp.ref.lastModifyPartnerID,retPartner.first)
                                it.setFieldValue(BaseApp.ref.createCorpID,retCorp.first)
                                it.setFieldValue(BaseApp.ref.lastModifyCorpID,retCorp.first)
                                it.setFieldValue(BaseApp.ref.partnerRole,retPartnerRole.first)
                            }
                        }
                        apps.forEach {
                            BaseApp.ref.rawCreate(it)
                        }
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.corp,retCorp.first)
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.partner,retPartner.first)
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.partnerRole,retPartnerRole.first)
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.createPartnerID,retPartner.first)
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.lastModifyPartnerID,retPartner.first)
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.createCorpID,retCorp.first)
                        partnerCorpRoleRel.setFieldValue(BaseCorpPartnerRel.ref.lastModifyCorpID,retCorp.first)
                        val retPartnerCorpRoleRel = BaseCorpPartnerRel.ref.rawCreate(partnerCorpRoleRel)
                        if(retPartnerCorpRoleRel.first!=null && retPartnerCorpRoleRel.first!!>0){
                            this.initData(retPartner.first!!,retCorp.first!!)
                            retObj.addProperty("errorCode",0)
                            retObj.addProperty("message","注册成功")
                            txManager.commit(status)
                            return retObj
                        }
                    }
                }

            } catch (ex: Exception) {

                ex.printStackTrace()
            }
            try
            {
                txManager.rollback(status)
            }
            catch(ex:Exception)
            {

            }
            retObj.addProperty("errorCode",3)
            retObj.addProperty("message","系统忙，稍后重试！")
            return retObj
        }
    private fun initData(partnerID:Long,corpID:Long){
        //add channel
        var channelObj = ModelDataObject(model = ChatChannel.ref)
        channelObj.setFieldValue(ChatChannel.ref.name,"全体员工")
        channelObj.setFieldValue(ChatChannel.ref.uuid,GUID.randString())
        channelObj.setFieldValue(ChatChannel.ref.defaultFlag,1)
        channelObj.setFieldValue(ChatChannel.ref.broadcastType,0)
        channelObj.setFieldValue(ChatChannel.ref.owner,partnerID)
        channelObj.setFieldValue(ChatChannel.ref.owner,partnerID)
        channelObj.setFieldValue(ChatChannel.ref.createPartnerID,partnerID)
        channelObj.setFieldValue(ChatChannel.ref.lastModifyPartnerID,partnerID)
        channelObj.setFieldValue(ChatChannel.ref.createCorpID,corpID)
        channelObj.setFieldValue(ChatChannel.ref.lastModifyCorpID,corpID)
        channelObj.setFieldValue(ChatChannel.ref.lastModifyTime,Date())
        channelObj.setFieldValue(ChatChannel.ref.createTime,Date())
        ChatChannel.ref.rawCreate(channelObj)
        //add customer follow step
        val steps = arrayOf("潜在机会","需求沟通","报价合同","赢得订单","失败订单")
        var stepID = 0.toLong()
        steps.reversed().forEachIndexed { index, s ->
            var customerFollowStep = ModelDataObject(model = CustomerFollowStep.ref)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.name,s)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.seqIndex,index+1)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.createPartnerID,partnerID)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.lastModifyPartnerID,partnerID)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.createCorpID,corpID)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.lastModifyCorpID,corpID)
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.lastModifyTime,Date())
            customerFollowStep.setFieldValue(CustomerFollowStep.ref.createTime,Date())
            var ret = CustomerFollowStep.ref.rawCreate(customerFollowStep)
            if(ret.first!=null && ret.first!!>0){
                stepID = ret.first!!
            }
        }

        //add customer
        var customer = ModelDataObject(model = Customer.ref)
        customer.setFieldValue(Customer.ref.name,"上海星野信息科技有限公司")
        customer.setFieldValue(Customer.ref.corpName,"上海星野信息科技有限公司")
        customer.setFieldValue(Customer.ref.isCorp,1)
        customer.setFieldValue(Customer.ref.mobile,"18621991588")
        customer.setFieldValue(Customer.ref.sex,1)
        customer.setFieldValue(Customer.ref.telephone,"400-850-3031")
        customer.setFieldValue(Customer.ref.email,"bayoujishu001@qq.com")
        customer.setFieldValue(Customer.ref.website,"https://bg.work")
        customer.setFieldValue(Customer.ref.comment,"办公系统开发服务商")
        customer.setFieldValue(Customer.ref.createPartnerID,partnerID)
        customer.setFieldValue(Customer.ref.lastModifyPartnerID,partnerID)
        customer.setFieldValue(Customer.ref.createCorpID,corpID)
        customer.setFieldValue(Customer.ref.lastModifyCorpID,corpID)
        customer.setFieldValue(Customer.ref.lastModifyTime,Date())
        customer.setFieldValue(Customer.ref.createTime,Date())
       var ret = Customer.ref.rawCreate(customer)

        //add follow step customer
        if(ret.first!=null && ret.first!!>0 && stepID>0){
            var stepCustomerRel  = ModelDataObject(model = CustomerFollowStepCustomerRel.ref)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.customer,ret.first)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.customerFollowStep,stepID)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.seqIndex,1)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.createPartnerID,partnerID)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.lastModifyPartnerID,partnerID)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.createCorpID,corpID)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.lastModifyCorpID,corpID)
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.lastModifyTime,Date())
            stepCustomerRel.setFieldValue(CustomerFollowStepCustomerRel.ref.createTime,Date())
            CustomerFollowStepCustomerRel.ref.rawCreate(stepCustomerRel)
        }
    }
}