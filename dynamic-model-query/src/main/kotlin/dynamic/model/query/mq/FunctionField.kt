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


open class FunctionField<T,ContextType>(model: ModelBase?,
                                        name:String,
                                        fieldType: dynamic.model.query.mq.FieldType,
                                        title:String?,
                                        val comp:((dynamic.model.query.mq.FieldValueArray, ContextType?, Any?)->T?)?=null,
                                        val inv:((dynamic.model.query.mq.FieldValueArray, ContextType?, T?, Any?)->Unit)?=null,
                                        open val depFields: Array<dynamic.model.query.mq.FieldBase?>?=null): dynamic.model.query.mq.FieldBase(name,title,fieldType,model) {
    open  fun  compute(fieldValueArray: dynamic.model.query.mq.FieldValueArray, context: ContextType?, data:Any?):T?{
            return this.comp?.invoke(fieldValueArray,context,data)
    }

    open fun  inverse(fieldValueArray: dynamic.model.query.mq.FieldValueArray, partnerCache: ContextType?, value:T?, data:Any?){
       this.inv?.invoke(fieldValueArray,partnerCache,value,data)
    }
}