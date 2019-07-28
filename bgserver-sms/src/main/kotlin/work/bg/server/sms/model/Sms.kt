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

package work.bg.server.sms.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.crm.model.CrmPartner

@Model("sms")
class Sms:ContextModel("sms","public") {
    companion object : RefSingleton<Sms> {
        override lateinit var ref: Sms
    }

    @Action("sendSms")
    fun sendSms():ActionResult?{
        var ar=ActionResult()

        return ar
    }

    @Action("importSendSms")
    fun importSendSms():ActionResult?{
        var ar=ActionResult()
        return ar
    }

}