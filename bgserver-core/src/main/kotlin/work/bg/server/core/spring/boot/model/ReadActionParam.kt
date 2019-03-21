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

package work.bg.server.core.spring.boot.model

import work.bg.server.core.mq.AttachedField
import work.bg.server.core.mq.FieldBase
import work.bg.server.core.mq.ModelExpression
import work.bg.server.core.mq.OrderBy

data class ReadActionParam(val fields:ArrayList<FieldBase>?=null,
                           val criteria:ModelExpression?=null,
                           val attachedFields:ArrayList<AttachedField>?=null,
                           val orderBy: OrderBy?=null,
                           val pageSize:Int=10,
                           val pageIndex:Int=1)