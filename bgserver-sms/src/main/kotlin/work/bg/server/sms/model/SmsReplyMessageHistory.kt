/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.sms.model

import dynamic.model.query.mq.RefSingleton
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.model.ContextModel

@Model("smsReplyMessageHistory")
class SmsReplyMessageHistory:ContextModel("sms_reply_message_history",
        "public") {
    companion object : RefSingleton<SmsReplyMessageHistory> {
        override lateinit var ref: SmsReplyMessageHistory
    }
    val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.BIGINT, "标识", primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val receivePartner = dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")
    val mobile = dynamic.model.query.mq.ModelField(null, "mobile", dynamic.model.query.mq.FieldType.STRING, "号码")
    val message = dynamic.model.query.mq.ModelField(null, "message", dynamic.model.query.mq.FieldType.STRING, "信息")
}