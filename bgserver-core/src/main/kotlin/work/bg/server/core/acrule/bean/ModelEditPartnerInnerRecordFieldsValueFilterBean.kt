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
import work.bg.server.core.acrule.ModelEditRecordFieldsValueFilterRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartner
import dynamic.model.query.mq.ModelDataObject

@Component
class ModelEditPartnerInnerRecordFieldsValueFilterBean: ModelEditRecordFieldsValueFilterRule<Unit,String> {
    private lateinit var _config:String
    override var config: String
        get() = _config
        set(value) {
            _config=value
        }

    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Unit?): Pair<Boolean, String> {
        if(modelData.hasFieldValue(BasePartner.ref.password)){
            var password = modelData.getFieldValue(BasePartner.ref.password)
            if(password!=null && !(password as String).isNullOrEmpty()){
                modelData.setFieldValue(BasePartner.ref.password, work.bg.server.util.MD5.hash(password))
            }
            else{
                modelData.removeFieldValue(BasePartner.ref.password)
            }
        }
        return Pair(true,"")
    }
}