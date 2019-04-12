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

import work.bg.server.core.constant.ModelReservedKey

abstract  class  FieldBase constructor(open var name:String,
                                       open var title:String?,
                                       open var fieldType:FieldType,
                                       open var model:ModelBase?,
                                       open var length:Int?=null):ModelExpression(){

    open val  propertyName:String=""
    override fun accept(visitor: ModelExpressionVisitor,parent:ModelExpression?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    open fun getFullName():String?{
        return "${this.model?.fullTableName}.${this.name}"
    }
    override fun render(parent: ModelExpression?): Pair<String, Map<String, Any?>>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open fun isSame(field:FieldBase):Boolean{
        return this.getFullName() == field.getFullName()
    }
    open fun isIdField():Boolean
    {
        return this.propertyName==ModelReservedKey.idFieldName
    }
}

