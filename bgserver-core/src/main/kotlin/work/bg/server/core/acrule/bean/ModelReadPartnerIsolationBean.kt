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

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelReadIsolationRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.ModelBase
import work.bg.server.core.mq.ModelExpression
import work.bg.server.core.mq.and
import work.bg.server.core.mq.eq

@Component
class ModelReadPartnerIsolationBean:ModelReadIsolationRule<ModelExpression> {
    private lateinit var _config:String
    override var config: String
        get() = _config //To change initializer of created properties use File | Settings | File Templates.
        set(value) {
            _config=value
        }

    override fun invoke(model: ModelBase, partnerCache: PartnerCache, criteria: ModelExpression?): ModelExpression? {
        if(!partnerCache.checkReadBelongToPartner(model)){
            return null
        }
        if(!model.skipCorpIsolationFields())
        {
            var acModel = model as AccessControlModel
            return if(criteria!=null){
                and(eq(acModel.createPartnerID,partnerCache.partnerID)!!,criteria)
            } else{
                eq(acModel.createPartnerID,partnerCache.partnerID)
            }
        }
        return null
    }
}