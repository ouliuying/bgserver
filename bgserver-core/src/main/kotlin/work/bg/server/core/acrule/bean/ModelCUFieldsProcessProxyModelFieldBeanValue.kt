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

package work.bg.server.core.acrule.bean

import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelCreateRecordFieldsValueInitializeRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.*

@Component
class ModelCUFieldsProcessProxyModelFieldBeanValue: ModelCreateRecordFieldsValueInitializeRule<Any> {
    private lateinit var _config:String
    override var config: String
        get() = _config
        set(value) {
            _config=value
        }
    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Any?): Pair<Boolean, String> {
        this.invokeFieldArray(modelData.data,partnerCache,data)
        return Pair(true,"")
    }

    private fun invokeFieldArray(fieldValueArray:FieldValueArray,partnerCache: PartnerCache, data: Any?){
        var cloneArr = arrayListOf(*fieldValueArray.toTypedArray())
        cloneArr.forEach {
            when {
                it.field is FunctionField<*> -> it.field.inverse(fieldValueArray,partnerCache,null,data)
                it.value is ModelDataObject -> this.invoke(it.value,partnerCache,null)
                it.value is ModelDataArray -> this.invokeArray(it.value,partnerCache,null)
                it.value is ModelDataSharedObject -> it.value.data.forEach { _, u ->
                    when(u){
                        is ModelDataObject->{
                            this.invoke(u,partnerCache,null)
                        }
                        is ModelDataArray->{
                            this.invokeArray(u,partnerCache,null)
                        }
                    }
                }
            }
        }
    }

    private fun invokeArray(modelData: ModelDataArray, partnerCache: PartnerCache, data: Any?){
        modelData.data.forEach {
            invokeFieldArray(it,partnerCache,data)
        }
    }
}