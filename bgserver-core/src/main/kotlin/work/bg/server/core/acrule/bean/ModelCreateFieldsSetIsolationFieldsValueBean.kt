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
import work.bg.server.core.model.AccessControlModel
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.ModelField

@Component
class ModelCreateFieldsSetIsolationFieldsValueBean:ModelCreateRecordFieldsValueInitializeRule<Any> {
    private lateinit var _config:String
    override  var config: String
        get() {
            return this._config
        }
        set(value) {
            this._config=value
        }
    override fun invoke(modelData: ModelDataObject,
                        partnerCache: PartnerCache,
                        data: Any?): Pair<Boolean, String> {
        if(modelData.model!=null && (modelData.model as AccessControlModel).corpIsolationFields()!=null){
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
            this.setFieldValue(modelData,createTime, work.bg.server.util.Time.now())
            this.setFieldValue(modelData,lastModifyTime, work.bg.server.util.Time.now())
        }
        return Pair(true,"")
    }
    private  fun setFieldValue(modelData: ModelDataObject, field: ModelField, value:Any?){
        var index= modelData.data.setValue(field,value)
       if(index<0){
            modelData.fields?.add(field)
        }
    }
    operator fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache): Pair<Boolean, String> {
       return this.invoke(modelData,partnerCache,null)
    }

}