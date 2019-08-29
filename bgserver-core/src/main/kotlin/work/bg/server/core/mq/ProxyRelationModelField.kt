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

import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.specialized.ConstRelRegistriesField

class ProxyRelationModelField<T>(model:ModelBase?,
                                 val field:FieldBase,
                                 name:String,
                                 fieldType:FieldType,
                                 title:String?=null):FunctionField<T>(model,name,fieldType,title,null,null, arrayOf(field)) {

    override fun compute(fieldValueArry: FieldValueArray, partnerCache: PartnerCache?, data: Any?): T? {
        setProxyModelFieldValueFromRelationModel(fieldValueArry, partnerCache, data)
        return null
    }

    private fun setProxyModelFieldValueFromRelationModel(fieldValueArray: FieldValueArray, partnerCache: PartnerCache?, data: Any?){
        var relRegisterFiled = fieldValueArray.firstOrNull {
            it.field.isSame(ConstRelRegistriesField.ref)
        }

        relRegisterFiled?.let {
                var md = (it.value as ModelDataSharedObject).data[this.field.model]
                md?.let { rMD->
                    when(rMD){
                        is ModelDataObject->{
                            val fv = rMD.data.getValue(this.field)
                            fieldValueArray.setValue(this,fv)
                        }
                        is ModelDataArray->{
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


    override fun inverse(fieldValueArray: FieldValueArray, partnerCache: PartnerCache?, value: T?, data: Any?) {
        setProxyModelFieldValueArrayToRelationModel(fieldValueArray)
    }

    private fun setProxyModelFieldValueArrayToRelationModel(fieldValueArray: FieldValueArray) {

        var relRegisterFiled = fieldValueArray.firstOrNull {
            it.field.isSame(ConstRelRegistriesField.ref)
        }
        val cloneFieldValueArray = fieldValueArray.clone()
        relRegisterFiled?.let {
                var md = (it.value as ModelDataSharedObject).data[this.field.model]

                md?.let {
                    when (it) {
                        is ModelDataObject -> {
                            if (!it.data.containFieldKey(this.field)) {
                                val v = fieldValueArray.getValue(this)
                                it.data.setValue(this.field, v)
                            }
                        }
                        is ModelDataArray -> {
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