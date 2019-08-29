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