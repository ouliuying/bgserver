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

package work.bg.server.core.mq

open  abstract  class ModelData(open val data:Any,
                var model:ModelBase? = null,var fields:ArrayList<FieldBase>?=null) {
    //parent field
    var fromField:FieldBase?= null
    var fromIdValue:Long?=null
    //model self field
    var toField:FieldBase?=null

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
    var context:Context?=null
    inner class Context{
            var refRecordMap:MutableMap<String,ModelData> = mutableMapOf()
    }
    fun <T>`as`():T{
        return this as T
    }
    open fun isEmpty():Boolean=false
}