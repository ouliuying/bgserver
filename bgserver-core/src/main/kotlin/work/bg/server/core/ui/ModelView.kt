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

package work.bg.server.core.ui

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dynamic.model.query.mq.Many2ManyField
import dynamic.model.query.mq.Many2OneField
import dynamic.model.query.mq.One2ManyField
import dynamic.model.query.mq.One2OneField
import dynamic.model.query.mq.model.AppModel
import org.apache.commons.logging.LogFactory
import org.dom4j.dom.DOMElement

class ModelView(val app:String?,val model:String?,val viewType:String?) {
    private val logger = LogFactory.getLog(javaClass)
    var fields:ArrayList<Field> = arrayListOf()
    var refActionGroups:ArrayList<RefActionGroup> = arrayListOf()
    var refMenus:ArrayList<RefMenu> = arrayListOf()
    var refViews:ArrayList<RefView> = arrayListOf()
    var visible:String?=null
    var enable:String? = null
    var meta:JsonObject?=null
    object ViewType{
        const val CREATE = "create"
        const val LIST = "list"
        const val EDIT = "edit"
        const val DETAIL = "detail"
        const val MODEL_ACTION="modelAction"
        const val MODEL_ACTION_CONFIRM = "modelActionConfirm"
        const val EVENT_LOG_LIST = "eventLogList"
    }
    fun addField(name:String,
                 style:String,
                 rowSpan:Int,
                 colSpan:Int,
                 type:String,
                 title:String,
                 icon:String):Field{

        var f=Field(this,name,style,rowSpan,colSpan,type)
        f.title=title
        if(f.title.isNullOrEmpty()){
            if(name.indexOf(".")>-1){
                var (propertyName,toPropertyName) = name.split(".")
                val fd = AppModel.ref.getModel(app!!,model!!)?.fields?.getFieldByPropertyName(propertyName)
                f.title =if(fd?.title != null) fd.title!! else ""
            }
            else{
                val fd = AppModel.ref.getModel(app!!,model!!)?.fields?.getFieldByPropertyName(name)
                f.title =if(fd?.title != null) fd.title!! else ""
            }
        }
        f.icon=icon
        if(name.indexOf(".")>-1 || f.style==Field.Style.relation){
            try {
                val model= AppModel.ref.getModel(this.app!!,this.model!!)
                var tName=name
                if(tName.indexOf(".")<0){
                    tName += "."
                }
                var (propertyName,toPropertyName) = tName.split(".")
                f.name=propertyName
                val mField = model?.getFieldByPropertyName(propertyName)
                if(mField!=null){
                    f.relationData=when(mField){
                        is Many2ManyField ->{
                            val rModel = AppModel.ref.getModel(mField.relationModelTable!!)
                            val rField = rModel?.fields?.getField(mField.relationModelFieldName)
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)

                            if(rModel!=null && rField!=null && tModel!=null && tField!=null){

                                 RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                        rModel.meta.appName,rModel.meta.name,rField.propertyName,RelationType.Many2Many,toPropertyName)
                            }
                            else{
                                logger.error("m2m ${this.app} ${this.model} $propertyName relation failed")
                                null
                            }
                        }
                        is Many2OneField ->{
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(tModel!=null && tField!=null){
                                RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                       "","","",RelationType.Many2One,toPropertyName)
                            }
                            else{
                                logger.error("m2o ${this.app} ${this.model} $propertyName relation failed")
                                null
                            }
                        }
                        is One2ManyField ->{
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(tModel!=null && tField!=null){
                                RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                        "","","",RelationType.One2Many,toPropertyName)
                            }
                            else{
                                var errorMsg = "o2m ${this.app} ${this.model} $propertyName relation failed"
                                if(tModel==null){
                                    errorMsg= "$errorMsg targetModelTable ${mField.targetModelTable} not exist!"
                                }
                                if(tField==null){
                                    errorMsg= "$errorMsg targetModelFieldName ${mField.targetModelFieldName} not exist!"
                                }
                                this.logger.error(errorMsg)
                                null
                            }
                        }
                        is One2OneField ->{
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(tModel!=null && tField!=null){
                                RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                        "","","",if(mField.isVirtualField) RelationType.VirtualOne2One else RelationType.One2One,toPropertyName)
                            }
                            else{
                                logger.error("o2o ${this.app} ${this.model} $propertyName relation failed")
                                null
                            }
                        }
                        else-> null
                    }
                }
            }
            catch (ex:Exception){

            }
        }
        this.fields.add(f)
        return f
    }

    fun createCopy():ModelView{
        var mv=ModelView(this.app,this.model,this.viewType)
        mv.meta=meta
        var cpyChildren= arrayListOf<Field>()
        this.fields.forEach {
            cpyChildren.add(it.createCopy())
        }
        mv.fields=cpyChildren
        this.refActionGroups.forEach {
            mv.refActionGroups.add(it.createCopy())
        }
        this.refMenus.forEach {
            mv.refMenus.add(it.createCopy())
        }
        this.refViews.forEach {
            mv.refViews.add(it.createCopy())
        }
        return mv
    }
    enum class  RelationType(type:Int){
        One2One(1),
        VirtualOne2One(2),
        One2Many(3),
        Many2One(4),
        Many2Many(5)
    }
    data class RelationData(val targetApp:String,
                            val targetModel:String,
                            val targetField:String,
                            val relationApp:String,
                            val relationModel:String,
                            val relationField:String,
                            val type:RelationType,
                            val toName:String?=null)

     class Field(private val modelView:ModelView,
                 var name:String,
                 val style:String,
                 val rowSpan:Int,
                 val colSpan:Int,
                 val type:String,val subNode:DOMElement?=null){
         object Style {
             const val head="head"
             const val subHead = "subHead"
             const val normal = "normal"
             const val lable = "label"
             const val relation = "relation"
             const val one2oneView="one2oneView"
         }
         object ViewFieldType{
             const val many2OneDataSetSelect="many2OneDataSetSelect"
             const val many2ManyDataSetSelect = "many2ManyDataSetSelect"
             const val selectModelFromListView = "selectModelFromListView"
             const val multiSelect  = "multiSelect"
         }
         var title:String=""
         var icon:String=""
         var relationData:RelationData?=null
         var targetFields:Array<Field>?=null
         var fieldView:ModelView?=null
         var meta:JsonElement?=null
         var ctrlProps:JsonObject?=null
         var visible:String?=null
         var enable:String?=null
         var source:ModelViewFieldSource?=null
         fun createCopy():Field{
            val f= Field(this.modelView,name,style,rowSpan,colSpan,type)
            f.title=title
            f.icon=icon
            f.relationData=relationData
            f.fieldView=fieldView
            f.meta=meta
            f.ctrlProps=ctrlProps
            f.visible=visible
            f.enable=enable
             f.source=source
            return f
         }
         val model=this.modelView.model
         val app=this.modelView.app
         val viewType=this.modelView.viewType
    }
    class RefActionGroup(val app:String,
                         val model:String,
                         val viewType:String,
                         val groupName:String,
                         val triggers:ArrayList<RefTrigger>,
                         var refTypes:ArrayList<String> = arrayListOf()){

        fun createCopy():RefActionGroup{
            return RefActionGroup(
                    app,
                    model,
                    viewType,
                    groupName,
                    triggers,
                    refTypes
            )
        }
        class RefTrigger(val app:String?,
                         val model:String?,
                         val viewType:String?,
                         val name:String,
                         val title:String?,
                         val ownerField:String?,
                         val actionName:String?,
                         val visible:String?,
                         val icon:String?,
                         val enable:String?){
            var meta:JsonObject?=null
            override fun toString(): String {
                return "${this.app} ${this.model} ${this.viewType} ${this.ownerField} ${this.name} ${this.title} ${this.actionName}  ${this.visible} ${this.icon} ${this.enable}"
            }
        }
    }
    class RefView(val app:String,
                  val model:String,
                  val viewType:String,
                  val fieldName:String,
                  val title:String,
                  val style:String,
                  var refTypes:ArrayList<String> = arrayListOf()){
        fun createCopy():RefView{
            return RefView(
                    app,
                    model,
                    viewType,
                    fieldName,
                    title,
                    style,
                    refTypes
            )
        }
    }

    class RefMenu(val app:String,
                  val name:String,
                  var refTypes:ArrayList<String> = arrayListOf()){
        fun createCopy():RefMenu{
            return RefMenu(
                    app,
                    name,
                    refTypes
            )
        }
    }
}