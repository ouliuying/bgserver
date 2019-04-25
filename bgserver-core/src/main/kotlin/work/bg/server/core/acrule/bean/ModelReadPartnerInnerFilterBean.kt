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
import work.bg.server.core.acrule.ModelReadFieldFilterRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartner
import work.bg.server.core.mq.FieldBase

@Component
class ModelReadPartnerInnerFilterBean:ModelReadFieldFilterRule {
    private lateinit var _config:String
    override var config: String
        get() = _config
        set(value) {
            _config=value
        }

    override fun invoke(field: FieldBase, partnerCache: PartnerCache, data: Any?): Pair<Boolean, String> {
        if(field.isSame(BasePartner.ref.password)){
           return Pair(true,"")
        }
        return Pair(false,"")
    }
}