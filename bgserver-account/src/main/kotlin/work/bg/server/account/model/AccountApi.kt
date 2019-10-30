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

package work.bg.server.account.model

import com.google.gson.JsonObject
import dynamic.model.query.mq.RefSingleton
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel

@Model("accountApi")
class AccountApi:ContextModel("account_api","public") {
    companion object: RefSingleton<AccountApi> {
        override lateinit var ref: AccountApi
    }
    @Action("loadTopListData")
    fun loadTopListData(@RequestBody data: JsonObject?,
                        partnerCache: PartnerCache):Any?{
        var ar=ActionResult()

        return ar
    }
}