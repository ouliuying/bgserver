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

import dynamic.model.query.constant.ModelReservedKey
import dynamic.model.query.mq.model.ModelBase
import sun.plugin.com.TypeConverter


class ModelDataObject(override var data: dynamic.model.query.mq.FieldValueArray = dynamic.model.query.mq.FieldValueArray(),
                      model: ModelBase?=null, fields:ArrayList<dynamic.model.query.mq.FieldBase>?=null): dynamic.model.query.mq.ModelData(data,model,fields) {
    val idFieldValue: dynamic.model.query.mq.FieldValue?
        get()= data.firstOrNull{
            it.field.name== ModelReservedKey.idFieldName
        }
    companion object{
        fun getModelDataObjectID(modelObject:Any?):Long?{
            when(modelObject){
                is ModelDataObject?->{
                    val v = modelObject?.idFieldValue?.value
                    return v?.let {
                        when (it) {
                            is Long -> it
                            is Number -> it.toLong()
                            else -> null
                        }
                    }
                }
                is Long?->{
                    return modelObject
                }
                is Number? ->{
                    return modelObject?.toLong()
                }
                else ->{
                    return null
                }
            }
        }
    }
    override fun isObject(): Boolean {
        return true
    }

    fun hasNormalField():Boolean{
        return data.count {
            !it.field.isSame(model!!.fields.getIdField()!!) && it.field is dynamic.model.query.mq.ModelField
        }>0
    }
    fun getFieldValue(field: dynamic.model.query.mq.FieldBase):Any?{
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
            var fValue= if(value!=null) dynamic.model.query.mq.ModelFieldConvert.Companion.toTypeValue(fieldValue.field, value) else null
            this.data.setValue(fieldValue.field,fValue)
        }
        else {
            var field = this.model?.fields?.getFieldByPropertyName(propertyName)
            if(field!=null){
                this.data.add(dynamic.model.query.mq.FieldValue(field, if (value != null) dynamic.model.query.mq.ModelFieldConvert.Companion.toTypeValue(field, value) else null))
                this.fields?.add(field)
            }
        }
    }

    fun setFieldValue(field: dynamic.model.query.mq.FieldBase, value:Any?){
        var fieldValue = this.data.firstOrNull {
            it.field.isSame(field)
        }
        val tValue = when(value) {
            is com.google.gson.internal.LazilyParsedNumber -> {
                dynamic.model.query.mq.ModelFieldConvert.Companion.toTypeValue(field, value.toString())
            }
            else -> value
        }

        if(fieldValue!=null){
           this.data.setValue(fieldValue.field,tValue)
        }
        else {
            this.data.add(dynamic.model.query.mq.FieldValue(field, tValue))
            this.fields?.add(field)
        }
    }

    fun hasFieldValue(propertyName:String):Boolean{
        return this.data.firstOrNull {
                    it.field.propertyName == propertyName
                }!=null
    }
    fun hasFieldValue(field: dynamic.model.query.mq.FieldBase):Boolean{
        return this.data.firstOrNull {
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
    fun removeFieldValue(field: dynamic.model.query.mq.FieldBase){
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