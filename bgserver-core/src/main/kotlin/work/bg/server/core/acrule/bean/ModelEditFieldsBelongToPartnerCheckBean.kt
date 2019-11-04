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

import dynamic.model.query.mq.*
import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelEditRecordFieldsValueCheckRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel

@Component
class ModelEditFieldsBelongToPartnerCheckBean: ModelEditRecordFieldsValueCheckRule<ModelExpression?,String> {
    private lateinit var _config:String
    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, criteria: ModelExpression?): Pair<Boolean, String> {
        if(!partnerCache.checkEditBelongToPartner(modelData.model!!)){
            return Pair(true,"")
        }
        var targetFieldValues = dynamic.model.query.mq.FieldValueArray()
        val m = modelData.model as AccessControlModel
        targetFieldValues.setValue(m.createPartnerID,partnerCache.partnerID)
        targetFieldValues.setValue(m.createCorpID,partnerCache.corpID)
        val idFV = modelData.idFieldValue
        if(idFV!=null){
            targetFieldValues.setValue(idFV.field,idFV.value)
        }
        var expArr = targetFieldValues.map {
            eq(it.field, it.value)
        }.toTypedArray()
        var expressions = and(*expArr)
        expressions = if(criteria!=null) and(expressions,criteria) else expressions
        var statement = select(fromModel = modelData.model!!).count().where(expressions)
        val count= modelData.model?.queryCount(statement)
        count?.let {
            if(count<1){
                return Pair(false,"无更新权限")
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