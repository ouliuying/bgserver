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
import dynamic.model.query.mq.specialized.ConstRelRegistriesField


class ProxyRelationModelField<T,ContextType>(model: ModelBase?,
                                             val field: dynamic.model.query.mq.FieldBase,
                                             name:String,
                                             fieldType: dynamic.model.query.mq.FieldType,
                                             title:String?=null): dynamic.model.query.mq.FunctionField<T,ContextType>(model,name,fieldType,title,null,null, arrayOf(field)) {

    override fun compute(fieldValueArry: dynamic.model.query.mq.FieldValueArray, context: ContextType?, data: Any?): T? {
        setProxyModelFieldValueFromRelationModel(fieldValueArry, context, data)
        return null
    }

    private fun setProxyModelFieldValueFromRelationModel(fieldValueArray: dynamic.model.query.mq.FieldValueArray, partnerCache: ContextType?, data: Any?){
        var relRegisterFiled = fieldValueArray.firstOrNull {
            it.field.isSame(ConstRelRegistriesField.ref)
        }

        relRegisterFiled?.let {
                var md = (it.value as dynamic.model.query.mq.ModelDataSharedObject).data[this.field.model]
                md?.let { rMD->
                    when(rMD){
                        is dynamic.model.query.mq.ModelDataObject ->{
                            val fv = rMD.data.getValue(this.field)
                            fieldValueArray.setValue(this,fv)
                        }
                        is dynamic.model.query.mq.ModelDataArray ->{
                            val firstFV = rMD.data.firstOrNull()
                            firstFV?.let {
                                val fv = it.getValue(this.field)
                                fieldValueArray.setValue(this,fv)
                            }
                        }
                        else -> null
                    }
                }
        }
    }


    override fun inverse(fieldValueArray: dynamic.model.query.mq.FieldValueArray, context: ContextType?, value: T?, data: Any?) {
        setProxyModelFieldValueArrayToRelationModel(fieldValueArray)
    }

    private fun setProxyModelFieldValueArrayToRelationModel(fieldValueArray: dynamic.model.query.mq.FieldValueArray) {

        var relRegisterFiled = fieldValueArray.firstOrNull {
            it.field.isSame(ConstRelRegistriesField.ref)
        }
        val cloneFieldValueArray = fieldValueArray.clone()
        relRegisterFiled?.let {
                var md = (it.value as dynamic.model.query.mq.ModelDataSharedObject).data[this.field.model]

                md?.let {
                    when (it) {
                        is dynamic.model.query.mq.ModelDataObject -> {
                            if (!it.data.containFieldKey(this.field)) {
                                val v = fieldValueArray.getValue(this)
                                it.data.setValue(this.field, v)
                            }
                        }
                        is dynamic.model.query.mq.ModelDataArray -> {
                            val cnt = it.data.count()-1
                            for(i in 0..cnt){
                                val tfv = it.data[i]
                                if (!tfv.containFieldKey(this.field)) {
                                   val v = fieldValueArray.getValue(this)
                                  tfv.setValue(this.field, v)
                                }
                            }
                        }
                    }
                }
        }
    }
}