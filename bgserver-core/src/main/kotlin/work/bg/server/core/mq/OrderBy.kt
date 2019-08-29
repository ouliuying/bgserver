

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

class  OrderBy constructor(vararg val fields:OrderField):ModelExpression(*fields){
    companion object {
        enum class OrderType(val typ:Int){
             ASC(0),
             DESC(1)
        }

    }

    override fun render(parent:ModelExpression?): Pair<String, Map<String, Any?>>? {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }

    override fun accept(visitor: ModelExpressionVisitor,parent:ModelExpression?): Boolean {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        visitor.visit(this,parent)
        return true
    }


    class OrderField constructor(val field: FieldBase,val orderType: OrderType=OrderType.ASC):ModelExpression(){
        override fun render(parent:ModelExpression?): Pair<String, Map<String, Any?>>? {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            return null
        }

        override fun accept(visitor: ModelExpressionVisitor,parent:ModelExpression?): Boolean {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            visitor.visit(this,parent)
            return true
        }


    }
}
