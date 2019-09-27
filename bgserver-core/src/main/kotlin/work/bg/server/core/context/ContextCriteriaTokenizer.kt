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

package work.bg.server.core.context

import dynamic.model.query.mq.ModelCriteria

/*
    针对crud 添加 context statement string ,然后在model action 中动态 附加 model criteria
 */
class ContextCriteriaTokenizer(val criteriaStatement:String?=null,modelExpressionContext: ModelExpressionContext?=null) {
    private var modelCriteria: ModelCriteria?=null
    init {
        this.parseImp()
    }
    //TODO NEXT VERSION
    private fun parseImp(){

    }
    fun toModelCriteria():ModelCriteria?{
        return modelCriteria
    }
}