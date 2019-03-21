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

class FieldValueArray: ArrayList<FieldValue>(){
    fun getValue(field:FieldBase):Any?{
        var fv=this.firstOrNull {
            it.field.isSame(field)
        }
        return fv?.value
    }
    fun setValue(field:FieldBase,value:Any?):Int{
        var index=this.indexOfFirst {
            it.field.isSame(field)
        }
        return if(index>-1){
            this[index]= FieldValue(field,value)
            index
        }
        else{
            this.add(FieldValue(field,value))
            -1
        }
    }
}