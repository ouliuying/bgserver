

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
