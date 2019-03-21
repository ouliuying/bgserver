/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  * GNU Lesser General Public License Usage
 *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  * General Public License version 3 as published by the Free Software
 *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  * project of this file. Please review the following information to
 *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *
 *
 */

package work.bg.server.core.cache

import com.sun.javafx.collections.ElementObservableListDecorator
import org.apache.commons.logging.LogFactory
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.io.SAXReader
import work.bg.server.core.acrule.ModelCreateAccessControlRule
import work.bg.server.core.mq.ModelBase
import work.bg.server.core.spring.boot.model.AppModel
import java.awt.MenuItem

class CorpPartnerRoleCache(val id:Long,val name:String,val isSuper:Boolean=false,val acRule:String?=null) {
    lateinit var modelCreateAccessControlRules:MutableMap<String, MutableList<ModelCreateAccessControlRule<*>>>
    var appRules = mutableMapOf<String,AppRule>()
    var menuRules = mutableMapOf<String,MenuRule>()
    var viewRules = mutableMapOf<String,ViewRule>()
    var actionRules = mutableMapOf<String,ActionRule>()
    private  val logger = LogFactory.getLog(javaClass)
    init {
        this.parseAcRule()
        this.cacheAcRule()
    }
    inline  fun <reified T>  getModelCreateAccessControlRules(model:ModelBase):List<T> {
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
    private  fun parseAcRule(){
        if(this.acRule!=null){
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
                logger.error(ex.toString())
            }
        }
    }
    private fun cacheAcRule(){
        this.modelCreateAccessControlRules= mutableMapOf()
        this.appRules.forEach { t, u ->
            val app=u.app
            u.modelRules.forEach { t, u ->

                val model = u.model
                u.createAction.ruleBeans?.forEach {
                    var tag = "${app}.${model}"
                    var bean =  this.createCreateAccessControlRuleBean(it)
                    if(bean!=null && this.modelCreateAccessControlRules.containsKey(tag)){
                        this.modelCreateAccessControlRules[tag]?.add(bean)
                    }
                    else if(bean!=null){
                        var beans = mutableListOf<ModelCreateAccessControlRule<*>>()
                        beans.add(bean)
                        this.modelCreateAccessControlRules[tag] =beans
                    }
                }
            }
        }
    }

    private fun createCreateAccessControlRuleBean(rb: ModelRule.RuleBean):ModelCreateAccessControlRule<*>?{
        var bt = org.springframework.util.ClassUtils.forName(rb.name,this.javaClass.classLoader)
        return if(bt!=null){
            var bean=(AppModel.ref.appContext?.getBean(bt) as ModelCreateAccessControlRule<*>?)
            bean?.config = rb.config
            bean
        }
        else{
            null
        }
    }

    private fun buildActionRule(actionElem:Element){

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
        val visible= if ((menuItemElem.selectSingleNode("setting/visible") as Element?)?.textTrim=="0") 0 else 1
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
        val visible= if ((menuElem.selectSingleNode("setting/visible") as Element?)?.textTrim=="0") 0 else 1
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
        val enable = if((viewElem.selectSingleNode("setting/enable") as Element?)?.textTrim=="0") 0 else 1
        val visible =  if((viewElem.selectSingleNode("setting/visible") as Element?)?.textTrim=="0") 0 else 1
        var fieldRules = arrayListOf<ViewRule.FieldRule>()
        viewElem.selectNodes("fields/field").forEach {
            val fieldElem = it as Element
            val name = fieldElem.attributeValue("name")
            val enable = if((fieldElem.selectSingleNode("setting/enable") as Element?)?.textTrim=="0") 0 else 1
            val visible =  if((fieldElem.selectSingleNode("setting/visible") as Element?)?.textTrim=="0") 0 else 1
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
            val createEnable = if((createElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
            var ruleBeans = arrayListOf<ModelRule.RuleBean>()
            createElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.attributeValue("config")
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.createAction = ModelRule.CreateAction(createEnable,ruleBeans.toTypedArray())

            val editEnable = if((editElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
            ruleBeans = arrayListOf<ModelRule.RuleBean>()
            editElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.attributeValue("config")
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.editAction = ModelRule.EditAction(editEnable,ruleBeans.toTypedArray())


            val readEnable = if((readElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
            ruleBeans = arrayListOf<ModelRule.RuleBean>()
            readElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.attributeValue("config")
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.readAction = ModelRule.ReadAction(readEnable,ruleBeans.toTypedArray())

            val deleteEnable = if((deleteElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
            ruleBeans = arrayListOf<ModelRule.RuleBean>()
            deleteElem.selectNodes("rules/rule/bean").forEach {
                val beanElem = it as Element
                val name = beanElem.attributeValue("name")
                val config = beanElem.textTrim
                ruleBeans.add(ModelRule.RuleBean(name,config))
            }
            modelRule.deleteAction = ModelRule.DeleteAction(deleteEnable,ruleBeans.toTypedArray())

            modelElem.selectNodes("fields/field").forEach {
                val fieldElem = it as Element
                val field = fieldElem.attributeValue("name")
                var fr= ModelRule.FieldRule(field)
                val readElem=fieldElem.selectSingleNode("setting/read")
                var enable = if((readElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
                fr.readAction= ModelRule.FieldRule.ReadAction(enable)

                val editElem=fieldElem.selectSingleNode("setting/edit")
                enable = if((editElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
               // var default = (editElem?.selectSingleNode("default") as Element?)?.textTrim
                var setValue =  (editElem?.selectSingleNode("setValue") as Element?)?.textTrim
              //  visible = if((editElem?.selectSingleNode("visible") as Element?)?.textTrim=="1") 1 else 0
                fr.editAction=ModelRule.FieldRule.EditAction(enable,setValue)

                val deleteElem=fieldElem.selectSingleNode("setting/delete")
                enable = if((deleteElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
              //  visible = if((deleteElem?.selectSingleNode("visible") as Element?)?.textTrim=="1") 1 else 0
                fr.deleteAction= ModelRule.FieldRule.DeleteAction(enable)

                val createElem=fieldElem.selectSingleNode("setting/create")
                enable = if((createElem?.selectSingleNode("enable") as Element?)?.textTrim=="0") 0 else 1
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

        class RuleBean(val name:String,val config:String){

        }
        open class ModelRuleAction(val enable:Int,
                                   val ruleBeans:Array<RuleBean> = arrayOf()){

        }
        class DeleteAction(enable:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,ruleBeans){

        }
        class CreateAction(enable:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,ruleBeans){

        }
        class EditAction(enable:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,ruleBeans){

        }
        class ReadAction(enable:Int,ruleBeans:Array<RuleBean>):ModelRuleAction(enable,ruleBeans){

        }


        class FieldRule(val field:String){
            lateinit var createAction : CreateAction
            lateinit var readAction : ReadAction
            lateinit var deleteAction : DeleteAction
            lateinit var editAction : EditAction



            open class FieldRuleAction(val enable:Int,val defalut:String?=null,val setValue:String?=null){

            }
            class DeleteAction(enable:Int):FieldRuleAction(enable){

            }
            class CreateAction(enable:Int, default: String?,setValue: String?):FieldRuleAction(enable,default,setValue){

            }
            class EditAction(enable:Int,setValue:String?):FieldRuleAction(enable,setValue=setValue){

            }
            class ReadAction(enable:Int):FieldRuleAction(enable){

            }
        }

    }


    class  ActionRule{

    }

    class MenuRule(val name:String,val model:String,val viewType:String,val app:String,val type:MenuType,val visible:Int){
        constructor(model:String,viewType:String,app:String,visible:Int):this("",model,viewType,app,MenuType.MENU_ITEM,visible)
        constructor(name:String,app:String,visible:Int):this(name,"","",app,MenuType.MENU,visible)
        var children:ArrayList<MenuRule> = arrayListOf()

        enum class MenuType(val type:Int){
            MENU_ITEM(0),
            MENU(1),
        }
    }

    class ViewRule(val app:String,val model:String,val type:String,val enable:Int,val visible:Int,
                   val fieldRules:Array<ViewRule.FieldRule> = arrayOf()){

        class FieldRule(val name:String,val enable:Int,val visible:Int){
            var subViewRule:ViewRule?=null
        }

    }

}