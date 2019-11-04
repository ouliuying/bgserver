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

import dynamic.model.query.mq.model.ModelBase
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.apache.commons.logging.LogFactory
import org.dom4j.DocumentHelper
import org.dom4j.Element
import work.bg.server.core.acrule.*

class CorpPartnerRoleCache(val id:Long,val name:String,val isSuper:Boolean=false,val acRule:String?=null) {
    lateinit var modelCreateAccessControlRules:MutableMap<String, MutableList<ModelCreateAccessControlRule<*>>>
    lateinit var modelReadAccessControlRules:MutableMap<String,MutableList<ModelReadAccessControlRule<*>>>
    lateinit var modelEditAccessControlRules:MutableMap<String,MutableList<ModelEditAccessControlRule<*,*>>>
    lateinit var modelDeleteAccessControlRules:MutableMap<String,MutableList<ModelDeleteAccessControlRule<*,*>>>
    var appRules = mutableMapOf<String,AppRule>()
    var menuRules = mutableMapOf<String,MenuRule>()
    var viewRules = mutableMapOf<String,ViewRule>()
    var actionRules = mutableMapOf<String,ActionRule>()
    private  val logger = LogFactory.getLog(javaClass)
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
        if(!this.acRule.isNullOrEmpty() && !this.acRule.isNullOrBlank()){
            try {

                var doc= DocumentHelper.parseText(this.acRule)
                var acRule=doc.rootElement.element("acrule")
                var ui= doc.rootElement.element("ui")
                acRule.selectNodes("app").forEach {
                    this.buildAppAcRule(it as Element)
                }
                ui.selectNodes("actions/action").forEach {
                    this.buildActionRule(it as Element)
                }
                ui.selectNodes("menus/menu").forEach {
                    this.buildMenuRule(it as Element)
                }
                ui.selectNodes("views/view").forEach {
                    this.buildViewRule(it as Element)
                }
            }
            catch (ex:Exception){
                ex.printStackTrace()
                logger.error(ex.toString())
            }
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
    private fun buildActionRule(actionElem:Element){
        val app = actionElem.attributeValue("app")
        var model = actionElem.attributeValue("model")
        var viewType = actionElem.attributeValue("viewType")
        if(!app.isNullOrEmpty() && !model.isNullOrEmpty() && !viewType.isNullOrEmpty()){
            var groups = mutableMapOf<String,ActionRule.GroupRule>()
            actionElem.elements("group").forEach {
                var gElem = it as Element
                var enable = (gElem.selectSingleNode("setting/enable") as Element?)?.textTrim
                var visible = (gElem.selectSingleNode("setting/visible") as Element?)?.textTrim
                var gName = gElem.attributeValue("name")
                var triggers =  mutableMapOf<String,ActionRule.TriggerRule>()
                gElem.elements("trigger").forEach { tIT->
                    var tElem = tIT as Element
                    var tName=tElem.attributeValue("name")
                    var tApp=tElem.attributeValue("app")?:app
                    var tModel=tElem.attributeValue("model")?:app
                    var tViewType=tElem.attributeValue("viewType")?:app
                    var enable = (tElem.selectSingleNode("setting/enable") as Element?)?.textTrim
                    var visible = (tElem.selectSingleNode("setting/visible") as Element?)?.textTrim

                    triggers[tName]=ActionRule.TriggerRule(tApp,tModel,tViewType,tName,enable,visible)
                }
                var g=ActionRule.GroupRule(name,enable,visible,triggers)
                groups[gName]=g
            }
            var aRule = ActionRule(app,model,viewType,groups)
            var tag="$app.$model.$viewType"
            this.actionRules[tag]= aRule
        }

    }
    private fun buildMenuRule(menuElem:Element){
        val app = menuElem.attributeValue("app")
        menuElem.elements().forEach {
            var menuItem = it as Element
            var menuType = menuElem.name
            if(menuType.compareTo("menu", true)==0){
                val m = this.buildSubMenuRule(menuItem,app)
                if(m!=null){
                    var tag = "${m.app}.${m.name}"
                    this.menuRules[tag]=m
                }
            }
        }
    }

    private fun buildMenuItemRule(menuItemElem:Element,app:String):MenuRule?{
        var cApp = menuItemElem.attributeValue("app")?:app
        val cModel = menuItemElem.attributeValue("model")
        val cViewType = menuItemElem.attributeValue("viewType")
        val visible= (menuItemElem.selectSingleNode("setting/visible") as Element?)?.textTrim
        if(cApp.isNullOrEmpty()
                || cModel.isNullOrEmpty()
                || cViewType.isNullOrEmpty()){
            return null
        }
        var sm = MenuRule(cModel,cViewType,cApp,visible)

        menuItemElem.elements().forEach {
            var menuItem = it as Element
            var menuType = menuItem.name
            if(menuType.compareTo("menuItem",true)==0){
                val mi=this.buildMenuItemRule(menuItem,cApp)
                if(mi!=null){
                    sm.children.add(mi)
                }
            }
            else if(menuType.compareTo("menu", true)==0){
                val m = this.buildSubMenuRule(menuItem,cApp)
                if(m!=null){
                    sm.children.add(m)
                }
            }
        }
        return sm
    }
    private  fun buildSubMenuRule(menuElem:Element,app:String):MenuRule?{
        var cApp = menuElem.attributeValue("app")?:app
        val cName = menuElem.attributeValue("name")
        val visible= (menuElem.selectSingleNode("setting/visible") as Element?)?.textTrim
        if(cApp.isNullOrEmpty()
                || cName.isNullOrEmpty()){
            return null
        }
        var sm = MenuRule(cName,cApp,visible)

        menuElem.elements().forEach {
            var menuItem = it as Element
            var menuType = menuItem.name
            if(menuType.compareTo("menuItem",true)==0){
                val mi=this.buildMenuItemRule(menuItem,app)
                if(mi!=null){
                    sm.children.add(mi)
                }
            }
            else if(menuType.compareTo("menu", true)==0){
                val m = this.buildSubMenuRule(menuItem,app)
                if(m!=null){
                    sm.children.add(m)
                }
            }
        }
        return sm
    }
    private fun buildViewRule(viewElem:Element){
        var vr = this.buildViewRuleImp(viewElem)
        if(vr!=null){
            this.viewRules["${vr.app}.${vr.model}.${vr.type}"]=vr
        }
    }
    private fun buildViewRuleImp(viewElem:Element):ViewRule?{
        val app = viewElem.attributeValue("app")
        val model =viewElem.attributeValue("model")
        val type = viewElem.attributeValue("type")
        if(app.isNullOrEmpty() || model.isNullOrEmpty() || type.isNullOrEmpty()){
            return null
        }
        val enable = (viewElem.selectSingleNode("setting/enable") as Element?)?.textTrim
        val visible =  (viewElem.selectSingleNode("setting/visible") as Element?)?.textTrim
        var fieldRules = arrayListOf<ViewRule.FieldRule>()
        viewElem.selectNodes("fields/field").forEach {
            val fieldElem = it as Element
            val name = fieldElem.attributeValue("name")
            val enable = (fieldElem.selectSingleNode("setting/enable") as Element?)?.textTrim
            val visible =  (fieldElem.selectSingleNode("setting/visible") as Element?)?.textTrim
            if(!name.isNullOrEmpty()){
                var fr = ViewRule.FieldRule(name,enable,visible)
                fieldRules.add(fr)
                val subViewRuleElem = fieldElem.selectSingleNode("view") as Element?
                if(subViewRuleElem!=null){
                    fr.subViewRule=this.buildViewRuleImp(subViewRuleElem)
                }
            }
        }
        return ViewRule(app,model,type,enable,visible,fieldRules.toTypedArray())
    }

    private fun buildAppAcRule(appElement:Element){
        val app = appElement.attributeValue("name")
        var appRule=AppRule(app)
        appElement.selectNodes("models/model").forEach {
            val modelElem = it as Element
            val model=modelElem.attributeValue("name")
            val createElem = modelElem.selectSingleNode("setting/create")
            val readElem = modelElem.selectSingleNode("setting/read")
            val editElem = modelElem.selectSingleNode("setting/edit")
            val deleteElem = modelElem.selectSingleNode("setting/delete")
            var modelRule = ModelRule(model)
            val createEnable = (createElem?.selectSingleNode("enable") as Element?)?.textTrim
            val createCheckBelongToPartner = if((createElem?.selectSingleNode("checkBelongTo") as Element?)?.textTrim=="0") 0 else 1
            var ruleBeans = arrayListOf<ModelRule.RuleBean>()
            createElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.attributeValue("config")
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.createAction = ModelRule.CreateAction(createEnable,createCheckBelongToPartner,ruleBeans.toTypedArray())

            val editEnable = (editElem?.selectSingleNode("enable") as Element?)?.textTrim
            val editCheckBelongToPartner = if((editElem?.selectSingleNode("checkBelongTo") as Element?)?.textTrim=="0") 0 else 1
            ruleBeans = arrayListOf<ModelRule.RuleBean>()
            editElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.attributeValue("config")
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.editAction = ModelRule.EditAction(editEnable,editCheckBelongToPartner,ruleBeans.toTypedArray())


            val readEnable = (readElem?.selectSingleNode("enable") as Element?)?.textTrim
            val readCheckBelongToPartner = if((editElem?.selectSingleNode("checkBelongTo") as Element?)?.textTrim=="0") 0 else 1
            ruleBeans = arrayListOf<ModelRule.RuleBean>()
            readElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.attributeValue("config")
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.readAction = ModelRule.ReadAction(readEnable,readCheckBelongToPartner,ruleBeans.toTypedArray())

            val deleteEnable = (deleteElem?.selectSingleNode("enable") as Element?)?.textTrim
            val deleteCheckBelongToPartner = if((editElem?.selectSingleNode("checkBelongTo") as Element?)?.textTrim=="0") 0 else 1
            ruleBeans = arrayListOf<ModelRule.RuleBean>()
            deleteElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.textTrim
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.deleteAction = ModelRule.DeleteAction(deleteEnable,deleteCheckBelongToPartner,ruleBeans.toTypedArray())

            modelElem.selectNodes("fields/field").forEach {
                val fieldElem = it as Element
                val field = fieldElem.attributeValue("name")
                var fr= ModelRule.FieldRule(field)
                val readElem=fieldElem.selectSingleNode("setting/read")
                var enable = (readElem?.selectSingleNode("enable") as Element?)?.textTrim
                fr.readAction= ModelRule.FieldRule.ReadAction(enable)

                val editElem=fieldElem.selectSingleNode("setting/edit")
                enable = (editElem?.selectSingleNode("enable") as Element?)?.textTrim
               // var default = (editElem?.selectSingleNode("default") as Element?)?.textTrim
                var setValue =  (editElem?.selectSingleNode("setValue") as Element?)?.textTrim
              //  visible = if((editElem?.selectSingleNode("visible") as Element?)?.textTrim=="1") 1 else 0
                fr.editAction=ModelRule.FieldRule.EditAction(enable,setValue)

                val deleteElem=fieldElem.selectSingleNode("setting/delete")
                enable = (deleteElem?.selectSingleNode("enable") as Element?)?.textTrim
              //  visible = if((deleteElem?.selectSingleNode("visible") as Element?)?.textTrim=="1") 1 else 0
                fr.deleteAction= ModelRule.FieldRule.DeleteAction(enable)

                val createElem=fieldElem.selectSingleNode("setting/create")
                enable = (createElem?.selectSingleNode("enable") as Element?)?.textTrim
                val default = (readElem?.selectSingleNode("default") as Element?)?.textTrim
                setValue =  (readElem?.selectSingleNode("setValue") as Element?)?.textTrim
               // visible = if((createElem?.selectSingleNode("visible") as Element?)?.textTrim=="1") 1 else 0
                fr.createAction=ModelRule.FieldRule.CreateAction(enable,default,setValue)
                modelRule.fieldRules[fr.field]=fr
            }
            appRule.modelRules[modelRule.model]=modelRule
        }
        this.appRules[appRule.app]=appRule
    }

    class AppRule(val app:String){
        var modelRules = mutableMapOf<String,ModelRule>()
    }

    class ModelRule(val model:String){

        var fieldRules = mutableMapOf<String,FieldRule>()
        lateinit var createAction:CreateAction
        lateinit var readAction: ReadAction
        lateinit var deleteAction: DeleteAction
        lateinit var editAction: EditAction

        class RuleBean(val name:String,val config:String)
        open class ModelRuleAction(val enable:String?,
                                   val checkBelongToPartner:Int=1,
                                   val ruleBeans:Array<RuleBean> = arrayOf())

        class DeleteAction(enable:String?,checkBelongToPartner:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,checkBelongToPartner,ruleBeans)
        class CreateAction(enable:String?,checkBelongToPartner:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,checkBelongToPartner,ruleBeans)
        class EditAction(enable:String?,checkBelongToPartner:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,checkBelongToPartner,ruleBeans)
        class ReadAction(enable:String?,checkBelongToPartner:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,checkBelongToPartner,ruleBeans)


        class FieldRule(val field:String){
            lateinit var createAction : CreateAction
            lateinit var readAction : ReadAction
            lateinit var deleteAction : DeleteAction
            lateinit var editAction : EditAction



            open class FieldRuleAction(val enable:String?, val default:String?=null, val setValue:String?=null)
            class DeleteAction(enable:String?):FieldRuleAction(enable)
            class CreateAction(enable:String?, default: String?,setValue: String?):FieldRuleAction(enable,default,setValue)
            class EditAction(enable:String?,setValue:String?):FieldRuleAction(enable,setValue=setValue)
            class ReadAction(enable:String?):FieldRuleAction(enable)
        }

    }


    class  ActionRule(val app:String,val model:String,val viewType:String,val groupRules:Map<String,GroupRule> = mutableMapOf()){
        class GroupRule(val name:String,val enable:String?,val visible:String?, val triggerRules:Map<String,TriggerRule> = mutableMapOf())
        class TriggerRule(val app:String,val model:String,val viewType:String,val name:String,val enable:String?,val visible:String?)
    }

    class MenuRule(val name:String,val model:String,val viewType:String,val app:String,val type:MenuType,val visible:String?){
        constructor(model:String,viewType:String,app:String,visible:String?):this("",model,viewType,app,MenuType.MENU_ITEM,visible)
        constructor(name:String,app:String,visible:String?):this(name,"","",app,MenuType.MENU,visible)
        var children:ArrayList<MenuRule> = arrayListOf()

        enum class MenuType(val type:Int){
            MENU_ITEM(0),
            MENU(1),
        }
    }

    class ViewRule(val app:String,val model:String,val type:String,val enable:String?,val visible:String?,
                   val fieldRules:Array<ViewRule.FieldRule> = arrayOf()){

        class FieldRule(val name:String,val enable:String?,val visible:String?){
            var subViewRule:ViewRule?=null
        }

    }

}