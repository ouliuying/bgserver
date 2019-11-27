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

package work.bg.server.admin.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartnerRole
import work.bg.server.core.model.BasePartnerRoleModelRule
import work.bg.server.core.model.BasePartnerRoleModelViewRule
import work.bg.server.core.model.ContextModel
import work.bg.server.core.ui.UICache
import work.bg.server.util.TypeConvert

@Model("partnerViewRuleApi","员工角色视图管理")
class PartnerViewRuleApi: ContextModel("base_partner_view_rule_api","public") {
    companion object: RefSingleton<PartnerViewRuleApi> {
        override lateinit var ref: PartnerViewRuleApi
    }
    override fun isDynamic(): Boolean {
        return true
    }

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {


    }
    private  fun loadModels(partnerCache: PartnerCache):JsonArray{
        var ja =JsonArray()
        AppModelWeb.ref.models?.forEach { model->
            var modelJson = JsonObject()
            modelJson.addProperty("model",model.meta.name)
            modelJson.addProperty("app",model.meta.appName)
            modelJson.addProperty("title",model.meta.title)
            var views = UICache.ref.getModelViews(model.meta.appName,
                    model.meta.name)
            var viewDatas = JsonArray()
            views.forEach{ viewIT->
                var viewData = JsonObject()
                viewData.addProperty("viewType",viewIT.key)
                var fieldsDatas = JsonArray()
                viewIT.value.fields.forEach {fdIT->
                    var fieldData = JsonObject()
                    fieldData.addProperty("name",fdIT.fullName)
                    fieldData.addProperty("title",fdIT.title)
                    fieldData.addProperty("app",fdIT.app)
                    fieldData.addProperty("model",fdIT.model)
                    fieldData.addProperty("viewType",fdIT.viewType)
                    fieldsDatas.add(fieldData)
                }
                viewData.add("fields",fieldsDatas)
                var agDatas = JsonArray()
                viewIT.value.refActionGroups.forEach {ag->
                    var agData = JsonObject()
                    agData.addProperty("name",ag.groupName)
                    agData.addProperty("app",ag.app)
                    agData.addProperty("model",ag.model)
                    agData.addProperty("viewType",ag.viewType)
                    var tDatas = JsonArray()
                    var va = UICache.ref.getViewAction(ag.app,ag.model,ag.viewType,ag.groupName)
                    va?.let {vaIT->
                        vaIT.groups[ag.groupName]?.let {
                            tgXIT->
                            tgXIT.triggers.forEach {tXIT->
                                var tData =JsonObject()
                                tData.addProperty("actionName",tXIT.actionName)
                                tData.addProperty("name",tXIT.name)
                                tData.addProperty("app",tXIT.app)
                                tData.addProperty("model",tXIT.model)
                                tData.addProperty("viewType",tXIT.viewType)
                                tData.addProperty("title",tXIT.title)
                                tDatas.add(tData)
                            }
                        }
                    }
                    agData.add("triggers",tDatas)
                    agDatas.add(agData)
                }
                viewData.add("actionGroups",agDatas)
                viewDatas.add(viewData)
            }
            modelJson.add("views",viewDatas)
            ja.add(modelJson)
        }
        return ja
    }

    private  fun loadRoles(partnerCache: PartnerCache): JsonArray {
        var ja = JsonArray()
        var roles = BasePartnerRole.ref.rawRead(criteria = null,
                useAccessControl = true,
                partnerCache = partnerCache)?.toModelDataObjectArray()
        roles?.forEach {
            var jo  = JsonObject()
            val id = TypeConvert.getLong(it.getFieldValue(BasePartnerRole.ref.id) as Number?)
            val name = it.getFieldValue(BasePartnerRole.ref.name) as String?
            jo.addProperty("id",id)
            jo.addProperty("name",name)
            ja.add(jo)
        }
        return ja
    }
    @Action("loadModelViewMetaData")
    fun loadModelViewMetaData(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar = ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var id = data?.get("id")?.asLong
        var ruleData:JsonObject?=null
        if(id!=null){
            val rule = BasePartnerRoleModelViewRule.ref.rawRead(criteria = eq(BasePartnerRoleModelViewRule.ref.id,id))?.firstOrNull()
            rule?.let {
                ruleData = JsonObject()
                ruleData?.addProperty("app",rule.getFieldValue(BasePartnerRoleModelViewRule.ref.app) as String?)
                ruleData?.addProperty("model",rule.getFieldValue(BasePartnerRoleModelViewRule.ref.model) as String?)
                ruleData?.addProperty("roleID",(rule.getFieldValue(BasePartnerRoleModelViewRule.ref.partnerRole) as ModelDataObject?)?.idFieldValue?.value as Number?)
                ruleData?.addProperty("rule",rule.getFieldValue(BasePartnerRoleModelViewRule.ref.viewRule) as String?)
            }
        }
        var roles = this.loadRoles(partnerCache = partnerCache)
        var models = this.loadModels(partnerCache)

        ar.bag["metaData"]= mapOf(
                "roles" to roles,
                "models" to models,
                "data" to ruleData
        )
        return ar
    }
    protected  fun roleExist(roleID:Long,partnerCache: PartnerCache):Boolean{
        if(BasePartnerRole.ref.rawCount(criteria = eq(BasePartnerRole.ref.id,roleID),useAccessControl = true,partnerCache = partnerCache)>0){
            return true
        }
        return false
    }
    protected  fun appModelExist(app:String,model:String):Boolean{
        if(this.appModel.appPackageManifests.filter {
                    it.value.name == app
                }.count()<1)
        {
            return false
        }

        if(this.appModel.models!!.filter {
                    it.meta.name == model
                }.count()<1){
            return false
        }
        return true
    }
    protected  fun appModelExistInStore(roleID: Long,app:String,model:String,id:Long?):Boolean{
      var ret = if(id==null||id!!<1) BasePartnerRoleModelViewRule.ref.rawCount(
              criteria = and(eq(BasePartnerRoleModelViewRule.ref.app,app),
                      eq(BasePartnerRoleModelViewRule.ref.model,model),
                      eq(BasePartnerRoleModelViewRule.ref.partnerRole,roleID)))
        else BasePartnerRoleModelViewRule.ref.rawCount(
              criteria = and(eq(BasePartnerRoleModelViewRule.ref.app,app),
                      eq(BasePartnerRoleModelViewRule.ref.model,model),
                      eq(BasePartnerRoleModelViewRule.ref.partnerRole,roleID),
                      notEq(BasePartnerRoleModelViewRule.ref.id,id)
              ))
        return ret>0
    }
    @Action("saveModelViewRule")
    fun saveModelViewRule(@RequestBody data:JsonObject?,
                          partnerCache: PartnerCache):ActionResult{
        var ar = ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var id = data?.get("id")?.asLong
        val app = data?.get("app")?.asString
        val model = data?.get("model")?.asString
        val roleID = data?.get("roleID")?.asLong
        if(app!=null && model!=null && roleID!=null && this.roleExist(roleID, partnerCache)&& this.appModelExist(app,model)){
            if(this.appModelExistInStore(roleID,app,model,id)){
                ar.errorCode=ErrorCode.UNKNOW
                ar.description="已经存在"
                return ar
            }
            var mo = ModelDataObject(model = BasePartnerRoleModelViewRule.ref)
            if(id!=null && id!!>0){
                mo.setFieldValue(BasePartnerRoleModelViewRule.ref.id,id)
            }
            mo.setFieldValue(BasePartnerRoleModelViewRule.ref.model,model)
            mo.setFieldValue(BasePartnerRoleModelViewRule.ref.app,app)
            mo.setFieldValue(BasePartnerRoleModelViewRule.ref.partnerRole,roleID)
            var viewRules = JsonArray()
            var views = data?.getAsJsonArray("views")
            views?.let {
                it.forEach { it ->
                    val viewRule = JsonObject()
                    val viewObj = it as JsonObject
                    val enable = viewObj.get("enable")?.asBoolean
                    viewRule.addProperty("enable", enable==null || enable)
                    viewRule.addProperty("viewType",viewObj.get("viewType")?.asString)
                    val fields = viewObj.get("fields")?.asJsonArray
                    val fieldRules=JsonArray()
                    fields?.let {
                        it.forEach {fit->
                            val fOjb = fit as JsonObject
                            val name = fOjb.get("name")?.asString
                            val visible = fOjb.get("visible")?.asBoolean
                            val exp = fOjb.get("exp")?.asString?:""
                            if(name!=null){
                                    var j = JsonObject()
                                    j.addProperty("visible",visible!=false)
                                    j.addProperty("exp",exp.trim())
                                    j.addProperty("name",name)
                                    fieldRules.add(j)
                            }
                        }
                    }
                    viewRule.add("fields",fieldRules)

                    val triggers = viewObj.get("triggers")?.asJsonArray
                    val triggerRules=JsonArray()
                    triggers?.let {
                        it.forEach {tit->
                            val tOjb = tit as JsonObject
                            val name = tOjb.get("name")?.asString
                            val visible = tOjb.get("visible")?.asBoolean
                            val exp = tOjb.get("exp")?.asString?:""
                            if(name!=null){
                                var groupTrigger = name.split("/")
                                if(groupTrigger.count()==2){
                                        var j = JsonObject()
                                        j.addProperty("visible",visible!=false)
                                        j.addProperty("exp",exp.trim())
                                        j.addProperty("group",groupTrigger[0])
                                        j.addProperty("trigger",groupTrigger[1])
                                        triggerRules.add(j)
                                }
                            }
                        }
                    }
                    viewRule.add("triggers",triggerRules)
                    viewRules.add(viewRule)
                }
            }
            mo.setFieldValue(BasePartnerRoleModelViewRule.ref.viewRule,viewRules.toString())
           var ret = if(id==null || id!!<1) BasePartnerRoleModelViewRule.ref.rawCreate(mo,useAccessControl = true,partnerCache = partnerCache)
            else BasePartnerRoleModelViewRule.ref.rawEdit(mo,useAccessControl = true,partnerCache = partnerCache)
            if(ret.first!=null && ret.first!!>0){
                if(id!=null && id!!>0){
                    ar.description=" 更新成功"
                }
                else{
                    ar.description="添加成功"
                }
                return ar
            }
        }
        ar.errorCode = ErrorCode.UNKNOW
        ar.description="提交错误"
        return ar
    }


    @Action("loadPartnerViewRulePage")
    fun loadPartnerViewRulePage(@RequestBody data:JsonObject?,
                                partnerCache: PartnerCache):ActionResult{
        var ar=ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        val pageSize = data?.get("pageSize")?.asInt?:10
        val pageIndex = data?.get("pageIndex")?.asInt?:1
        val pageDatas= BasePartnerRoleModelViewRule.ref.rawRead(pageSize = pageSize,
                pageIndex = pageIndex,
                useAccessControl = true,
                partnerCache = partnerCache)?.toModelDataObjectArray()

        val totalCount = BasePartnerRoleModelViewRule.ref.rawCount(criteria = null,
                useAccessControl = true,
                partnerCache = partnerCache)
        var rows = JsonArray()
        pageDatas?.forEach {
            var jo = JsonObject()
            jo.addProperty("app",it.getFieldValue(BasePartnerRoleModelViewRule.ref.app) as String?)
            jo.addProperty("model",it.getFieldValue(BasePartnerRoleModelViewRule.ref.model) as String?)
            jo.addProperty("id",it.getFieldValue(BasePartnerRoleModelViewRule.ref.id) as Long?)
            val role = it.getFieldValue(BasePartnerRoleModelViewRule.ref.partnerRole) as ModelDataObject?
            role?.let {
                jo.addProperty("roleName",it.getFieldValue(BasePartnerRole.ref.name) as String?)
            }
            rows.add(jo)
        }
        ar.bag["totalCount"]=totalCount
        ar.bag["rows"]=rows
        return ar
    }
}