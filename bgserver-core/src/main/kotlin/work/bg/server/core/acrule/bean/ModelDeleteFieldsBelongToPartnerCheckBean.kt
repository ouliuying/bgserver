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

import dynamic.model.query.mq.and
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.select
import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelDeleteAccessControlRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel

@Component
class ModelDeleteFieldsBelongToPartnerCheckBean: ModelDeleteAccessControlRule<Unit> {
    private lateinit var _config:String
    override var config: String
        get() = _config//To change initializer of created properties use File | Settings | File Templates.
        set(value) {
            _config=value
        }

    override fun invoke(modelData: dynamic.model.query.mq.ModelDataObject, partnerCache: PartnerCache, data: Unit?): Pair<Boolean, String> {
        if(!partnerCache.checkEditBelongToPartner(modelData.model!!)){
            return Pair(true,"")
        }
        val idFV = modelData.idFieldValue
        idFV?.let {
            var targetFieldValues = dynamic.model.query.mq.FieldValueArray()
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