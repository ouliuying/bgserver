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

class ModelView(val app:String?,val model:String?,val viewType:String?) {
    var fields:ArrayList<Field> = arrayListOf()
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
        f.icon=icon
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
                 val name:String,
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
         }
         var title:String=""
         var icon:String=""
         var visible:Int=1
         var enable = 1
         var relationData:RelationData?=null
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

}