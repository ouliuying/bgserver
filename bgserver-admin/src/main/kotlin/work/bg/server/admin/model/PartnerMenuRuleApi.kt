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
import dynamic.model.query.mq.eq
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartnerRole
import work.bg.server.core.model.BasePartnerRoleAppMenuRule
import work.bg.server.core.model.BasePartnerRoleModelViewRule
import work.bg.server.core.model.ContextModel
import work.bg.server.core.ui.MenuNode
import work.bg.server.core.ui.MenuTree
import work.bg.server.core.ui.UICache
import work.bg.server.util.TypeConvert

@Model("partnerMenuRuleApi","员工角色菜单管理")
class PartnerMenuRuleApi:ContextModel("base_partner_menu_rule_api","public") {
    companion object: RefSingleton<PartnerMenuRuleApi> {
        override lateinit var ref: PartnerMenuRuleApi
    }
    override fun isDynamic(): Boolean {
        return true
    }

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    private  fun loadAppMenus(partnerCache: PartnerCache): JsonArray {
        var ja = JsonArray()
        AppModelWeb.ref.appPackageManifests.forEach { t, u ->
            var appMenu = UICache.ref.getAppMenu(u.name)
            appMenu?.let {
                var appObj = JsonObject()
                appObj.addProperty("name",u.name)
                appObj.addProperty("title",u.title)
                var treeData = JsonArray()
                it.menus.forEach {
                    var mt = this.buildMenuTree(it.value)
                    mt?.let {
                        treeData.add(it)
                    }
                }
                appObj.add("treeData",treeData)
                ja.add(appObj)
            }
        }
        return ja
    }
    private fun buildMenuTree(mt: MenuTree):JsonObject{
        var jo = JsonObject()
        jo.addProperty("title",mt.title)
        jo.addProperty("key",mt.key)
        jo.addProperty("icon",mt.icon)
        mt.children?.let {
            val children = this.buildMenuChildren(it)
            jo.add("children",children)
        }
        return jo
    }
    private  fun buildMenuChildren(menuNodes:ArrayList<MenuNode>):JsonArray{
        var ja=JsonArray()
        menuNodes.forEach {
            var jjo = JsonObject()
            jjo.addProperty("title",it.title)
            jjo.addProperty("key",it.key)
            jjo.addProperty("icon",it.icon)
            it.children?.let {
                val children = this.buildMenuChildren(it)
                jjo.add("children",children)
            }
            ja.add(jjo)
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

    @Action("loadModelMenuMetaData")
    fun loadModelMenuMetaData(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar= ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }

        var id = data?.get("id")?.asLong
        var ruleData:JsonObject?=null
        if(id!=null){
            val rule = BasePartnerRoleAppMenuRule.ref.rawRead(criteria = eq(BasePartnerRoleAppMenuRule.ref.id,id))?.firstOrNull()
            rule?.let {
                ruleData = JsonObject()
                ruleData?.addProperty("app",rule.getFieldValue(BasePartnerRoleAppMenuRule.ref.app) as String?)
                ruleData?.addProperty("roleID",(rule.getFieldValue(BasePartnerRoleAppMenuRule.ref.partnerRole) as ModelDataObject?)?.idFieldValue?.value as Number?)
                ruleData?.addProperty("rule",rule.getFieldValue(BasePartnerRoleAppMenuRule.ref.menuRule) as String?)
            }
        }
        var roles = this.loadRoles(partnerCache = partnerCache)
        var appMenus = this.loadAppMenus(partnerCache)

        ar.bag["metaData"]= mapOf(
                "roles" to roles,
                "appMenus" to appMenus,
                "data" to ruleData
        )
        return ar
    }
    @Action("saveAppMenuRule")
    fun saveAppMenuRule(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar= ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        var roleID = data?.get("roleID")?.asLong
        var app = data?.get("app")?.asString
        var rule = data?.getAsJsonArray("rule")?: JsonArray()
        var id = data?.get("id")?.asLong
        if(id!=null){

        }
        else{
            if(roleID!=null && app!=null){
                var mo = ModelDataObject(model = BasePartnerRoleAppMenuRule.ref)
                mo.setFieldValue(BasePartnerRoleAppMenuRule.ref.partnerRole,roleID)
                mo.setFieldValue(BasePartnerRoleAppMenuRule.ref.app,app)
                mo.setFieldValue(BasePartnerRoleAppMenuRule.ref.menuRule,rule.toString())
                var ret =  BasePartnerRoleAppMenuRule.ref.rawCreate(mo,useAccessControl = true,partnerCache = partnerCache)
                if(ret.first!=null && ret.first!!>0){
                    ar.description="添加成功"
                    return ar
                }
                ar.errorCode = ErrorCode.UNKNOW
                ar.description="提交参数错误或已经存在"
            }
        }
        return ar
    }
    @Action("loadPartnerMenuRulePage")
    fun loadPartnerMenuRulePage(@RequestBody data:JsonObject?,partnerCache: PartnerCache):ActionResult{
        var ar=ActionResult()
        if(partnerCache.currRole?.isSuper == null || !partnerCache.currRole!!.isSuper){
            return ar
        }
        val pageSize = data?.get("pageSize")?.asInt?:10
        val pageIndex = data?.get("pageIndex")?.asInt?:1
        val pageDatas= BasePartnerRoleAppMenuRule.ref.rawRead(pageSize = pageSize,
                pageIndex = pageIndex,
                useAccessControl = true,
                partnerCache = partnerCache)?.toModelDataObjectArray()

        val totalCount = BasePartnerRoleAppMenuRule.ref.rawCount(criteria = null,
                useAccessControl = true,
                partnerCache = partnerCache)
        var rows = JsonArray()
        pageDatas?.forEach {
            var jo = JsonObject()
            jo.addProperty("app",it.getFieldValue(BasePartnerRoleAppMenuRule.ref.app) as String?)
            jo.addProperty("id",it.getFieldValue(BasePartnerRoleAppMenuRule.ref.id) as Long?)
            val role = it.getFieldValue(BasePartnerRoleAppMenuRule.ref.partnerRole) as ModelDataObject?
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