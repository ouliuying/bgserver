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

package work.bg.server.core.mq.join

import work.bg.server.core.mq.ModelBase
import work.bg.server.core.mq.ModelCriteria
import work.bg.server.core.mq.ModelExpression
import work.bg.server.core.mq.OrderBy

private class  JoinTag{
    companion object {
        const val INNER_JOIN=" INNER JOIN "
        const val LEFT_JOIN=" LEFT JOIN  "
        const val RIGHT_JOIN=" RIGHT JOIN "
        const val LATERAL_INNER_JOIN=" LATERAL INNER JOIN "
        const val LATERAL_LEFT_JOIN=" LATERAL INNER JOIN "
    }
}
fun innerJoin(model: ModelBase?, onConditions: ModelExpression,criteria:ModelExpression?=null)=JoinModel(model,onConditions,JoinTag.INNER_JOIN,criteria)
fun leftJoin(model: ModelBase?, onConditions: ModelExpression,criteria:ModelExpression?=null)=JoinModel(model,onConditions,JoinTag.LEFT_JOIN,criteria)
fun rightJoin(model: ModelBase?, onConditions: ModelExpression,criteria:ModelExpression?=null)=JoinModel(model,onConditions,JoinTag.RIGHT_JOIN,criteria)

fun lateralInnerJoin(model: ModelBase?,onCondition: ModelExpression,criteria: ModelExpression?=null,fetchCount:Int?=null,orderBy: OrderBy?=null)=
        LateralJoinModel(model,JoinTag.LATERAL_INNER_JOIN,onCondition,criteria,fetchCount,orderBy )
fun lateralLeftJoin(model: ModelBase?,onCondition: ModelExpression,criteria: ModelExpression?=null,fetchCount:Int?=null,orderBy: OrderBy?=null)=
        LateralJoinModel(model,JoinTag.LATERAL_LEFT_JOIN,onCondition,criteria,fetchCount,orderBy )