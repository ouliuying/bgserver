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
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.`is`
import dynamic.model.query.mq.eq
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.dom4j.io.SAXReader
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartner
import work.bg.server.core.model.BasePartnerRole
import work.bg.server.core.model.ContextModel
import work.bg.server.corp.model.Department
import work.bg.server.util.TypeConvert
import java.io.StringReader

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



    @Action("loadCreateMeta")
    fun loadCreateMeta(partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var roles = this.loadRoles(partnerCache = partnerCache)
        var partners =this.loadPartners(partnerCache)
        var departmentTree = this.buildDepartmentTree(partnerCache)
        var models = this.loadModels(partnerCache)
        ar.bag["metaData"]= mapOf(
                "roles" to roles,
                "partners" to partners,
                "departmentTree" to departmentTree,
                "models" to models
        )
        return ar
    }


    @Action("saveCreateMeta")
    fun saveCreateMeta(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar =ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }

        return ar
    }

}