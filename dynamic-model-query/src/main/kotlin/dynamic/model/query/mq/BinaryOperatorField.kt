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

import dynamic.model.query.mq.model.ModelBase

open class BinaryOperatorField constructor(val leftField: dynamic.model.query.mq.FieldBase?, val rightField: dynamic.model.query.mq.FieldBase?, val operator:String, name:String="", title:String?=null, fieldType: dynamic.model.query.mq.FieldType = dynamic.model.query.mq.FieldType.NONE, model: ModelBase?=null, length:Int?=null): dynamic.model.query.mq.FieldBase(name,title,fieldType,model,length){


    operator  fun plus(field: dynamic.model.query.mq.BinaryOperatorField): dynamic.model.query.mq.BinaryOperatorField { //+operator
        return dynamic.model.query.mq.BinaryOperatorField(this, field, "+")
    }
    operator fun minus(field: dynamic.model.query.mq.BinaryOperatorField): dynamic.model.query.mq.BinaryOperatorField {//-operator
        return dynamic.model.query.mq.BinaryOperatorField(this, field, "-")
    }

    operator fun times(field: dynamic.model.query.mq.BinaryOperatorField): dynamic.model.query.mq.BinaryOperatorField {//*operator
        return dynamic.model.query.mq.BinaryOperatorField(this, field, "*")
    }
    operator fun div(field: dynamic.model.query.mq.BinaryOperatorField): dynamic.model.query.mq.BinaryOperatorField {// /operator
        return dynamic.model.query.mq.BinaryOperatorField(this, field, "/")
    }

}