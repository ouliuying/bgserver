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

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.apache.commons.logging.LogFactory
import work.bg.server.core.acrule.*
import work.bg.server.core.model.BasePartnerRoleAppMenuRule
import work.bg.server.core.model.BasePartnerRoleModelRule
import work.bg.server.core.model.BasePartnerRoleModelViewRule

class CorpPartnerRoleCache(val id:Long,val name:String,val isSuper:Boolean=false) {
    lateinit var modelCreateAccessControlRules:MutableMap<String, MutableList<ModelCreateAccessControlRule<*>>>
    lateinit var modelReadAccessControlRules:MutableMap<String,MutableList<ModelReadAccessControlRule<*>>>
    lateinit var modelEditAccessControlRules:MutableMap<String,MutableList<ModelEditAccessControlRule<*,*>>>
    lateinit var modelDeleteAccessControlRules:MutableMap<String,MutableList<ModelDeleteAccessControlRule<*,*>>>
    var appRules = mutableMapOf<String,AppRule>()
    var menuRules = mutableMapOf<String,MenuRule>()
    var viewRules = mutableMapOf<String,ViewRule>()
    private  val logger = LogFactory.getLog(javaClass)
    private  var gson = Gson()
    init {
        this.parseAcRule()
        this.cacheAcRule()
    }
    inline  fun <reified T>  getModelCreateAccessControlRules(model: ModelBase):List<T> {
        var name = model.meta.tag
        var rules = modelCreateAccessControlRules[name]
        var typeCreateRules = mutableListOf<T>()
        rules?.forEach {
            when (it) {
                is T -> {
                    typeCreateRules.add(it)
                }
            }
        }
        return typeCreateRules
    }
    inline fun <reified T> getModelReadAccessControlRules(model:ModelBase):List<T>{
        var name = model.meta.tag
        var rules = modelReadAccessControlRules[name]
        var typeReadRules = mutableListOf<T>()
        rules?.forEach {
            when (it) {
                is T -> {
                    typeReadRules.add(it)
                }
            }
        }
        return typeReadRules
    }

    inline fun <reified T> getModelEditAccessControlRules(model:ModelBase):List<T>{
        var name = model.meta.tag
        var rules = modelEditAccessControlRules[name]
        var typeEditRules = mutableListOf<T>()
        rules?.forEach {
            when (it) {
                is T -> {
                    typeEditRules.add(it)
                }
            }
        }
        return typeEditRules
    }
    inline  fun <reified T> getModelDeleteAccessControlRules(model:ModelBase):List<T>{
        var name = model.meta.tag
        var rules = modelDeleteAccessControlRules[name]
        var typeEditRules = mutableListOf<T>()
        rules?.forEach {
            when (it) {
                is T -> {
                    typeEditRules.add(it)
                }
            }
        }
        return typeEditRules
    }
    private  fun parseAcRule(){
            try {
                    this.buildAppAcRule()
                    this.buildMenuRule()
                    this.buildViewRule()
            }
            catch (ex:Exception){
                ex.printStackTrace()
                logger.error(ex.toString())
            }

    }
    private fun cacheAcRule(){
        this.modelCreateAccessControlRules= mutableMapOf()
        this.modelReadAccessControlRules = mutableMapOf()
        this.modelEditAccessControlRules= mutableMapOf()
        this.modelDeleteAccessControlRules= mutableMapOf()
        this.appRules.forEach { t, u ->
            val app=u.app
            u.modelRules.forEach { t, u ->

                val model = u.model
                u.createAction.ruleBeans.forEach {
                    var tag = "${app}.${model}"
                    var bean =  this.createCreateAccessControlRuleBean(it)
                    if(bean!=null && this.modelCreateAccessControlRules.containsKey(tag)){
                        this.modelCreateAccessControlRules[tag]?.add(bean)
                    } else if(bean!=null){
                        var beans = mutableListOf<ModelCreateAccessControlRule<*>>()
                        beans.add(bean)
                        this.modelCreateAccessControlRules[tag] =beans
                    }
                }
                u.readAction.ruleBeans.forEach {
                    var tag = "${app}.${model}"
                    var bean =  this.createReadAccessControlRuleBean(it)
                    if(bean!=null && this.modelReadAccessControlRules.containsKey(tag)){
                        this.modelReadAccessControlRules[tag]?.add(bean)
                    } else if(bean!=null){
                        var beans = mutableListOf<ModelReadAccessControlRule<*>>()
                        beans.add(bean)
                        this.modelReadAccessControlRules[tag] =beans
                    }
                }

                u.editAction.ruleBeans.forEach {
                    var tag = "${app}.${model}"
                    var bean =  this.createEditAccessControlRuleBean(it)
                    if(bean!=null && this.modelReadAccessControlRules.containsKey(tag)){
                        this.modelEditAccessControlRules[tag]?.add(bean)
                    } else if(bean!=null){
                        var beans = mutableListOf<ModelEditAccessControlRule<*,*>>()
                        beans.add(bean)
                        this.modelEditAccessControlRules[tag] =beans
                    }
                }

                u.deleteAction.ruleBeans.forEach {
                    var tag = "${app}.${model}"
                    var bean =  this.createDeleteAccessControlRuleBean(it)
                    if(bean!=null && this.modelDeleteAccessControlRules.containsKey(tag)){
                        this.modelDeleteAccessControlRules[tag]?.add(bean)
                    } else if(bean!=null){
                        var beans = mutableListOf<ModelDeleteAccessControlRule<*,*>>()
                        beans.add(bean)
                        this.modelDeleteAccessControlRules[tag] =beans
                    }
                }
            }
        }
    }
    fun getModelRule(model:ModelBase):ModelRule?{
        return this.appRules[model.meta.appName]?.modelRules?.get(model.meta.name)
    }
    private fun createCreateAccessControlRuleBean(rb: ModelRule.RuleBean):ModelCreateAccessControlRule<*>?{
        var bt = org.springframework.util.ClassUtils.forName(rb.name,this.javaClass.classLoader)
        return if(bt!=null){
            var bean=(AppModelWeb.ref.appContext?.getBean(bt) as ModelCreateAccessControlRule<*>?)
            bean?.config = rb.config
            bean
        }
        else{
            null
        }
    }
    private  fun createReadAccessControlRuleBean(rb:ModelRule.RuleBean):ModelReadAccessAttachCriterialRule<*>?{
        var bt = org.springframework.util.ClassUtils.forName(rb.name,this.javaClass.classLoader)
        return if(bt!=null){
            var bean=(AppModelWeb.ref.appContext?.getBean(bt) as ModelReadAccessAttachCriterialRule<*>?)
            bean?.config = rb.config
            bean
        }
        else{
            null
        }
    }
    private  fun createEditAccessControlRuleBean(rb:ModelRule.RuleBean):ModelEditAccessControlRule<*,*>?{
        var bt = org.springframework.util.ClassUtils.forName(rb.name,this.javaClass.classLoader)
        return if(bt!=null){
            var bean=(AppModelWeb.ref.appContext?.getBean(bt) as ModelEditAccessControlRule<*,*>?)
            bean?.config = rb.config
            bean
        }
        else{
            null
        }
    }
    private  fun createDeleteAccessControlRuleBean(rb:ModelRule.RuleBean):ModelDeleteAccessControlRule<*,*>?{
        var bt = org.springframework.util.ClassUtils.forName(rb.name,this.javaClass.classLoader)
        return if(bt!=null){
            var bean=(AppModelWeb.ref.appContext?.getBean(bt) as ModelDeleteAccessControlRule<*,*>?)
            bean?.config = rb.config
            bean
        }
        else{
            null
        }
    }

    private fun buildMenuRule(){
        var modelDatas = BasePartnerRoleAppMenuRule.ref.rawRead(criteria = eq(BasePartnerRoleAppMenuRule.ref.partnerRole,this.id))?.toModelDataObjectArray()
        modelDatas?.forEach {
            val app = it.getFieldValue(BasePartnerRoleAppMenuRule.ref.app) as String?
            val rule = it.getFieldValue(BasePartnerRoleAppMenuRule.ref.menuRule) as String?
            if(app!=null && rule!=null){
                var filterKeys = arrayListOf<String>()
                try {
                    var ruleArr = this.gson.fromJson(rule,JsonArray::class.java)
                    ruleArr?.forEach {
                        it?.let {
                            filterKeys.add(it.asString)
                        }
                    }
                    var m = MenuRule(filterKeys)
                    this.menuRules[app]=m
                }
                catch (ex:Exception){
                    ex.printStackTrace()
                    this.logger.error(ex.toString())
                }
            }
        }
    }


    private fun buildViewRule(){
        val modelDatas = BasePartnerRoleModelViewRule.ref.rawRead(criteria = eq(BasePartnerRoleModelViewRule.ref.id,this.id))?.toModelDataObjectArray()
        modelDatas?.forEach {
            var app = it.getFieldValue(BasePartnerRoleModelViewRule.ref.app) as String?
            val model  = it.getFieldValue(BasePartnerRoleModelViewRule.ref.model) as String?
            val rule = it.getFieldValue(BasePartnerRoleModelViewRule.ref.viewRule) as String?
            if(app!=null && model!=null && rule!=null){
                try {
                    var ruleJa = this.gson.fromJson(rule,JsonArray::class.java)
                    ruleJa?.forEach {rIT->
                        val rco = rIT as JsonObject
                        val enable = rco.get("enable")?.asBoolean != false
                        val viewType = rco.get("viewType")?.asString
                        var fieldRules = arrayListOf<ViewRule.FieldRule>()
                        rco.getAsJsonArray("fields")?.forEach {fIT->
                            val fco = fIT as JsonObject
                            val visible = fco.get("visible")?.asBoolean!=false
                            val exp = fco.get("exp")?.asString
                            val name = fco.get("name")?.asString
                            if(!name.isNullOrEmpty() && !name.isNullOrBlank()){
                                fieldRules.add(ViewRule.FieldRule(name,visible, exp))
                            }
                        }
                        var groupTriggerRules = arrayListOf<ViewRule.GroupTriggerRule>()
                        rco.getAsJsonArray("triggers")?.forEach {tIT->
                            val fco = tIT as JsonObject
                            val visible = fco.get("visible")?.asBoolean!=false
                            val exp = fco.get("exp")?.asString
                            val trigger = fco.get("trigger")?.asString
                            val group = fco.get("group")?.asString
                            if(!group.isNullOrEmpty() && !group.isNullOrBlank()
                                    &&!trigger.isNullOrEmpty() && !trigger.isNullOrBlank()
                            ){
                                groupTriggerRules.add(ViewRule.GroupTriggerRule(group,trigger,visible, exp))
                            }
                        }
                        if(!viewType.isNullOrEmpty() && !viewType.isNullOrBlank()){
                            val vr = ViewRule(app,model,viewType,enable,fieldRules.toTypedArray(),groupTriggerRules.toTypedArray())
                            this.viewRules["$app.$model.$viewType"] = vr
                        }
                    }
                }
                catch (ex:Exception){
                    ex.printStackTrace()
                    this.logger.error(ex.toString())
                }
            }
        }
    }

    private fun buildAppAcRule(){
        var modelRules = BasePartnerRoleModelRule.ref.rawRead(criteria = eq(BasePartnerRoleModelRule.ref.partnerRole,this.id))?.toModelDataObjectArray()
        modelRules?.forEach {
            var app = it.getFieldValue(BasePartnerRoleModelRule.ref.app) as String?
            val model = it.getFieldValue(BasePartnerRoleModelRule.ref.model) as String?
            val modelRule = it.getFieldValue(BasePartnerRoleModelRule.ref.modelRule) as String?
            if(app!=null && model!=null && modelRule!=null){
                try {
                    val appRule = this.appRules[app]?: AppRule(app)
                    val acJA = this.gson.fromJson(modelRule,JsonArray::class.java)
                    acJA?.let {
                        acJA.forEach { acIT->
                            val aco = acIT as JsonObject
                          //  [{"accessType":"read","enable":true,"isolocation":"corp","targetDepartments":[4,3,2,10,9],"disableFields":[],"targetRoles":[],"targetPartners":[],"rules":[],"criteria":"","overrideCriteria":""},{"accessType":"create","enable":true,"isolocation":"corp","targetDepartments":[],"disableFields":[],"targetRoles":[],"targetPartners":[],"rules":[],"criteria":"","overrideCriteria":""},{"accessType":"edit","enable":true,"isolocation":"corp","targetDepartments":[],"disableFields":[],"targetRoles":[],"targetPartners":[],"rules":[],"criteria":"","overrideCriteria":""},{"accessType":"delete","enable":true,"isolocation":"corp","targetDepartments":[],"targetRoles":[],"targetPartners":[],"rules":[],"criteria":"","overrideCriteria":""}]
                            aco.get("accessType")?.asString?.let {
                                accessType->
                                val enable = aco.get("enable")?.asBoolean!=false
                                val isolation = aco.get("isolation")?.asString?:"corp"
                                val filterFields = arrayListOf<String>()
                                aco.get("disableFields")?.asJsonArray?.forEach {
                                    it?.let {
                                        filterFields.add(it.asString)
                                    }
                                }
                                val targetDepartments = arrayListOf<Long>()
                                aco.get("targetDepartments")?.asJsonArray?.forEach {
                                    it?.let {
                                        try {
                                            targetDepartments.add(it.asLong)
                                        }
                                        catch (ex:Exception){
                                            ex.printStackTrace()
                                            this.logger.error(ex.toString())
                                        }
                                    }
                                }

                                val targetPartners = arrayListOf<Long>()
                                aco.get("targetDepartments")?.asJsonArray?.forEach {
                                    it?.let {
                                        try {
                                            targetPartners.add(it.asLong)
                                        }
                                        catch (ex:Exception){
                                            ex.printStackTrace()
                                            this.logger.error(ex.toString())
                                        }
                                    }
                                }

                                val targetRoles = arrayListOf<Long>()
                                aco.get("targetRoles")?.asJsonArray?.forEach {
                                    it?.let {
                                        try {
                                            targetRoles.add(it.asLong)
                                        }
                                        catch (ex:Exception){
                                            ex.printStackTrace()
                                            this.logger.error(ex.toString())
                                        }
                                    }
                                }
                                val ruleBeans = arrayListOf<ModelRule.RuleBean>()
                                aco.get("rules")?.asJsonArray?.forEach {
                                    it?.let {
                                        ruleBeans.add(ModelRule.RuleBean(it.asString,"{}"))
                                    }
                                }
                                val criteria = aco.get("criteria")?.asString?:""
                                val overrideCriteria = aco.get("overrideCriteria")?.asString?:""
                                val appModelRule = ModelRule(model)
                                when(accessType){
                                    "read"->{
                                        appModelRule.readAction = ModelRule.ReadAction(
                                                enable,
                                                filterFields,
                                                isolation,
                                                targetDepartments,
                                                targetPartners,
                                                targetRoles,
                                                criteria,
                                                overrideCriteria,
                                                ruleBeans.toTypedArray())
                                    }
                                    "create"->{
                                        appModelRule.createAction = ModelRule.CreateAction(enable,
                                                filterFields,
                                                ruleBeans.toTypedArray())
                                    }
                                    "edit"->{
                                        appModelRule.editAction = ModelRule.EditAction(enable,
                                                filterFields,
                                                isolation,
                                                targetDepartments,
                                                targetPartners,
                                                targetRoles,
                                                criteria,
                                                overrideCriteria,
                                                ruleBeans.toTypedArray())
                                    }
                                    "delete"->{
                                        appModelRule.deleteAction = ModelRule.DeleteAction(enable,
                                                isolation,
                                                targetDepartments,
                                                targetPartners,
                                                targetRoles,
                                                criteria,
                                                overrideCriteria,
                                                ruleBeans.toTypedArray())
                                    }
                                }
                                appRule.modelRules[appModelRule.model]=appModelRule
                            }
                        }
                    }
                }
                catch (ex:Exception){
                    ex.printStackTrace()
                    this.logger.error(ex.toString())
                }
            }
        }
    }

    class AppRule(val app:String){
        var modelRules = mutableMapOf<String,ModelRule>()
    }

    class ModelRule(val model:String){

        lateinit var createAction:CreateAction
        lateinit var readAction: ReadAction
        lateinit var deleteAction: DeleteAction
        lateinit var editAction: EditAction

        class RuleBean(val name:String,val config:String)
        open class ModelRuleAction(val enable:Boolean,
                                   val fields:ArrayList<String> = arrayListOf(),
                                   val isolation:String="corp",
                                   val targetDepartments:ArrayList<Long> = arrayListOf(),
                                   val targetPartners:ArrayList<Long> = arrayListOf(),
                                   val targetRoles:ArrayList<Long> = arrayListOf(),
                                   val criteria:String = "",
                                   val overrideCriteria:String="",
                                   val ruleBeans:Array<RuleBean> = arrayOf())

        class DeleteAction(enable:Boolean,
                           isolation:String,
                           targetDepartments: ArrayList<Long>,
                           targetPartners: ArrayList<Long>,
                           targetRoles: ArrayList<Long>,
                           criteria: String,
                           overrideCriteria: String,
                           ruleBeans:Array<RuleBean>):
                ModelRuleAction(enable,
                        arrayListOf(),
                        isolation,
                        targetDepartments,
                        targetPartners,
                        targetRoles,
                        criteria,
                        overrideCriteria
                        ,ruleBeans)

        class CreateAction(enable:Boolean,
                           fields: ArrayList<String>,
                           ruleBeans:Array<RuleBean>):
                ModelRuleAction(enable,
                        fields,
                        "partner",
                        arrayListOf<Long>(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "",
                        ruleBeans)

        class EditAction(enable:Boolean,
                         fields: ArrayList<String>,
                         isolation: String,
                         targetDepartments: ArrayList<Long>,
                         targetPartners:ArrayList<Long>,
                         targetRoles: ArrayList<Long>,
                         criteria: String,
                         overrideCriteria: String,
                         ruleBeans:Array<RuleBean>):
                ModelRuleAction(enable,
                        fields,
                        isolation,
                        targetDepartments,
                        targetPartners,
                        targetRoles,
                        criteria,
                        overrideCriteria,
                        ruleBeans)

        class ReadAction(enable:Boolean,
                         fields: ArrayList<String>,
                         isolation: String,
                         targetDepartments: ArrayList<Long>,
                         targetPartners:ArrayList<Long>,
                         targetRoles: ArrayList<Long>,
                         criteria: String,
                         overrideCriteria: String,
                         ruleBeans:Array<RuleBean>):
                ModelRuleAction(enable,
                        fields,
                        isolation,
                        targetDepartments,
                        targetPartners,
                        targetRoles,
                        criteria,
                        overrideCriteria,
                        ruleBeans)
    }




    class MenuRule(val filterKeys:ArrayList<String>){

    }

    class ViewRule(val app:String,
                   val model:String,
                   val type:String,
                   val enable:Boolean,
                   val fieldRules:Array<ViewRule.FieldRule> = arrayOf(),
                   val groupTriggerRules:Array<ViewRule.GroupTriggerRule> = arrayOf()){

        class FieldRule(val name:String,
                        val visible:Boolean,
                        val exp:String?){

        }
        class GroupTriggerRule(val groupName:String,
                               val triggerName:String,
                               val visible: Boolean,
                               val exp:String?)

    }

}