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

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import work.bg.server.core.context.ModelExpressionContext
import work.bg.server.core.acrule.ModelRuleCriteriaProcessor
import work.bg.server.core.context.CriteriaStatementUtil
import work.bg.server.core.mq.ModelBase
import work.bg.server.core.mq.ModelCriteria
import work.bg.server.core.mq.ModelExpression

@Scope(value = "prototype")
class ModelRuleCriteriaProcessorBean:ModelRuleCriteriaProcessor {
    override fun parse(criteriaStatement: String,model:ModelBase?, context: ModelExpressionContext): ModelExpression? {
        return CriteriaStatementUtil.parse(criteriaStatement,model,context)
    }
}