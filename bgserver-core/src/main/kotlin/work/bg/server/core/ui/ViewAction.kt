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

import com.google.gson.JsonObject

data class Trigger(val name:String,
                   var title:String,
                   var app:String,
                   var model:String,
                   var viewType:String){

    var enable:String?=null
    var visible:String?=null
    var ownerField:String?=null
    var actionName:String?=null
    var icon:String?=null
    var meta:JsonObject?=null

    fun createCopy():Trigger{
        val t= Trigger(name,title,app,model,viewType)
        t.meta = meta
        t.icon=icon
        return t
    }
}
data class TriggerGroup(val name:String,
                        var triggers:ArrayList<Trigger> = arrayListOf()){
    var enable:String?=null
    fun createCopy():TriggerGroup{
        var tg=TriggerGroup(name)
        this.triggers.forEach {
            tg.triggers.add(it.createCopy())
        }
        return tg
    }
}
data class ViewAction(var app:String,
                 var model:String,
                 var viewType:String,var groups:MutableMap<String,TriggerGroup> = mutableMapOf()){
    fun createCopy():ViewAction{
        var va=ViewAction(app,model,viewType)
        this.groups.forEach {
            va.groups[it.key]=it.value.createCopy()
        }
        return va
    }
}