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

package work.bg.server.core.ui

import com.google.gson.JsonObject
import org.dom4j.dom.DOMElement
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.model.AppModel

class ModelView(val app:String?,val model:String?,val viewType:String?) {
    var fields:ArrayList<Field> = arrayListOf()
    var refActionGroups:ArrayList<RefActionGroup> = arrayListOf()
    var refMenus:ArrayList<RefMenu> = arrayListOf()
    var refViews:ArrayList<RefView> = arrayListOf()
    var visible=1
    var enable = 1
    var meta:JsonObject?=null
    object ViewType{
        const val CREATE = "create"
        const val LIST = "list"
        const val EDIT = "edit"
        const val DETAIL = "detail"

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
                f.title =if(fd?.title != null) fd!!.title!! else ""
            }
            else{
                val fd = AppModel.ref.getModel(app!!,model!!)?.fields?.getFieldByPropertyName(name)
                f.title =if(fd?.title != null) fd!!.title!! else ""
            }
        }
        f.icon=icon
        if(name.indexOf(".")>-1){
            try {
                val model= AppModel.ref.getModel(this.app!!,this.model!!)
                var (propertyName,toPropertyName) = name.split(".")
                f.name=propertyName
                val mField = model?.getFieldByPropertyName(propertyName)
                if(mField!=null){
                    f.relationData=when(mField){
                        is Many2ManyField->{
                            val rModel = AppModel.ref.getModel(mField.relationModelTable!!)
                            val rField = rModel?.fields?.getField(mField.relationModelFieldName)
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(rModel!=null && rField!=null && tModel!=null && tField!=null){
                                 RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                        rModel.meta.appName,rModel.meta.name,rField.propertyName,RelationType.Many2Many,toPropertyName)
                            }
                            else{
                                null
                            }
                        }
                        is Many2OneField->{
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(tModel!=null && tField!=null){
                                RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                       "","","",RelationType.Many2One,toPropertyName)
                            }
                            else{
                                null
                            }
                        }
                        is One2ManyField->{
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(tModel!=null && tField!=null){
                                RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                        "","","",RelationType.One2Many,toPropertyName)
                            }
                            else{
                                null
                            }
                        }
                        is One2OneField->{
                            var tModel = AppModel.ref.getModel(mField.targetModelTable!!)
                            var tField=tModel?.fields?.getField(mField.targetModelFieldName)
                            if(tModel!=null && tField!=null){
                                RelationData(tModel.meta.appName,tModel.meta.name,tField.propertyName,
                                        "","","",if(mField.isVirtualField) RelationType.VirtualOne2One else RelationType.One2One,toPropertyName)
                            }
                            else{
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
        mv.enable=enable
        mv.visible=visible
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
         }
         var title:String=""
         var icon:String=""
         var visible:Int=1
         var enable = 1
         var relationData:RelationData?=null
         var targetFields:Array<Field>?=null
         var fieldView:ModelView?=null
         var meta:JsonObject?=null
         fun createCopy():Field{
            val f= Field(this.modelView,name,style,rowSpan,colSpan,type)
            f.visible=visible
            f.title=title
            f.enable=this.enable
            f.icon=icon
            f.relationData=relationData
            f.fieldView=fieldView
            f.meta=meta
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
                         val refType:String){
        fun createCopy():RefActionGroup{
            return RefActionGroup(
                    app,
                    model,
                    viewType,
                    groupName,
                    refType
            )
        }
    }
    class RefView(val app:String,
                  val model:String,
                  val viewType:String,
                  val fieldName:String,
                  val title:String,
                  val style:String,
                  val refType:String){
        fun createCopy():RefView{
            return RefView(
                    app,
                    model,
                    viewType,
                    fieldName,
                    title,
                    style,
                    refType
            )
        }
    }

    class RefMenu(val app:String,
                  val name:String,
                  val refType:String){
        fun createCopy():RefMenu{
            return RefMenu(
                    app,
                    name,
                    refType
            )
        }
    }
}