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

package work.bg.server.core.context

class ModelExpressionContext(val partnerID:Long,val corpID:Long,val roleID:Long){
     internal object ContextKey {
         const val partner_id="\$partnerID$"
         const val corp_id="\$corpID$"
     }
     private var contextValues:MutableMap<String,ContextValue> = mutableMapOf()
     fun valueFromContextKey(key:String):Pair<Boolean,Any?>{
         return when(key){
             ContextKey.corp_id-> Pair(true,corpID)
             ContextKey.partner_id->Pair(true,partnerID)
             else->this.valueFromDynamicContextKey(key)
         }
     }
    private fun valueFromDynamicContextKey(key:String):Pair<Boolean,ContextValue?>{
        var v=this.contextValues[key]
        return if(v!=null) {
            Pair(true,v)
        } else Pair(false,null as ContextValue?)

    }

    fun put(name:String,value:ContextValue){
        this.contextValues[name] = value
    }
    fun remove(name:String){
        this.contextValues.remove(name)
    }
    inner class ContextValue{
        var name:String?=null
        var value:Any?=null
    }
}