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

data class Trigger(val name:String,
                   var title:String,
                   var app:String,
                   var model:String,
                   var viewType:String){

    var enable:String?=null
    var visible:String?=null
    var ownerField:String?=null
    var actionName:String?=null
    var meta:JsonObject?=null

    fun createCopy():Trigger{
        val t= Trigger(name,title,app,model,viewType)
        t.meta = meta
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