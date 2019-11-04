/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.core.acrule.bean

import dynamic.model.query.mq.*
import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelDeleteAccessControlRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
@Component
class ModelDeleteCorpIsolationBean : ModelDeleteAccessControlRule<ModelExpression?,ModelExpression?> {
    private lateinit var _config:String
    override var config: String
        get() = _config//To change initializer of created properties use File | Settings | File Templates.
        set(value) {
            _config=value
        }

    override fun invoke(modelData: ModelDataObject,
                        partnerCache: PartnerCache,
                        criteria: ModelExpression?): Pair<Boolean, ModelExpression?> {
         val model = modelData.model as AccessControlModel?
         if(model?.corpIsolationFields() != null){
             var isoCriteria = eq(model.createCorpID,partnerCache.corpID)

             return Pair(true, if(criteria!=null) and(criteria,isoCriteria) else isoCriteria)
         }
         return Pair(true,criteria)
    }
}