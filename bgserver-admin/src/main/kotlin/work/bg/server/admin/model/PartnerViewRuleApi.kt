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

package work.bg.server.admin.model

import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.RefSingleton
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel

@Model("partnerViewRuleApi","员工角色视图管理")
class PartnerViewRuleApi: ContextModel("base_partner_view_rule_api","public") {
    companion object: RefSingleton<PartnerViewRuleApi> {
        override lateinit var ref: PartnerViewRuleApi
    }
    override fun isDynamic(): Boolean {
        return true
    }

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {


    }
}