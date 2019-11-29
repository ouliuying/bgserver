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

import dynamic.model.query.config.ActionType
import dynamic.model.query.exception.ModelErrorException
import dynamic.model.query.mq.*
import dynamic.model.query.mq.model.AppModel
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.web.context.ContextType
import org.apache.commons.logging.LogFactory
import work.bg.server.core.model.*
import java.util.concurrent.locks.StampedLock
import work.bg.server.core.context.ModelExpressionContext
import work.bg.server.core.ui.*
import work.bg.server.expression.AtomRuleCriteriaScanner
import java.lang.Exception
import kotlin.math.exp

class PartnerCache(partnerData:Map<String,Any?>?,
                   val partnerID:Long?,
                   val corpID:Long?,
                   val roleID:Long?,
                   val devType:Int?):ContextType {
    private  val logger = LogFactory.getLog(javaClass)
    val modelExpressionContext: ModelExpressionContext = ModelExpressionContext(partnerID, corpID, roleID,devType)
    companion object {
        private val locker=StampedLock()
        private var corps= mutableMapOf<Long,CorpCache>()
    }
    private val departmentPartnerRel by lazy {
        AppModel.ref.models?.find {
             it.meta.name == "departmentPartnerRel" && it.meta.appName=="corp"
        }
    }
    private val departmentPartnerRelPartnerField by lazy {
        val model = AppModel.ref.models?.find {
            it.meta.name == "departmentPartnerRel" && it.meta.appName=="corp"
        }
        model?.fields?.getFieldByPropertyName("partner")
    }
    private val departmentDepartmentRelDepartmentField by lazy {
        val model = AppModel.ref.models?.find {
            it.meta.name == "departmentPartnerRel" && it.meta.appName=="corp"
        }
        model?.fields?.getFieldByPropertyName("department")
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
        if(partnerData==null){
            return
        }
        var corpObject= partnerData.get("corpObject") as dynamic.model.query.mq.ModelDataObject?
        var partnerRoleObject= partnerData.get("partnerRoleObject") as dynamic.model.query.mq.ModelDataObject?
       // var roleModelsArray=partnerData?.get("roleModelArray") as ModelDataArray
        if(corpObject!=null && partnerRoleObject!=null){
            var corpCache=buildCorpCache(corpID?:0,corpObject,partnerRoleObject)
            rebuildCorp(corpCache)
        }
    }
    private  fun rebuildCorp(corpCache:CorpCache){
        var tamp= locker.writeLock()
        try {
            if(corps.containsKey(corpCache.id)){
                var cc=corps[corpCache.id]
                corpCache.roles.forEach { _, u ->
                    (cc?.roles as MutableMap)[u.id]=u}
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
        roleData["name"]= role.name
        return map
    }
    inline  fun <reified T>  getModelCreateAccessControlRules(model: ModelBase):List<T>?{
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
    //安全/超级管理员 权限
    private  fun createSafeCriteria(model:AccessControlModel):ModelExpression?{
        if(this.currRole==null){
            return eq(model.createCorpID,this.corpID)
        }
        if(this.currRole?.isSuper!=false){
            return eq(model.createCorpID,this.corpID)
        }
        return null
    }
    fun getEditAccessControlCriteria(model:AccessControlModel):ModelExpression?{
        val safeCriteria = this.createSafeCriteria(model)
        if(safeCriteria!=null){
            return safeCriteria
        }

        val roleCriteria = model.createRoleRuleCriteria(this,ActionType.EDIT)
        if(roleCriteria!=null){
            return roleCriteria
        }

        var mr = this.currRole?.getModelRule(model)
       mr?.editAction?.let {
            //maybe cached
           var criterias = mutableMapOf<String,ModelExpression>()
           this.createCustomCriteria(it.criteria,model)?.let{cIT->
               criterias.put("E",cIT)
           }
           criterias.put("A",this.createIsolationCriteria(it.isolation,model))
           this.createTargetPartnerCriteria(it.targetPartners,model)?.let {pIT->
               criterias.put("D",pIT)
           }
           this.createTargetDepartmentCriteria(it.targetDepartments,model)?.let {dIT->
               criterias.put("C",dIT)
           }
           this.createTargetRoleCriteria(it.targetRoles,model)?.let {
               rIT->
               criterias.put("B",rIT)
           }
           if(it.overrideCriteria!=null){
                this.createOverrideCriteria(it.overrideCriteria,criterias)?.let {oIT->
                    return oIT
                }
           }
           return if(criterias.count()>1) and(*criterias.values.toTypedArray()) else  criterias.values.first()
        }
        return eq(model.createPartnerID,this.partnerID)
    }
    private fun createOverrideCriteria(criteria: String,criteriaItems:Map<String,ModelExpression>):ModelExpression?{
        var c = AtomRuleCriteriaScanner().scan(criteria)
        c?.let {
            try {
                return this.buildCriteriaFromRawExpressionNode(it,
                        criteriaItems)
            }
            catch (ex:Exception){
                ex.printStackTrace()
                this.logger.error(ex.toString())
            }
        }
        return null
    }
    private fun buildCriteriaFromRawExpressionNode(expNode:AtomRuleCriteriaScanner.RawExpressionNode
                                                   ,criteriaItems:Map<String,ModelExpression>):ModelExpression?{
        if(expNode.operator.isNullOrBlank() || expNode.operator.isNullOrEmpty()){
            return if(expNode.expression.isNullOrBlank() || expNode.expression.isNullOrEmpty()){
                this.buildCriteriaFromRawExpressionNode(expNode.children.first(),criteriaItems)
            } else if(criteriaItems.containsKey(expNode.expression)){
                criteriaItems[expNode.expression]
            } else{
                throw ModelErrorException("不存在条件"+expNode.expression)
            }
        }
        else{
            val exps = arrayListOf<ModelExpression>()
            expNode.children.forEach {
                val cc = this.buildCriteriaFromRawExpressionNode(it,criteriaItems)
                cc?.let {
                    exps.add(cc)
                }
            }
            if(expNode.operator==AtomRuleCriteriaScanner.OPERATOR_AND && exps.count()>0){
                return and(*exps.toTypedArray())
            }
            else if(expNode.operator == AtomRuleCriteriaScanner.OPERATOR_OR && exps.count()>0){
                return or(*exps.toTypedArray())
            }
        }
        return null
    }
    private fun createCustomCriteria(criteria:String?,model: AccessControlModel):ModelExpression?{
        val ret = model.createModelRuleCustomCriteria(criteria,this)
        if(ret.first){
            return ret.second
        }
        criteria?.let {
            var c = AtomRuleCriteriaScanner().scan(criteria)
            c?.let {
                try{
                    return this.buildModelCriteriaFromRawExpressionNode(c,model)
                }
                catch (ex:Exception){
                    ex.printStackTrace()
                    this.logger.error(ex.toString())
                }
            }
        }
        return null
    }
    private fun buildModelCriteriaFromRawExpression(expression:String,
                                                    model:AccessControlModel):ModelExpression?{
        var field = null as FieldBase?
        var operator=""
        run {
            model.fields.getAllFields().forEach {sf->
                val p = Regex("^"+sf.value.propertyName+"([\\s=><!]+)").find(expression,0)?.groupValues
                p?.let {
                    if(p.count()>0){
                        operator=p[0].replace(" ","")
                        field=sf.value
                    }
                }
            }
        }
        field?.let {
            when(operator){
                ">"->{
                    val v = expression.substringAfter(operator)
                    val rV = ModelFieldConvert.toTypeValue(field,v)
                    rV?.let {
                        return gt(field!!,rV)
                    }
                }
                "<"->{
                    val v = expression.substringAfter(operator)
                    val rV = ModelFieldConvert.toTypeValue(field,v)
                    rV?.let {
                        return lt(field!!,rV)
                    }
                }
                "="->{
                    val v = expression.substringAfter(operator)
                    val rV = ModelFieldConvert.toTypeValue(field,v)
                    rV?.let {
                        return eq(field!!,rV)
                    }
                }
                ">="->{
                    val v = expression.substringAfter(operator)
                    val rV = ModelFieldConvert.toTypeValue(field,v)
                    rV?.let {
                        return gtEq(field!!,rV)
                    }
                }
                "<="->{
                    val v = expression.substringAfter(operator)
                    val rV = ModelFieldConvert.toTypeValue(field,v)
                    rV?.let {
                        return ltEq(field!!,rV)
                    }
                }
                "!="->{
                    val v = expression.substringAfter(operator)
                    val rV = ModelFieldConvert.toTypeValue(field,v)
                    rV?.let {
                        return notEq(field!!,rV)
                    }
                }
                else->{

                }
            }
        }
        return null
    }
    private fun buildModelCriteriaFromRawExpressionNode(expNode:AtomRuleCriteriaScanner.RawExpressionNode,
                                                        model:AccessControlModel):ModelExpression?{
        if(expNode.operator.isNullOrBlank() || expNode.operator.isNullOrEmpty()){
            return if(expNode.expression.isNullOrBlank() || expNode.expression.isNullOrEmpty()){
                this.buildModelCriteriaFromRawExpressionNode(expNode.children.first(),model)
            } else{
               this.buildModelCriteriaFromRawExpression(expNode.expression,model)
            }
        }
        else{
            val exps = arrayListOf<ModelExpression>()
            expNode.children.forEach {
                val cc = this.buildModelCriteriaFromRawExpressionNode(it,model)
                cc?.let {
                    exps.add(cc)
                }
            }
            if(expNode.operator==AtomRuleCriteriaScanner.OPERATOR_AND && exps.count()>0){
                return and(*exps.toTypedArray())
            }
            else if(expNode.operator == AtomRuleCriteriaScanner.OPERATOR_OR && exps.count()>0){
                return or(*exps.toTypedArray())
            }
        }
        return null
    }
    private fun createIsolationCriteria(isolation:String,model:AccessControlModel):ModelExpression{
        val ret = model.createModelRuleIsolationCriteria(isolation,this)
        if(ret.first){
            return ret.second?:eq(model.createPartnerID,this.partnerID)
        }
        return if(isolation=="corp"){
            eq(model.createCorpID,this.corpID)
        }
        else{
            eq(model.createPartnerID,this.partnerID)
        }
    }
    private fun createTargetPartnerCriteria(targetPartners:ArrayList<Long>,model:AccessControlModel):ModelExpression?{
        val ret = model.createModelRuleTargetPartnerCriteria(targetPartners,this)
        if(ret.first){
            return ret.second
        }
        if(targetPartners.count()<1){
            return null
        }
        var tps = targetPartners.clone() as ArrayList<Long>
        this.partnerID?.let {
            tps.add(it)
        }
        return `in`(model.createPartnerID,tps.toArray())
    }
    private fun createTargetDepartmentCriteria(targetDepartments:ArrayList<Long>,
                                               model:AccessControlModel):ModelExpression?{
        val ret = model.createModelRuleTargetDepartmentCriteria(targetDepartments,this)
        if(ret.first){
            return ret.second
        }

        if(targetDepartments.count()<1){
            return null
        }
        if(this.departmentPartnerRel!=null &&
                this.departmentPartnerRelPartnerField!=null &&
                this.departmentDepartmentRelDepartmentField!=null){
            val subSelect  = select(this.departmentPartnerRelPartnerField!!,fromModel = this.departmentPartnerRel!!).where(
                    `in`(this.departmentDepartmentRelDepartmentField!!,targetDepartments.toArray())
            )
            return or(eq(model.createPartnerID,this.partnerID),`in`(model.createPartnerID, subSelect))
        }
       return null
    }
    private fun createTargetRoleCriteria(targetRoles:ArrayList<Long>,
                                         model:AccessControlModel):ModelExpression?{
        val ret = model.createModelRuleTargetRoleCriteria(targetRoles,this)
        if(ret.first){
            return ret.second
        }
        if(targetRoles.count()<1){
            return null
        }

        val subSelect  = select(BaseCorpPartnerRel.ref.partner,fromModel = BaseCorpPartnerRel.ref).where(
                `in`(BaseCorpPartnerRel.ref.partnerRole,targetRoles.toArray())
        )
        return or(eq(model.createPartnerID,this.partnerID),`in`(model.createPartnerID, subSelect))
    }

    fun getReadAccessControlCriteria(model:AccessControlModel):ModelExpression?{

        val safeCriteria = this.createSafeCriteria(model)
        if(safeCriteria!=null){
            return safeCriteria
        }

        val roleCriteria = model.createRoleRuleCriteria(this,ActionType.READ)
        if(roleCriteria!=null){
            return roleCriteria
        }

        var mr = this.currRole?.getModelRule(model)
        mr?.readAction?.let {
            //maybe cached
            var criterias = mutableMapOf<String,ModelExpression>()
            this.createCustomCriteria(it.criteria,model)?.let{cIT->
                criterias.put("E",cIT)
            }
            criterias.put("A",this.createIsolationCriteria(it.isolation,model))
            this.createTargetPartnerCriteria(it.targetPartners,model)?.let {pIT->
                criterias.put("D",pIT)
            }
            this.createTargetDepartmentCriteria(it.targetDepartments,model)?.let {dIT->
                criterias.put("C",dIT)
            }
            this.createTargetRoleCriteria(it.targetRoles,model)?.let {
                rIT->
                criterias.put("B",rIT)
            }
            if(it.overrideCriteria!=null){
                this.createOverrideCriteria(it.overrideCriteria,criterias)?.let {oIT->
                    return oIT
                }
            }
            return if(criterias.count()>1) and(*criterias.values.toTypedArray()) else  criterias.values.first()
        }
        return eq(model.createPartnerID,this.partnerID)
    }

    fun getDeleteAccessControlCriteria(model:AccessControlModel):ModelExpression?{
        val safeCriteria = this.createSafeCriteria(model)
        if(safeCriteria!=null){
            return safeCriteria
        }

        val roleCriteria = model.createRoleRuleCriteria(this,ActionType.DELETE)
        if(roleCriteria!=null){
            return roleCriteria
        }
        var mr = this.currRole?.getModelRule(model)
        mr?.deleteAction?.let {
            //maybe cached
            var criterias = mutableMapOf<String,ModelExpression>()
            this.createCustomCriteria(it.criteria,model)?.let{cIT->
                criterias.put("E",cIT)
            }
            criterias.put("A",this.createIsolationCriteria(it.isolation,model))
            this.createTargetPartnerCriteria(it.targetPartners,model)?.let {pIT->
                criterias.put("D",pIT)
            }
            this.createTargetDepartmentCriteria(it.targetDepartments,model)?.let {dIT->
                criterias.put("C",dIT)
            }
            this.createTargetRoleCriteria(it.targetRoles,model)?.let {
                rIT->
                criterias.put("B",rIT)
            }
            if(it.overrideCriteria!=null){
                this.createOverrideCriteria(it.overrideCriteria,criterias)?.let {oIT->
                    return oIT
                }
            }
            return if(criterias.count()>1) and(*criterias.values.toTypedArray()) else  criterias.values.first()
        }
        return eq(model.createPartnerID,this.partnerID)
    }

    fun getCreateAccessControlCriteria(model:AccessControlModel):ModelExpression?{
        return null
    }

    fun getAcModelFields(fields:Array<FieldBase>,
                         model:AccessControlModel,
                         acType:ActionType):Array<FieldBase>{
        var mr = this.currRole?.getModelRule(model)
        mr?.let {
            when (acType) {
                ActionType.CREATE -> {
                    return this.removeTargetFields(fields,model,it.readAction.fields)
                }
                ActionType.EDIT -> {
                    return this.removeTargetFields(fields,model,it.editAction.fields)
                }
                ActionType.READ -> {
                    return this.removeTargetFields(fields,model,it.readAction.fields)
                }
                else->{

                }
            }
        }
        return fields
    }
    fun checkACModelOwnerRelation(model: AccessControlModel,acType: ActionType):Boolean{
        if(this.currRole?.isSuper!=false){
            return false
        }
        var modelRule  = this.currRole?.getModelRule(model)
        modelRule?.let {
            when(acType){
                ActionType.EDIT->{
                    return it.editAction.isolation!="corp"
                }
                ActionType.READ->{
                    return it.readAction.isolation!="corp"
                }
                ActionType.DELETE->{
                    return it.deleteAction.isolation!="corp"
                }
                ActionType.CREATE->{
                    return it.createAction.isolation!="corp"
                }
            }
        }
        return true
    }
    fun canReadModelField(field:FieldBase,
                          model:AccessControlModel):Boolean{
        val mr = this.currRole?.getModelRule(model)
        if(mr!=null && field.propertyName in mr.readAction.fields){
            return false
        }
        return true
    }

    private fun removeTargetFields(targetFields:Array<FieldBase>,
                                   model:AccessControlModel,
                                   rmFields: ArrayList<String>):Array<FieldBase>{
        if(rmFields.count()<1){
            return targetFields
        }
        var res = arrayListOf<FieldBase>()
        targetFields.forEach {
            if(it.propertyName !in rmFields){
                res.add(it)
            }
        }
        return res.toTypedArray()
    }
    private  fun buildCorpCache(corpID:Long,
                                corpObject: dynamic.model.query.mq.ModelDataObject,
                                partnerRoleObject: dynamic.model.query.mq.ModelDataObject):CorpCache{
        var roleID=partnerRoleObject.data.getValue(BasePartnerRole.ref.id) as Long
        var roleName=partnerRoleObject.data.getValue(BasePartnerRole.ref.name) as String
        var isSuper=(partnerRoleObject.data.getValue(BasePartnerRole.ref.isSuper) as Int)>0
        var name=corpObject.data.getValue(BaseCorp.ref.name) as String
        var  role=CorpPartnerRoleCache(roleID,roleName,isSuper)
        return CorpCache(corpID,name, mutableMapOf(role.id to role))
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
                        if(rv.enable){
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
            var mr = this.currRole?.menuRules?.get(app)
            if(mr!=null){
                ruleMenu = this.applyMenuRule(ruleMenu,mr) as MenuTree?
            }
            return ruleMenu
        }
        return null
    }

    private fun applyMenuRule(menu: MenuNode?, rule:CorpPartnerRoleCache.MenuRule):MenuNode?{
        if(menu==null){
            return null
        }
        if(menu!!.key in rule.filterKeys){
            return null
        }
        var index = menu.children?.indexOfFirst {
            it.key in rule.filterKeys
        }
        while (index!=null && index>-1){
            menu.children?.removeAt(index)
            index = menu.children?.indexOfFirst {
                it.key in rule.filterKeys
            }
        }
        menu.children?.forEach {
            this.applyMenuRule(it,rule)
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
                    refActionGroup.viewType)
            if(tg!=null){
                var cloneTG = tg.createCopy()
                var rmTriggers = arrayListOf<Trigger>()
                tgRule?.forEach { u->
                    var t=cloneTG.triggers.firstOrNull {tit->
                        tit.name==u.triggerName
                    }
                    if(t!=null){
                        if(!u.visible){
                           rmTriggers.add(t)
                        }
                        else{
                            t.visible= if(t.visible!=null && u.exp!=null) "(${t.visible}) and (${u.exp})" else t.visible?:u.exp
                        }
                    }
                }
                cloneTG.triggers.removeAll(rmTriggers)
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
                        t.icon = u.icon?:t.icon
                    }
                }
                return cloneTG
            }
            return tg
        }
        return null
    }
    private fun getActionGroupRule(tg:TriggerGroup?,app:String,model:String,viewType:String):List<CorpPartnerRoleCache.ViewRule.GroupTriggerRule>?{
        if(tg==null){
            return null
        }
        if(this.currRole?.viewRules!=null){
            return this.getActionGroupRuleImp(tg,app,model,viewType)
        }
        return null
    }
    private fun getActionGroupRuleImp(tg:TriggerGroup,app:String,model:String,viewType:String):List<CorpPartnerRoleCache.ViewRule.GroupTriggerRule>?{
        var tag ="$app.$model.$viewType"
        var ar= this.currRole?.viewRules?.get(tag)
        if(ar!=null){
            return ar?.groupTriggerRules?.filter {
                it.groupName == tg.name
            }.toList()
        }
        return null
    }

    private fun applyViewRule(mv:ModelView?,
                              rule:CorpPartnerRoleCache.ViewRule):ModelView?{
        if(mv!=null){
            if(!rule.enable){
                return null
            }
            rule.fieldRules.forEach {
               var tf= mv.fields.find{f->
                    f.name == it.name
                }
                if(tf!=null){
                    if(!it.visible){
                        mv.fields.remove(tf)
                    }
                    else{
                        if(it.exp!=null){
                            if(tf.visible==null){
                                tf.visible=it.exp
                            }
                            else{
                                tf.visible="(${tf.visible}) and (${it.exp})"
                            }
                        }
                    }
                }
            }
        }
        return mv
    }
}