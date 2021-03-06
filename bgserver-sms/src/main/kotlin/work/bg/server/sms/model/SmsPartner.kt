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

package work.bg.server.sms.model

import dynamic.model.query.mq.RefSingleton
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.crm.model.CrmPartner

@Model("partner" ,"员工")
class SmsPartner:CrmPartner() {
    companion object : RefSingleton<SmsPartner> {
        override lateinit var ref: SmsPartner
    }
    val smsJobLog = dynamic.model.query.mq.ModelOne2ManyField(null,
            "sms_job_log", dynamic.model.query.mq.FieldType.BIGINT,
            "短信发送Job",
            targetModelTable = "public.sms_send_job_log",
            targetModelFieldName = "partner_id")
    val smsSendHistory = dynamic.model.query.mq.ModelOne2ManyField(null,
            "sms_send_history",
            dynamic.model.query.mq.FieldType.BIGINT,
            "短信发送记录",
            targetModelTable = "public.sms_send_history",
            targetModelFieldName = "partner_id")
    val smsTimerQueue = dynamic.model.query.mq.ModelOne2ManyField(null,
            "sms_timer_queue",
            dynamic.model.query.mq.FieldType.BIGINT,
            "短信定时队列",
            targetModelTable = "public.sms_timer_queue",
            targetModelFieldName = "partner_id")
    val smsReplyMessageHistory = dynamic.model.query.mq.ModelOne2ManyField(null,
            "sms_reply_message_history",
            dynamic.model.query.mq.FieldType.BIGINT,
            "回复信息",
            targetModelTable = "public.sms_reply_message_history",
            targetModelFieldName = "partner_id")
}