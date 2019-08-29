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
import work.bg.server.core.acrule.ModelCreateRecordFieldsValueCheckInStoreRule
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.FieldValueArray
import work.bg.server.core.mq.ModelDataObject
@Component
class ModelCreateFieldsInStoreInspectorCheckBean:
        ModelCreateRecordFieldsValueCheckInStoreRule<Array<ModelFieldInspector>> {
        private lateinit var _config:String
        override  var config: String
            get() {
                return this._config
            }
            set(value) {
                this._config=value
            }
        override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Array<ModelFieldInspector>?): Pair<Boolean, String> {
            data?.forEach {
                when(it){
                    is ModelFieldUnique->{
                        var targetFieldValues=FieldValueArray()
                        modelData.data.forEach {fv->
                            if(it.targetFields.count { sit->sit.isSame(fv.field) }>0){
                                targetFieldValues.setValue(fv.field,fv.value)
                            }
                        }
                        if(it.targetFields.count()!=targetFieldValues.count()){
                            return@forEach
                        }
                        val m =modelData.model as AccessControlModel

                        if(it.isolationType==ModelFieldUnique.IsolationType.IN_CORP){
                            targetFieldValues.setValue(m.createCorpID,partnerCache.corpID)
                        }
                        else if(it.isolationType==ModelFieldUnique.IsolationType.IN_PARTNER){
                            targetFieldValues.setValue(m.createCorpID,partnerCache.corpID)
                            targetFieldValues.setValue(m.createPartnerID,partnerCache.partnerID)
                        }

                        if(m.rawCount(targetFieldValues)>0){
                             return Pair(false,it.advice)
                         }
                    }
                }
            }
            return Pair(true,"")
        }
}