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

package work.bg.server.core.cache

import work.bg.server.core.acrule.AccessControlRule
import work.bg.server.core.acrule.ModelEditAccessControlRule
import work.bg.server.core.model.*
import work.bg.server.core.spring.boot.model.AppModel
import java.util.concurrent.locks.StampedLock
import work.bg.server.core.context.ModelExpressionContext
import work.bg.server.core.mq.*
import work.bg.server.core.ui.*

class PartnerCache(partnerData:Map<String,Any?>?,
                   val partnerID:Long,
                   val corpID:Long,
                   val roleID:Long) {
    var modelExpressionContext: ModelExpressionContext = ModelExpressionContext(partnerID, corpID, roleID)
    companion object {
        private val locker=StampedLock()
        private var corps= mutableMapOf<Long,CorpCache>()
    }
    init {
        this.buildPartnerCache(partnerData)
    }
    val currCorp:CorpCache?
    get() {
        var tamp= locker.readLock()
        try {
                return corps[this.corpID]
        }
        finally {
            locker.unlockRead(tamp)
        }
    }
    val currRole by lazy {
         corps[this.corpID]?.roles?.get(this.roleID)
    }

    private fun buildPartnerCache(partnerData:Map<String,Any?>?){
        var corpObject=partnerData?.get("corpObject") as ModelDataObject
        var partnerRoleObject=partnerData?.get("partnerRoleObject") as ModelDataObject
       // var roleModelsArray=partnerData?.get("roleModelArray") as ModelDataArray
        var corpCache=buildCorpCache(corpID,corpObject,partnerRoleObject)
        rebuildCorp(corpCache)
    }
    private  fun rebuildCorp(corpCache:CorpCache){
        var tamp= locker.writeLock()
        try {
            if(corps.containsKey(corpCache.id)){
                var cc=corps[corpCache.id]
                corpCache.roles.forEach { _, u ->  (cc?.roles as MutableMap)[u.id]=u}
            }
            else
            {
                corps[corpCache.id]=corpCache
            }
        }
        finally {
            locker.unlockWrite(tamp)
        }
    }
    val activeCorp:Map<String,Any?>
    get(){
        var map= mutableMapOf<String,Any?>()
        var corp=this.currCorp
        map["id"]=corp?.id
        map["name"]=corp?.name
        val roleData= mutableMapOf<String,Any?>()
        map["role"]=roleData
        val role=this.currRole
        roleData["id"]=role?.id
        roleData["isSuper"]=if(role!!.isSuper) 1 else 0
        roleData["name"]=role?.name
        return map
    }
    inline  fun <reified T>  getModelCreateAccessControlRules(model:ModelBase):List<T>?{
        return this.currRole?.getModelCreateAccessControlRules(model)
    }
    inline  fun <reified T> getModelReadAccessControlRules(model:ModelBase):List<T>?{
        return this.currRole?.getModelReadAccessControlRules(model)
    }
    inline fun <reified T> getModelEditAccessControlRules(model:ModelBase):List<T>?{
        return this.currRole?.getModelEditAccessControlRules(model)
    }
    inline fun <reified T> getModelDeleteAccessControlRules(model:ModelBase):List<T>?{
        return this.currRole?.getModelDeleteAccessControlRules(model)
    }
    fun  checkEditBelongToPartner(model:ModelBase):Boolean{
         val editRule = this.getEditModelRule(model.meta.appName,model.meta.name)
         return  if(editRule?.checkBelongToPartner!=null) editRule?.checkBelongToPartner > 0 else false
    }

    fun  checkReadBelongToPartner(model:ModelBase):Boolean{
        val readRule = this.getReadModelRule(model.meta.appName,model.meta.name)
        return  if(readRule?.checkBelongToPartner!=null) readRule?.checkBelongToPartner > 0 else false
    }

    private  fun buildCorpCache(corpID:Long,
                                corpObject:ModelDataObject,
                                partnerRoleObject:ModelDataObject):CorpCache{
        var roleID=partnerRoleObject.data.getValue(BasePartnerRole.ref.id) as Long
        var roleName=partnerRoleObject.data.getValue(BasePartnerRole.ref.name) as String
        var isSuper=(partnerRoleObject.data.getValue(BasePartnerRole.ref.isSuper) as Int)>0
        var name=corpObject.data.getValue(BaseCorp.ref?.name!!) as String
        var acRuleMeta = partnerRoleObject.data.getValue(BasePartnerRole.ref.accessControlRule) as String?
        var  role=CorpPartnerRoleCache(roleID,roleName,isSuper,acRuleMeta)
        return CorpCache(corpID,name, mapOf(role.id to role))
    }

    fun getModelRule(app:String,model:String):CorpPartnerRoleCache.ModelRule?{
        return this.currRole?.appRules?.get(app)?.modelRules?.get(model)
    }
    fun getCreateModelRule(app:String,model:String):CorpPartnerRoleCache.ModelRule.CreateAction?{
        return this.currRole?.appRules?.get(app)?.modelRules?.get(model)?.createAction
    }
    fun getDeleteModelRule(app:String,model:String):CorpPartnerRoleCache.ModelRule.DeleteAction?{
        return this.currRole?.appRules?.get(app)?.modelRules?.get(model)?.deleteAction
    }
    fun getEditModelRule(app:String,model:String):CorpPartnerRoleCache.ModelRule.EditAction?{
        return this.currRole?.appRules?.get(app)?.modelRules?.get(model)?.editAction
    }
    fun getReadModelRule(app:String,model:String):CorpPartnerRoleCache.ModelRule.ReadAction?{
        return this.currRole?.appRules?.get(app)?.modelRules?.get(model)?.readAction
    }

    fun getContextValue(contextKey:String):Pair<Boolean,Any?>{
        return this.modelExpressionContext.valueFromContextKey(contextKey)
    }

    fun getAccessControlAppViewKey(app:String):Map<String,Array<String>>?{
        var amv = UICache.ref.getAppModelView(app)
        if(amv!=null){
            var allKeys = amv.modelViewKeys
            var ruleKeys= mutableMapOf<String,Array<String>>()
            allKeys.forEach { t, u ->
                var toKeys = arrayListOf<String>()
                u.forEach {
                    val tag = "$app.$t.$it"
                    val rv=this.currRole?.viewRules?.get(tag)
                    if(rv!=null){
                        if(rv.enable!="false"){
                            toKeys.add(it)
                        }
                    }
                    else{
                        toKeys.add(it)
                    }
                }
                ruleKeys[t]=toKeys.toTypedArray()
            }
            return ruleKeys
        }
        return null
    }
    fun getAccessControlMenu(app:String?,name:String?):MenuTree?{
        if(app.isNullOrEmpty() || name.isNullOrEmpty()){
            return null
        }
        var menu = UICache.ref.getMenu(app,name)
        if(menu!=null){
            var ruleMenu = menu.createCopy() as MenuTree?
            val tag = "$app.$name"
            var mr = this.currRole?.menuRules?.get(tag)
            if(mr!=null){
                ruleMenu = this.applyMenuRule(ruleMenu,mr) as MenuTree?
            }
            return ruleMenu
        }
        return null
    }




    private fun removeMenuChild(menu:MenuNode,app:String,name:String){
        var index=menu.children?.indexOfFirst {
            it is MenuTree && it.name==name && it.app == app
        }
        if(index!=null && index>-1){
            menu.children?.removeAt(index)
        }
    }
    private fun removeMenuItemChild(menu:MenuNode,app:String,model:String,viewType:String){
        var index=menu.children?.indexOfFirst {
            it is MenuNode
                    && it !is MenuTree
                    && it.model==model
                    && it.viewType==viewType
                    && it.app == app
        }
        if(index!=null && index>-1){
            menu.children?.removeAt(index)
        }
    }
    private  fun removeMenuChild(menu:MenuNode,index:Int){
        if(index>-1){
            menu.children?.removeAt(index)
        }
    }
    private  fun findMenuChildIndex(menu:MenuNode,app:String,name:String):Int?{
        return menu.children?.indexOfFirst {
            it is MenuTree && it.name==name && it.app == app
        }
    }
    private fun findMenuItemChildIndex(menu:MenuNode,app:String,model:String,viewType:String):Int?{
        return menu.children?.indexOfFirst {
            it is MenuNode
                    && it !is MenuTree
                    && it.model==model
                    && it.viewType==viewType
                    && it.app == app
        }

    }
    private fun applyMenuRule(menu: MenuNode?, rule:CorpPartnerRoleCache.MenuRule):MenuNode?{
        if(menu==null || rule.visible=="false"){
            return null
        }
        rule.children.forEach {
            when(it.type){
                CorpPartnerRoleCache.MenuRule.MenuType.MENU->{
                    if(it.visible=="false"){
                        removeMenuChild(menu,it.app,it.name)
                    }
                    else{
                        var index=findMenuChildIndex(menu,it.app,it.name)
                        if(index!=null && index>-1){
                            var subMenuNode=menu?.children?.get(index)
                            var newChild=applyMenuRule(subMenuNode,it)
                            if(newChild!=null){
                                menu?.children?.set(index,newChild)
                            }
                            else{
                                removeMenuChild(menu,index)
                            }
                        }
                    }
                }
                CorpPartnerRoleCache.MenuRule.MenuType.MENU_ITEM->{
                    if(it.visible=="false"){
                        removeMenuItemChild(menu,it.app,it.model,it.viewType)
                    }
                    else{
                        var index = findMenuItemChildIndex(menu,it.app,it.model,it.viewType)
                        if(index!=null && index>-1){
                            var subMenuNode=menu?.children?.get(index)
                            var newChild=applyMenuRule(subMenuNode,it)
                            if(newChild!=null){
                                menu.children?.set(index,newChild)
                            }
                            else{
                                removeMenuChild(menu,index)
                            }
                        }
                    }
                }
            }
        }
        return menu
    }

    fun getAccessControlModelView(app:String,model:String,viewType:String):ModelView?{
        val mv=UICache.ref.getModelView(app,model,viewType)
        if(mv!=null){
            var tag="$app.$model.$viewType"
            var vRule=this.currRole?.viewRules?.get(tag)
            if(vRule!=null){
                val cloneMV = mv.createCopy()
                return this.applyViewRule(cloneMV,vRule)
            }
            return mv
        }
        return null
    }

    fun getAccessControlModelViewActionGroup(refActionGroup: ModelView.RefActionGroup):TriggerGroup?{
        var va = UICache.ref.getViewAction(refActionGroup.app,refActionGroup.model,refActionGroup.viewType,refActionGroup.groupName)
        //todo add access control
        if(va!=null){
            var tg = va.groups[refActionGroup.groupName]
            var tgRule = this.getActionGroupRule(tg,
                    refActionGroup.app,
                    refActionGroup.model,
                    refActionGroup.viewType)?.groupRules?.get(refActionGroup.groupName)
            if(tg!=null){
                if(tgRule!=null && tgRule.visible == "false") {
                        return null
                }
                var cloneTG = tg.createCopy()
                if(tgRule!=null){
                    if(tgRule.visible=="false"){
                        return null
                    }
                    tg.enable=tgRule.enable


                    tgRule.triggerRules.forEach { t, u ->
                        var t=cloneTG.triggers.firstOrNull {tit->
                            tit.name==u.name
                        }
                        if(t!=null){
                            t.enable= if(t.enable!=null && u.enable!=null) "(${t.enable}) and (${u.enable})" else t.enable?:u.enable
                            t.visible= if(t.visible!=null && u.visible!=null) "(${t.visible}) and (${u.visible})" else t.visible?:u.visible
                        }
                    }
                }
                refActionGroup.triggers.forEach { u->
                    var t=cloneTG.triggers.firstOrNull {tit->
                        tit.name==u.name
                    }
                    if(t!=null){
                        t.enable= if(t.enable!=null && u.enable!=null) "(${t.enable}) and (${u.enable})" else t.enable?:u.enable
                        t.visible= if(t.visible!=null && u.visible!=null) "(${t.visible}) and (${u.visible})" else t.visible?:u.visible
                        t.ownerField=u.ownerField
                        t.app=u.app?:t.app
                        t.actionName=u.actionName?:t.actionName
                        t.model=u.model?:t.model
                        t.title=u.title?:t.title
                        t.viewType=u.viewType?:t.viewType
                        t.meta = u.meta?:t.meta
                    }
                }
                return cloneTG
            }
            return tg
        }
        return null
    }
    fun getActionGroupRule(tg:TriggerGroup?,app:String,model:String,viewType:String):CorpPartnerRoleCache.ActionRule?{
        if(tg==null){
            return null
        }
        if(this.currRole?.actionRules!=null){
            return this.getActionGroupRuleImp(tg,app,model,viewType)
        }
        return null
    }
    private fun getActionGroupRuleImp(tg:TriggerGroup,app:String,model:String,viewType:String):CorpPartnerRoleCache.ActionRule?{
        var tag ="$app.$model.$viewType"
        var ar= this.currRole?.actionRules?.get(tag)
        if(ar!=null){
            return ar
        }
        if(app!="*" && model!="*"){
            return this.getActionGroupRuleImp(tg,"*",model,viewType)
        }
        if(app=="*" && model!="*"){
            return this.getActionGroupRuleImp(tg,"*","*",viewType)
        }
        return null
    }
    private fun applyViewRule(mv:ModelView?,rule:CorpPartnerRoleCache.ViewRule):ModelView?{
        if(mv!=null){
            if(rule.visible=="false"){
                return null
            }
            mv.enable=rule.enable
            rule.fieldRules.forEach {
               var tf= mv.fields.find{f->
                    f.name == it.name
                }
                if(tf!=null){
                    if(it.enable=="false"){
                        mv.fields.remove(tf)
                    }
                    else{
                        if(it.visible!=null){
                            if(tf.visible==null){
                                tf.visible=it.visible
                            }
                            else{
                                tf.visible="(${tf.visible}) and (${it.visible})"
                            }
                        }
                        if(it.subViewRule!=null){
                            tf.fieldView=applyViewRule(tf.fieldView,it.subViewRule as CorpPartnerRoleCache.ViewRule)
                        }
                    }
                }
            }
        }
        return mv
    }

}