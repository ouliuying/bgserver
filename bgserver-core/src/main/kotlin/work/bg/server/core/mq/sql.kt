

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

package work.bg.server.core.mq

import work.bg.server.core.mq.aggregation.CountExpression
import work.bg.server.core.mq.condition.*
import work.bg.server.core.mq.logical.AndExpression
import work.bg.server.core.mq.logical.OrExpression

fun select(vararg fields:FieldBase,fromModel:ModelBase):SelectStatement{
 return SelectStatement(*fields,fromModel = fromModel)
}
fun update(vararg fieldValues:FieldValue,setModel:ModelBase):UpdateStatement{
    return UpdateStatement(*fieldValues,setModel = setModel)
}
fun delete(fromModel:ModelBase):DeleteStatement{
    return DeleteStatement(fromModel)
}
fun create(vararg fieldValues:FieldValue,model:ModelBase):CreateStatement{
    return CreateStatement(*fieldValues,model=model)
}
fun and(vararg expression:ModelExpression):ModelExpression{
    return AndExpression(*expression)
}
fun or(vararg expression:ModelExpression):ModelExpression{

    return OrExpression(*expression)
}

fun eq(field:FieldBase,value:Any?):ModelExpression{
    return EqualExpression(field,value)
}

fun exists(field:FieldBase,criteria:ModelExpression):ModelExpression{
    return ExistsExpression(field,criteria)
}

fun notExists(field:FieldBase,criteria:ModelExpression):ModelExpression{
    return NotExistsExpression(field,criteria)
}

fun gtEq(field:FieldBase,value:Any?):ModelExpression{
    return GreaterEqualExpression(field,value)
}
fun gt(field:FieldBase,value:Any?):ModelExpression?{
    return GreaterExpression(field,value)
}
fun `in`(field:FieldBase,valueSet:Array<Any>?):ModelExpression{
    return InExpression(field,valueSet)
}
fun `in`(field:FieldBase,criteria:ModelExpression?):ModelExpression{
    return InExpression(field,criteria)
}

fun notIn(field:FieldBase,valueSet:Array<Any>?):ModelExpression{
    return NotInExpression(field,valueSet)
}
fun notIn(field:FieldBase,criteria:ModelExpression?):ModelExpression{
    return NotInExpression(field,criteria)
}

fun `is`(field:FieldBase,value:Any?=null):ModelExpression{
    return IsExpression(field,value)
}
fun isNot(field:FieldBase,value:Any?=null):ModelExpression{
    return IsNotExpression(field,value)
}
fun ltEq(field:FieldBase, value:Any?):ModelExpression{
    return LessEqualExpression(field,value)
}
fun lt(field:FieldBase, value:Any?):ModelExpression{
    return LessExpression(field,value)
}

fun like(field:FieldBase,value:String):ModelExpression{
    return LikeExpression(field,value)
}
fun iLike(field:FieldBase,value:String):ModelExpression{
    return LikeExpression(field,value)
}
fun notLike(field:FieldBase,value:String):ModelExpression{
    return NotLikeExpression(field,value)
}
fun notILike(field:FieldBase,value:String):ModelExpression{
    return LikeExpression(field,value)
}

fun notEq(field:FieldBase,value:Any?):ModelExpression{
    return NotEqualExpression(field,value)
}

