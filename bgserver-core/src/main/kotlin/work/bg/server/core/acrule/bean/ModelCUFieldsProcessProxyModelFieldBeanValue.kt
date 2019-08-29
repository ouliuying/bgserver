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