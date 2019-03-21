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
import work.bg.server.core.acrule.ModelCreateRecordFieldsInitializeRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.FieldValue
import work.bg.server.core.mq.ModelDataObject
import work.bg.server.core.mq.ModelField

@Component
class ModelCreateFieldsSetIsolationFieldsBean:ModelCreateRecordFieldsInitializeRule<Any> {
    private lateinit var _config:String
    override  var config: String
        get() {
            return this._config
        }
        set(value) {
            this._config=value
        }
    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Any?): Pair<Boolean, String> {
        if(modelData.model!=null && !(modelData.model as AccessControlModel).skipCorpIsolationFields()){
            var createCorpID = (modelData.model as AccessControlModel).createCorpID
            var lastModifyCorpID = (modelData.model as AccessControlModel).lastModifyCorpID
            var createPartnerID = (modelData.model as AccessControlModel).createPartnerID
            var lastModifyPartnerID = (modelData.model as AccessControlModel).lastModifyPartnerID
            var createTime = (modelData.model as AccessControlModel).createTime
            var lastModifyTime = (modelData.model as AccessControlModel).lastModifyTime
            this.setFieldValue(modelData,createCorpID,partnerCache.corpID)
            this.setFieldValue(modelData,lastModifyCorpID,partnerCache.corpID)
            this.setFieldValue(modelData,createPartnerID,partnerCache.partnerID)
            this.setFieldValue(modelData,lastModifyPartnerID,partnerCache.partnerID)
            this.setFieldValue(modelData,createTime,util.Time.now())
            this.setFieldValue(modelData,lastModifyTime,util.Time.now())
        }
        return Pair(true,"")
    }
    private  fun setFieldValue(modelData: ModelDataObject,field:ModelField,value:Any?){
        var index=modelData.data?.setValue(field,value)
       if(index<0){
            modelData.fields?.add(field)
        }
    }
    operator fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache): Pair<Boolean, String> {
       return this.invoke(modelData,partnerCache,null)
    }

}