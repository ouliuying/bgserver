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

package work.bg.server.chat.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelOne2OneField
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.sms.model.SmsPartner

@Model("partner")
class ChatPartner: SmsPartner() {
    companion object: RefSingleton<ChatPartner> {
        override lateinit var ref: ChatPartner
    }

    val chatUUID = ModelOne2OneField(null,
            "chat_partner_uuid",
            FieldType.BIGINT,
            "通讯UUID",
            isVirtualField = true,
            targetModelTable = "public.chat_partner_uuid",
            targetModelFieldName = "partner_id")
}