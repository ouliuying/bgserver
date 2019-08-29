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
            this.size-1
        }
    }
    fun containFieldKey(field:FieldBase):Boolean{
        var fv=this.firstOrNull {
            it.field.isSame(field)
        }
        return fv!=null
    }
}