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
import work.bg.server.core.acrule.ModelDeleteAccessControlRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.*

@Component
class ModelDeleteFieldsBelongToPartnerCheckBean: ModelDeleteAccessControlRule<Unit> {
    private lateinit var _config:String
    override var config: String
        get() = _config//To change initializer of created properties use File | Settings | File Templates.
        set(value) {
            _config=value
        }

    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Unit?): Pair<Boolean, String> {
        if(!partnerCache.checkEditBelongToPartner(modelData.model!!)){
            return Pair(true,"")
        }
        val idFV = modelData.idFieldValue
        idFV?.let {
            var targetFieldValues = FieldValueArray()
            targetFieldValues.setValue(it.field,it.value)
            val m = modelData.model as AccessControlModel
            targetFieldValues.setValue(m.createPartnerID,partnerCache.partnerID)
            targetFieldValues.setValue(m.createCorpID,partnerCache.corpID)

            var expArr = targetFieldValues.map {
                eq(it.field,it.value)!!
            }.toTypedArray()
            var expressions = and(*expArr)
            var statement = select(fromModel = modelData.model!!).count().where(expressions)
            val count= modelData.model?.queryCount(statement)
            count?.let {
                if(count<1){
                    return Pair(false,"无删除权限")
                }
            }
        }
        return Pair(true,"")
    }
}