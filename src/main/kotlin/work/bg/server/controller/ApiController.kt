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
import work.bg.server.chat.model.ChatPartner
import work.bg.server.core.model.*
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
}