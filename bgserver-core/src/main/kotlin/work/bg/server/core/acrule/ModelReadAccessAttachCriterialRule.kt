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

package work.bg.server.core.acrule

import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.ModelBase
import work.bg.server.core.mq.ModelExpression


interface ModelReadAccessAttachCriterialRule<T>:ModelReadAccessControlRule<T> {
     operator fun invoke(model: ModelBase, partnerCache: PartnerCache,criteria:ModelExpression?):ModelExpression?
     var config:String
}