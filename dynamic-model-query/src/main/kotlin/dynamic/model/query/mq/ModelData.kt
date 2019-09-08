/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package dynamic.model.query.mq

import dynamic.model.query.mq.model.ModelBase

open  abstract  class ModelData(open val data:Any,
                                var model: ModelBase? = null, var fields:ArrayList<dynamic.model.query.mq.FieldBase>?=null) {
    //parent field
    var fromField: dynamic.model.query.mq.FieldBase?= null
    var fromIdValue:Long?=null
    //model self field
    var toField: dynamic.model.query.mq.FieldBase?=null

    open fun isArray():Boolean
    {
        return false
    }
    open fun isObject():Boolean{
        return false
    }
    open fun isSharedObject():Boolean{
        return false
    }
    fun createContext()
    {
        this.context=Context()
    }
    var context: dynamic.model.query.mq.ModelData.Context?=null
    inner class Context{
            var refRecordMap:MutableMap<String, dynamic.model.query.mq.ModelData> = mutableMapOf()
    }
    fun <T>`as`():T{
        return this as T
    }
    open fun isEmpty():Boolean=false
}