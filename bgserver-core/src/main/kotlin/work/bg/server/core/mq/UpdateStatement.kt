

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

class UpdateStatement(vararg val fieldValues:FieldValue,val setModel:ModelBase?):ModelExpression(){
    var whereExpression:ModelExpression?=null
    fun where(expression:ModelExpression?):UpdateStatement{
        this.whereExpression=expression
        return this
    }
    override fun accept(visitor: ModelExpressionVisitor, parent: ModelExpression?): Boolean {
        visitor.visit(this,parent)
        return true
    }

    override fun render(parent: ModelExpression?): Pair<String, Map<String, FieldValue>>? {
        var render=ModelCriteriaRender()
        this.accept(render,parent)
        return Pair(render.namedSql.toString(),render.namedParameters)
    }
}