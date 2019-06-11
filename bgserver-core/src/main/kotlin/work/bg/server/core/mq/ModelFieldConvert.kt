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

import org.apache.tomcat.jni.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ModelFieldConvert {
    companion object {
        fun toTypeValue(field:FieldBase?,value:String?):Any?{
            return when(field?.fieldType){
                FieldType.INT->value?.toInt()
                FieldType.DATE->if(value=="now()") LocalDate.now() else LocalDate.parse(value)
                FieldType.DATETIME->if(value=="now()") LocalDateTime.now() else LocalDateTime.parse(value)
                FieldType.TIME->if(value=="now()") LocalTime.now() else LocalTime.parse(value)
                FieldType.TEXT->value
                FieldType.STRING->value
                FieldType.BIGINT->value?.toBigInteger()
                FieldType.NUMBER->value?.toBigDecimal()
                else->null
            }
        }
    }
}