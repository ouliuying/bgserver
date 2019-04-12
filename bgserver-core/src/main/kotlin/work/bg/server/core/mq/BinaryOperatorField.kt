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

open class BinaryOperatorField constructor(val leftField:FieldBase?, val rightField:FieldBase?, val operator:String, name:String="",title:String?=null, fieldType:FieldType=FieldType.NONE, model:ModelBase?=null, length:Int?=null):FieldBase(name,title,fieldType,model,length){


    operator  fun plus(field:BinaryOperatorField):BinaryOperatorField{ //+operator
        return BinaryOperatorField(this,field,"+")
    }
    operator fun minus(field: BinaryOperatorField):BinaryOperatorField{//-operator
        return BinaryOperatorField(this,field,"-")
    }

    operator fun times(field: BinaryOperatorField):BinaryOperatorField{//*operator
        return BinaryOperatorField(this,field,"*")
    }
    operator fun div(field: BinaryOperatorField):BinaryOperatorField{// /operator
        return BinaryOperatorField(this,field,"/")
    }

}