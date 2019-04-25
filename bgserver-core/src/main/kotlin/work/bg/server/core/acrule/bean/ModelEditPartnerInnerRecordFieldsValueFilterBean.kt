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
import work.bg.server.core.acrule.ModelEditRecordFieldsValueFilterRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartner
import work.bg.server.core.mq.ModelDataObject

@Component
class ModelEditPartnerInnerRecordFieldsValueFilterBean: ModelEditRecordFieldsValueFilterRule<Unit> {
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
                modelData.setFieldValue(BasePartner.ref.password,util.MD5.hash(password))
            }
            else{
                modelData.removeFieldValue(BasePartner.ref.password)
            }
        }
        return Pair(true,"")
    }
}