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

package work.bg.server.worktable.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import javax.servlet.http.HttpSession

@Model("worktable")
class Worktable:ContextModel("worktable","public") {
    companion object : RefSingleton<Worktable> {
        override lateinit var ref: Worktable
    }

    @Action("loadModelLog")
    fun loadModelLog(session: HttpSession):Any?{
        var ar= ActionResult()

        return ar
    }
}