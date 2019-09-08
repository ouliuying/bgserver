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

package work.bg.server.core.ui

import dynamic.model.query.mq.model.AppModel
import work.bg.server.util.MethodInvocation




//TODO cache instance method,call method with parameters
object ModelViewFieldSourceCache {
    fun run(source:ModelViewFieldSource):Any?{
        var model = AppModel.ref.getModel(source.app,source.model)
        model?.let {
            return MethodInvocation(model,source.method)()
        }
        return null
    }
}