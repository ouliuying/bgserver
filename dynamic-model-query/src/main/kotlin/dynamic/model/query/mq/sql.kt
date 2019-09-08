

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

import dynamic.model.query.mq.logical.AndExpression
import dynamic.model.query.mq.logical.OrExpression
import dynamic.model.query.mq.model.ModelBase


fun select(vararg fields: dynamic.model.query.mq.FieldBase, fromModel: ModelBase): dynamic.model.query.mq.SelectStatement {
 return dynamic.model.query.mq.SelectStatement(*fields, fromModel = fromModel)
}
fun update(vararg fieldValues: dynamic.model.query.mq.FieldValue, setModel: ModelBase): dynamic.model.query.mq.UpdateStatement {
    return dynamic.model.query.mq.UpdateStatement(*fieldValues, setModel = setModel)
}
fun delete(fromModel: ModelBase): dynamic.model.query.mq.DeleteStatement {
    return dynamic.model.query.mq.DeleteStatement(fromModel)
}
fun create(vararg fieldValues: dynamic.model.query.mq.FieldValue, model: ModelBase): dynamic.model.query.mq.CreateStatement {
    return dynamic.model.query.mq.CreateStatement(*fieldValues, model = model)
}
fun and(vararg expression: dynamic.model.query.mq.ModelExpression): dynamic.model.query.mq.ModelExpression {
    return AndExpression(*expression)
}
fun or(vararg expression: dynamic.model.query.mq.ModelExpression): dynamic.model.query.mq.ModelExpression {

    return OrExpression(*expression)
}

fun eq(field: dynamic.model.query.mq.FieldBase, value:Any?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.EqualExpression(field, value)
}

fun exists(field: dynamic.model.query.mq.FieldBase, criteria: dynamic.model.query.mq.ModelExpression): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.ExistsExpression(field, criteria)
}

fun notExists(field: dynamic.model.query.mq.FieldBase, criteria: dynamic.model.query.mq.ModelExpression): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.NotExistsExpression(field, criteria)
}

fun gtEq(field: dynamic.model.query.mq.FieldBase, value:Any?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.GreaterEqualExpression(field, value)
}
fun gt(field: dynamic.model.query.mq.FieldBase, value:Any?): dynamic.model.query.mq.ModelExpression?{
    return dynamic.model.query.mq.condition.GreaterExpression(field, value)
}
fun `in`(field: dynamic.model.query.mq.FieldBase, valueSet:Array<Any>?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.InExpression(field, valueSet)
}
fun `in`(field: dynamic.model.query.mq.FieldBase, criteria: dynamic.model.query.mq.ModelExpression?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.InExpression(field, criteria)
}

fun notIn(field: dynamic.model.query.mq.FieldBase, valueSet:Array<Any>?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.NotInExpression(field, valueSet)
}
fun notIn(field: dynamic.model.query.mq.FieldBase, criteria: dynamic.model.query.mq.ModelExpression?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.NotInExpression(field, criteria)
}

fun `is`(field: dynamic.model.query.mq.FieldBase, value:Any?=null): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.IsExpression(field, value)
}
fun isNot(field: dynamic.model.query.mq.FieldBase, value:Any?=null): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.IsNotExpression(field, value)
}
fun ltEq(field: dynamic.model.query.mq.FieldBase, value:Any?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.LessEqualExpression(field, value)
}
fun lt(field: dynamic.model.query.mq.FieldBase, value:Any?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.LessExpression(field, value)
}

fun like(field: dynamic.model.query.mq.FieldBase, value:String): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.LikeExpression(field, value)
}
fun iLike(field: dynamic.model.query.mq.FieldBase, value:String): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.LikeExpression(field, value)
}
fun notLike(field: dynamic.model.query.mq.FieldBase, value:String): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.NotLikeExpression(field, value)
}
fun notILike(field: dynamic.model.query.mq.FieldBase, value:String): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.LikeExpression(field, value)
}

fun notEq(field: dynamic.model.query.mq.FieldBase, value:Any?): dynamic.model.query.mq.ModelExpression {
    return dynamic.model.query.mq.condition.NotEqualExpression(field, value)
}

