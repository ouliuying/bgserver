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
import work.bg.server.core.model.*
import work.bg.server.corp.model.Department
import work.bg.server.util.TypeConvert

@Model("partnerRuleApi","员工角色权限管理")
class PartnerRuleApi: ContextModel("base_partner_rule_api","public") {
    companion object: RefSingleton<PartnerRuleApi> {
        override lateinit var ref: PartnerRuleApi
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
        AppModelWeb.ref.models?.forEach {model->
            if(model.isDynamic()){
                return@forEach
            }
            var modelJson = JsonObject()
            modelJson.addProperty("model",model.meta.name)
            modelJson.addProperty("app",model.meta.appName)
            modelJson.addProperty("title",model.meta.title)
            var fieldsArr = JsonArray()
            modelJson.add("fields",fieldsArr)
                model.fields.getAllPersistFields().values.forEach {fd->
                    var fJo = JsonObject()
                    fJo.addProperty("name",fd.propertyName)
                    fJo.addProperty("title",fd.title)
                    fieldsArr.add(fJo)
                }
            ja.add(modelJson)
        }
        return ja
    }
    private  fun buildDepartmentTree(partnerCache: PartnerCache):JsonObject{
        var jo =JsonObject()
        this.loadSubDepartments(null,jo,partnerCache)
        return jo
    }

    private  fun loadSubDepartments(parentID:Long?,departmentTree:JsonObject,partnerCache: PartnerCache){
        var modelObjects = if(parentID!=null) Department.ref.rawRead(criteria = eq(Department.ref.parent,parentID))?.toModelDataObjectArray()
        else Department.ref.rawRead(criteria = `is`(Department.ref.parent,parentID))?.toModelDataObjectArray()
        var ja= JsonArray()
        modelObjects?.forEach {
            var id = TypeConvert.getLong(it.getFieldValue(Department.ref.id) as Number?)
            val name = it.getFieldValue(Department.ref.name) as String?
            id?.let {
                var jo = JsonObject()
                jo.addProperty("value",id)
                jo.addProperty("title",name)
                ja.add(jo)
                loadSubDepartments(id,jo,partnerCache = partnerCache)
            }
        }
        departmentTree.add("children",ja)
    }
    private fun loadPartners(partnerCache: PartnerCache):JsonArray{
        var ja = JsonArray()
        var datas = BasePartner.ref.rawRead(criteria = null,useAccessControl = true,partnerCache = partnerCache)?.toModelDataObjectArray()
        datas?.forEach {
            var id = TypeConvert.getLong(it.getFieldValue(BasePartner.ref.id) as Number?)
            var userName = it.getFieldValue(BasePartner.ref.userName) as String?
            var jo = JsonObject()
            jo.addProperty("id",id)
            jo.addProperty("userName",userName)
            ja.add(jo)
        }
        return ja
    }

    private  fun loadRoles(partnerCache: PartnerCache):JsonArray{
        var ja = JsonArray()
        var roles = BasePartnerRole.ref.rawRead(criteria = null,useAccessControl = true,partnerCache = partnerCache)?.toModelDataObjectArray()
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

    @Action("loadRoleAppModelData")
    fun loadRoleAppModelData(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var roleID = data?.get("roleID")?.asLong
        val app = data?.get("app")?.asString
        val model = data?.get("model")?.asString
        if(roleID!=null && app!=null && model!=null){
           var mo= BasePartnerRoleModelRule.ref.rawRead(criteria = and(eq(BasePartnerRoleModelRule.ref.partnerRole,roleID),
                    eq(BasePartnerRoleModelRule.ref.app,app),
                    eq(BasePartnerRoleModelRule.ref.model,model)))?.firstOrNull()
            mo?.let {rule->
                var ruleData = JsonObject()
                ruleData?.addProperty("app",rule.getFieldValue(BasePartnerRoleModelRule.ref.app) as String?)
                ruleData?.addProperty("model",rule.getFieldValue(BasePartnerRoleModelRule.ref.model) as String?)
                ruleData?.addProperty("roleID",(rule.getFieldValue(BasePartnerRoleModelRule.ref.partnerRole) as ModelDataObject?)?.idFieldValue?.value as Number?)
                ruleData?.addProperty("rule",rule.getFieldValue(BasePartnerRoleModelRule.ref.modelRule) as String?)
                ar.bag["metaData"]= mapOf(
                        "data" to ruleData
                )
            }
        }
        return ar
    }

    @Action("loadCreateMeta")
    fun loadCreateMeta(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var id = data?.get("id")?.asLong
        var ruleData:JsonObject?=null
        if(id!=null){
            val rule = BasePartnerRoleModelRule.ref.rawRead(criteria = eq(BasePartnerRoleModelRule.ref.id,id))?.firstOrNull()
            rule?.let {
                ruleData = JsonObject()
                ruleData?.addProperty("app",rule.getFieldValue(BasePartnerRoleModelRule.ref.app) as String?)
                ruleData?.addProperty("model",rule.getFieldValue(BasePartnerRoleModelRule.ref.model) as String?)
                ruleData?.addProperty("roleID",(rule.getFieldValue(BasePartnerRoleModelRule.ref.partnerRole) as ModelDataObject?)?.idFieldValue?.value as Number?)
                ruleData?.addProperty("rule",rule.getFieldValue(BasePartnerRoleModelRule.ref.modelRule) as String?)
            }
        }
        var roles = this.loadRoles(partnerCache = partnerCache)
        var partners =this.loadPartners(partnerCache)
        var departmentTree = this.buildDepartmentTree(partnerCache)
        var models = this.loadModels(partnerCache)
        ar.bag["metaData"]= mapOf(
                "roles" to roles,
                "partners" to partners,
                "departmentTree" to departmentTree,
                "models" to models,
                "data" to ruleData
        )
        return ar
    }

    @Action("doSaveModelRule")
    fun saveCreateMeta(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var id = data?.get("id")?.asLong
        var ruleModelObject= ModelDataObject(model=BasePartnerRoleModelRule.ref)
        val app = data?.get("app")?.asString
        val model = data?.get("model")?.asString
        val roleID = data?.get("roleID")?.asLong
        if(app!=null && model!=null && roleID!=null && this.appModelExist(app,model) && this.roleExist(roleID, partnerCache)){

            if(this.appModelExistInStore(roleID,app,model,id)){
                ar.errorCode=ErrorCode.UNKNOW
                ar.description="已经存在"
                return ar
            }

            if(id!=null && id!!>0){
                ruleModelObject.setFieldValue(BasePartnerRoleModelRule.ref.id, id)
            }
            ruleModelObject.setFieldValue(BasePartnerRoleModelRule.ref.app, app)
            ruleModelObject.setFieldValue(BasePartnerRoleModelRule.ref.model, model)
            ruleModelObject.setFieldValue(BasePartnerRoleModelRule.ref.partnerRole, roleID)
            val ruleJa = JsonArray()
            data?.getAsJsonArray("accessTypes")?.forEach { acIt ->
                acIt.asJsonObject.get("accessType")?.asString?.let {
                    val acObj = acIt.asJsonObject
                    var mrObj = JsonObject()
                    mrObj.addProperty("accessType",it)
                    mrObj.addProperty("enable", acObj.get("enable")?.asBoolean ?: true)
                    mrObj.addProperty("isolation", acObj.get("isolation")?.asString ?: "corp")
                    mrObj.add("targetDepartments", acObj.getAsJsonArray("departments") ?: JsonArray())
                    if(it!="delete"){
                        mrObj.add("disableFields", acObj.getAsJsonArray("disableFields") ?: JsonArray())
                    }
                    mrObj.add("targetRoles", acObj.getAsJsonArray("roles") ?: JsonArray())
                    mrObj.add("targetPartners", acObj.getAsJsonArray("partners") ?: JsonArray())
                    mrObj.add("rules", acObj.getAsJsonArray("rules") ?: JsonArray())
                    mrObj.addProperty("criteria", acObj.get("criteria")?.asString ?: "")
                    mrObj.addProperty("overrideCriteria", acObj.get("overrideCriteria")?.asString ?: "")
                    ruleJa.add(mrObj)
                }
            }
            ruleModelObject.setFieldValue(BasePartnerRoleModelRule.ref.modelRule, ruleJa.toString())

            var ret = if(id==null || id!!<1) BasePartnerRoleModelRule.ref.safeCreate(ruleModelObject,
                    useAccessControl = true,
                    partnerCache = partnerCache)
            else BasePartnerRoleModelRule.ref.safeEdit(ruleModelObject,
                    useAccessControl = true,
                    partnerCache = partnerCache)

            if(ret.first!=null && ret.first!!>0){
                ar.errorCode = ErrorCode.SUCCESS
                if(id!=null && id!!>0){
                    ar.description="更新成功"
                }
                else{
                    ar.description="添加成功"
                }
            }
            else{
                ar.errorCode = ErrorCode.UNKNOW
                ar.description=ret.second?:"添加失败"
            }
        }
        else{
            ar.errorCode =ErrorCode.UNKNOW
            ar.description="针对此模型的设置已经存在，不能添加"
        }

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
        var ret = if(id==null||id!!<1) BasePartnerRoleModelRule.ref.rawCount(
                criteria = and(eq(BasePartnerRoleModelRule.ref.app,app),
                        eq(BasePartnerRoleModelRule.ref.model,model),
                        eq(BasePartnerRoleModelRule.ref.partnerRole,roleID)))
        else BasePartnerRoleModelRule.ref.rawCount(
                criteria = and(eq(BasePartnerRoleModelRule.ref.app,app),
                        eq(BasePartnerRoleModelRule.ref.model,model), notEq(
                        BasePartnerRoleModelRule.ref.id,id
                ), eq(BasePartnerRoleModelRule.ref.partnerRole,roleID)))
        return ret>0
    }
    @Action("loadPartnerRulePage")
    fun loadPartnerRulePage(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar=ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        val pageSize = data?.get("pageSize")?.asInt?:10
        val pageIndex = data?.get("pageIndex")?.asInt?:1
        val pageDatas= BasePartnerRoleModelRule.ref.rawRead(pageSize = pageSize,
                pageIndex = pageIndex,
                useAccessControl = true,
                partnerCache = partnerCache)?.toModelDataObjectArray()
        val totalCount = BasePartnerRoleModelRule.ref.rawCount(criteria = null,
                useAccessControl = true,
                partnerCache = partnerCache)
        var rows = JsonArray()
        pageDatas?.forEach {
            var jo = JsonObject()
            jo.addProperty("app",it.getFieldValue(BasePartnerRoleModelRule.ref.app) as String?)
            jo.addProperty("model",it.getFieldValue(BasePartnerRoleModelRule.ref.model) as String?)
            jo.addProperty("id",it.getFieldValue(BasePartnerRoleModelRule.ref.id) as Long?)
            val role = it.getFieldValue(BasePartnerRoleModelRule.ref.partnerRole) as ModelDataObject?
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