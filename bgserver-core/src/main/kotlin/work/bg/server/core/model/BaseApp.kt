/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *it under the terms of the GNU Affero General Public License as published by
t *  *  *he Free Software Foundation, either version 3 of the License.

 *  *  *This program is distributed in the hope that it will be useful,
 *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *GNU Affero General Public License for more details.

 *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  *
  *
  */

package work.bg.server.core.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import dynamic.model.query.config.ActionType
import dynamic.model.query.mq.*
import dynamic.model.query.mq.billboard.FieldValueDependentingRecordBillboard
import dynamic.model.query.mq.model.AppModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult

@Model("app","应用")
class BaseApp(tableName:String, schemaName:String):ContextModel(tableName,schemaName) {

    companion object: RefSingleton<BaseApp> {
        override lateinit var ref: BaseApp
    }
    constructor():this("base_app","public")

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name= ModelField(null,
            "name",
            FieldType.STRING,
            "名称")

    var modelUrl = ModelField(null,"model_url",FieldType.STRING,"快捷连接")

    val title= ModelField(null,
            "title",
            FieldType.STRING,
            "说明",
            defaultValue = object : FieldValueDependentingRecordBillboard {
                override fun computeValue(fvs: FieldValueArray?, actionType: ActionType): Pair<Boolean, Any?> {
                    val nameField = this@BaseApp.name
                    fvs?.let {
                        val name = it.getValue(nameField) as String?
                        name?.let {
                            val appManifest = AppModel.ref.appPackageManifests[name]
                            appManifest?.let {
                                return Pair(true, it.title)
                            }
                        }
                    }
                    return Pair(false, null)
                }
            })

    val defaultFlag= ModelField(null,
            "default_flag",
            FieldType.INT,
            "默认",
            defaultValue = 0)

    val partnerRole= ModelMany2OneField(null,
            "partner_role_id",
            FieldType.BIGINT,
            targetModelTable = "public.base_partner_role",
            targetModelFieldName = "id",
            title = "角色",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE)
    )

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,this.partnerRole,advice = "角色的应用必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,this.partnerRole,advice = "角色的应用必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    @Action("loadAppContainer")
    fun loadAppContainer(@RequestBody appData :JsonObject,
                         partnerCache:PartnerCache): ActionResult?{
        var res = ActionResult()
        var app= appData["app"].asString
        var menu = appData["menu"].asJsonObject
        var menuApp = menu["app"]?.asString
        var menuName = menu["menu"]?.asString
        var viewKeys = partnerCache.getAccessControlAppViewKey(app)
        var menuTree = partnerCache.getAccessControlMenu(menuApp,menuName)
        if(viewKeys!=null){
            res.bag["views"]=viewKeys
        }
        if(menuName!=null && menuTree!=null){
            res.bag[menuName]=menuTree
        }
        return res
    }
    @Action("saveShortcutAppSetting")
    fun saveShortcutAppSetting(@RequestBody shortcutApps :JsonArray,
                               partnerCache:PartnerCache):ActionResult?{
        var ret= ActionResult()
        BasePartnerAppShortcut.ref.acDelete(criteria = eq(BasePartnerAppShortcut.ref.partner,partnerCache.partnerID),partnerCache = partnerCache)
        var modelDataArray= ModelDataArray(model = BasePartnerAppShortcut.ref)
        var shortRef= BasePartnerAppShortcut.ref
        var index=0
        shortcutApps.forEach {
            var jo = it as JsonObject
            var fvs = FieldValueArray()
            fvs.setValue(shortRef.partner,partnerCache.partnerID)
            var name = jo["name"]?.asString
            name?.let {
                var app=this.rawRead(criteria = eq(this.name,name),useAccessControl = true,partnerCache = partnerCache)?.firstOrNull()
                app?.let {
                    fvs.setValue(shortRef.app,app)
                }
            }
            fvs.setValue(shortRef.index,index)
            index++
            modelDataArray.add(fvs)
        }
        shortRef.safeCreate(modelDataArray,partnerCache=partnerCache,useAccessControl = true)
        return ret
    }

    fun loadInstallApps():Any{
        var jArr = JsonArray()
        var metaObj=JsonObject()
        AppModel.ref.appPackageManifests.forEach { t, u ->
            jArr.add(this.gson.toJsonTree(mapOf(
                    "text" to u.title,
                    "value" to u.name
            )))
        }
        metaObj.add("options",jArr)
        return metaObj
    }
    fun loadInstallAppsNameToTitle():Any{
        var jArr = JsonArray()
        var metaObj=JsonObject()
        AppModel.ref.appPackageManifests.forEach { t, u ->
            metaObj.addProperty(u.name,u.title)
        }
        return metaObj
    }

    class BaseAppData(val name:String,val modelUrl:String)
}