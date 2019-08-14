/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.chat.billboard

import work.bg.server.core.model.BasePartner
import work.bg.server.core.model.billboard.ConstantFieldBillboard
import work.bg.server.core.model.billboard.FieldDefaultValueBillboard
import java.util.*

class ChatGuidBillboard(override val constant: Boolean=false) : FieldDefaultValueBillboard, ConstantFieldBillboard {
    override fun looked(glass: Any?): Any {
        return UUID.randomUUID().toString()
    }
}