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