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
import work.bg.server.core.acrule.ModelEditRecordFieldsValueCheckInStoreRule
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.*

@Component
class ModelEditFieldsInStoreInspectorCheckBean : ModelEditRecordFieldsValueCheckInStoreRule<Array<ModelFieldInspector>> {
    private lateinit var _config:String
    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Array<ModelFieldInspector>?): Pair<Boolean, String> {
        var idFV: FieldValue = modelData.idFieldValue ?: return Pair(true,"")
        data?.forEach {
            when(it){
                is ModelFieldUnique ->{
                    var targetFieldValues= FieldValueArray()
                    modelData.data.forEach {fv->
                        if(it.targetFields.count { sit->sit.isSame(fv.field) }>0){
                            targetFieldValues.setValue(fv.field, fv.value)
                        }
                    }
                    var expArr = targetFieldValues.map {ifv->
                        eq(ifv.field,ifv.value)!!
                    }.toTypedArray()
                    if(it.isolationType==ModelFieldUnique.IsolationType.IN_PARTNER){
                        val m = modelData.model as AccessControlModel
                        targetFieldValues.setValue(m.createCorpID,partnerCache.corpID)
                       if(!modelData.hasFieldValue(m.createPartnerID)){
                           targetFieldValues.setValue(m.createPartnerID, partnerCache.partnerID)
                       }
                    }
                    else if(it.isolationType == ModelFieldUnique.IsolationType.IN_CORP){
                        val m = modelData.model as AccessControlModel
                        targetFieldValues.setValue(m.createCorpID,partnerCache.corpID)
                    }
                    var expressions = and(*expArr, notEq(idFV.field,idFV.value)!!)
                    if((modelData.model as AccessControlModel).rawCount(expressions)>0){
                        return Pair(false,it.advice)
                    }
                }
            }
        }
        return Pair(true,"")
    }

    override var config: String
        get() = _config//To change initializer of created properties use File | Settings | File Templates.
        set(value) {
            _config=value
        }
}