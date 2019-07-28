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
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelOne2ManyField
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.crm.model.CrmPartner

@Model("partner")
class SmsPartner:CrmPartner() {
    companion object : RefSingleton<SmsPartner> {
        override lateinit var ref: SmsPartner
    }
    val smsJobLog = ModelOne2ManyField(null,"sms_job_log",FieldType.BIGINT,
            "短信发送Job",targetModelTable = "public.sms_send_job_log",targetModelFieldName = "partner_id")
    val smsSendHistory = ModelOne2ManyField(null,"sms_send_history",FieldType.BIGINT,
            "短信发送记录",targetModelTable = "public.sms_send_history",targetModelFieldName = "partner_id")
    val smsTimerQueue = ModelOne2ManyField(null,"sms_timer_queue",FieldType.BIGINT,
            "短信定时队列",targetModelTable = "public.sms_timer_queue",targetModelFieldName = "partner_id")
}