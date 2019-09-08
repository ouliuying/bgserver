

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

class  OrderBy constructor(vararg val fields: dynamic.model.query.mq.OrderBy.OrderField): dynamic.model.query.mq.ModelExpression(*fields){
    companion object {
        enum class OrderType(val typ:Int){
             ASC(0),
             DESC(1)
        }

    }

    override fun render(parent: dynamic.model.query.mq.ModelExpression?): Pair<String, Map<String, Any?>>? {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }

    override fun accept(visitor: dynamic.model.query.mq.ModelExpressionVisitor, parent: dynamic.model.query.mq.ModelExpression?): Boolean {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        visitor.visit(this,parent)
        return true
    }


    class OrderField constructor(val field: dynamic.model.query.mq.FieldBase, val orderType: dynamic.model.query.mq.OrderBy.Companion.OrderType = dynamic.model.query.mq.OrderBy.Companion.OrderType.ASC): dynamic.model.query.mq.ModelExpression(){
        override fun render(parent: dynamic.model.query.mq.ModelExpression?): Pair<String, Map<String, Any?>>? {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            return null
        }

        override fun accept(visitor: dynamic.model.query.mq.ModelExpressionVisitor, parent: dynamic.model.query.mq.ModelExpression?): Boolean {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            visitor.visit(this,parent)
            return true
        }


    }
}
