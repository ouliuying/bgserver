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

import org.apache.tomcat.jni.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ModelFieldConvert {
    companion object {
        fun toTypeValue(field: FieldBase?, value:String?):Any?{
            return when(field?.fieldType){
                FieldType.INT ->value?.toInt()
                FieldType.DATE ->if(value=="now()") LocalDate.now() else LocalDate.parse(value)
                FieldType.DATETIME ->if(value=="now()") LocalDateTime.now() else LocalDateTime.parse(value)
                FieldType.TIME ->if(value=="now()") LocalTime.now() else LocalTime.parse(value)
                FieldType.TEXT ->value
                FieldType.STRING ->value
                FieldType.BIGINT ->value?.toBigInteger()
                FieldType.NUMBER ->value?.toBigDecimal()
                else->null
            }
        }
    }
}