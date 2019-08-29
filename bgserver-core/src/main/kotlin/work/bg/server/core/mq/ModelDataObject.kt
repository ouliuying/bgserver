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

import work.bg.server.core.constant.ModelReservedKey

class ModelDataObject(override var data: FieldValueArray = FieldValueArray(),
    model:ModelBase?=null,fields:ArrayList<FieldBase>?=null):ModelData(data,model,fields) {
    val idFieldValue:FieldValue?
        get()= data.firstOrNull{
            it.field.name== ModelReservedKey.idFieldName
        }

    override fun isObject(): Boolean {
        return true
    }

    fun hasNormalField():Boolean{
        return data.count {
            !it.field.isSame(model!!.fields.getIdField()!!) && it.field is ModelField
        }>0
    }
    fun getFieldValue(field:FieldBase):Any?{
        var fieldValue = this.data.firstOrNull {
            it.field.isSame(field)
        }
        return if(fieldValue!=null) fieldValue.value else null
    }
    fun setFieldValue(propertyName:String,value:String?){
        var fieldValue = this.data.firstOrNull {
            it.field.propertyName==propertyName
        }
        if(fieldValue!=null) {
            var fValue= if(value!=null) ModelFieldConvert.toTypeValue(fieldValue.field,value) else null
            this.data.setValue(fieldValue.field,fValue)
        }
        else {
            var field = this.model?.fields?.getFieldByPropertyName(propertyName)
            if(field!=null){
                this.data.add(FieldValue(field,if(value!=null) ModelFieldConvert.toTypeValue(field,value) else null))
                this.fields?.add(field)
            }
        }
    }

    fun setFieldValue(field:FieldBase,value:Any?){
        var fieldValue = this.data.firstOrNull {
            it.field.isSame(field)
        }
        val tValue = when(value) {
            is com.google.gson.internal.LazilyParsedNumber -> {
                ModelFieldConvert.toTypeValue(field,value?.toString())
            }
            else -> value
        }

        if(fieldValue!=null){
           this.data.setValue(fieldValue.field,tValue)
        }
        else {
            this.data.add(FieldValue(field,tValue))
            this.fields?.add(field)
        }
    }

    fun hasFieldValue(propertyName:String):Boolean{
        return this.data.firstOrNull() {
                    it.field.propertyName == propertyName
                }!=null
    }
    fun hasFieldValue(field:FieldBase):Boolean{
        return this.data.firstOrNull() {
            it.field.isSame(field)
        }!=null
    }
    fun removeFieldValue(propertyName:String){
        this.data.removeIf{
            it.field.propertyName==propertyName
        }
        this.fields?.removeIf {
            it.propertyName==propertyName
        }
    }
    fun removeFieldValue(field:FieldBase){
        this.data.removeIf{
            it.field.isSame(field)
        }
        this.fields?.removeIf {
            it.isSame(field)
        }
    }

    override fun isEmpty(): Boolean {
        return this.data.isEmpty()
    }
}