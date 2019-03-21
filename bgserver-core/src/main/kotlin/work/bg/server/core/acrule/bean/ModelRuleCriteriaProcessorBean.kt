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