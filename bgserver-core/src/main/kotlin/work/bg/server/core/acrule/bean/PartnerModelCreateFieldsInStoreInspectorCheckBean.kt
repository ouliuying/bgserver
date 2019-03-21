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
import work.bg.server.core.acrule.ModelCreateRecordFieldsValueCheckInStoreControlRule
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.FieldValueArray
import work.bg.server.core.mq.ModelDataObject
@Component
class PartnerModelCreateFieldsInStoreInspectorCheckBean:
        ModelCreateRecordFieldsValueCheckInStoreControlRule<Array<ModelFieldInspector>> {
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
                         if((modelData.model as AccessControlModel).rawCount(targetFieldValues)>0){
                             return Pair(false,it.advice)
                         }
                    }
                }
            }
            return Pair(true,"")
        }
}