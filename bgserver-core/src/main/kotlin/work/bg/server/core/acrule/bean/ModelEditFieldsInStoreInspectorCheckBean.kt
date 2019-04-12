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
        var idFV: FieldValue = modelData.idFieldValue ?: return Pair(false,"")
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