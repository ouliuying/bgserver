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

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelReadIsolationRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import dynamic.model.query.mq.ModelExpression
import dynamic.model.query.mq.and
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.model.ModelBase

@Component
class ModelReadCorpIsolationBean:ModelReadIsolationRule<dynamic.model.query.mq.ModelExpression> {
    private lateinit var _config:String
    override var config: String
        get() = _config
        set(value) {
            _config=value
        }

    override fun invoke(model: ModelBase,
                        partnerCache: PartnerCache,
                        criteria: dynamic.model.query.mq.ModelExpression?): dynamic.model.query.mq.ModelExpression? {
        if((model as AccessControlModel).corpIsolationFields()!=null)
        {
            var acModel = model as AccessControlModel
            return if(criteria!=null){
                and(eq(acModel.createCorpID,partnerCache.corpID)!!,criteria)
            } else{
                eq(acModel.createCorpID,partnerCache.corpID)
            }
        }
        return null
    }
}