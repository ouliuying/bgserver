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

import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.specialized.ConstRelRegistriesField

class ProxyRelationModelField<T>(model:ModelBase?,
                                 val field:FieldBase,
                                 name:String,
                                 fieldType:FieldType,
                                 title:String?=null):FunctionField<T>(model,null,null,name,fieldType,title, arrayOf(field)) {

    override fun compute(fieldValueArry: FieldValueArray, partnerCache: PartnerCache, data: Any?): T? {
        setProxyModelFieldValueFromRelationModel(fieldValueArry, partnerCache, data)
        return null
    }

    private fun setProxyModelFieldValueFromRelationModel(fieldValueArray: FieldValueArray, partnerCache: PartnerCache, data: Any?){
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


    override fun inverse(fieldValueArray: FieldValueArray, partnerCache: PartnerCache, value: T?, data: Any?) {
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