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

import dynamic.model.query.constant.ModelReservedKey
import dynamic.model.query.mq.model.ModelBase

abstract  class  FieldBase constructor(open var name:String,
                                       open var title:String?,
                                       open var fieldType: dynamic.model.query.mq.FieldType,
                                       open var model: ModelBase?,
                                       open var length:Int?=null): dynamic.model.query.mq.ModelExpression(){

    open val  propertyName:String=""
    override fun accept(visitor: dynamic.model.query.mq.ModelExpressionVisitor, parent: dynamic.model.query.mq.ModelExpression?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    open fun getFullName():String{
        return "${this.model?.fullTableName}.${this.name}"
    }
    override fun render(parent: dynamic.model.query.mq.ModelExpression?): Pair<String, Map<String, Any?>>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open fun isSame(field: dynamic.model.query.mq.FieldBase):Boolean{
        return this.getFullName() == field.getFullName()
    }
    open fun isIdField():Boolean
    {
        return this.propertyName== ModelReservedKey.idFieldName
    }
}

