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
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model("smsSendHistory")
class SmsSendHistory:ContextModel("sms_send_history","public") {
    companion object : RefSingleton<SmsSendHistory> {
        override lateinit var ref: SmsSendHistory
    }
    val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.BIGINT, "标识", primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val sendPartner = dynamic.model.query.mq.ModelMany2OneField(null, "partner_id", dynamic.model.query.mq.FieldType.BIGINT, "发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")
    val mobile = dynamic.model.query.mq.ModelField(null, "mobile", dynamic.model.query.mq.FieldType.STRING, "号码")
    val message = dynamic.model.query.mq.ModelField(null, "message", dynamic.model.query.mq.FieldType.STRING, "信息")
    val smsCount = dynamic.model.query.mq.ModelField(null, "sms_count", dynamic.model.query.mq.FieldType.INT, "短信条数")
    val sendType = dynamic.model.query.mq.ModelField(null, "send_type", dynamic.model.query.mq.FieldType.INT, "类型", comment = "0：普通，1：定时")
    val sendTime = dynamic.model.query.mq.ModelField(null, "send_time", dynamic.model.query.mq.FieldType.DATETIME, "发送时间")
    val status = dynamic.model.query.mq.ModelField(null, "status", dynamic.model.query.mq.FieldType.INT, "状态", defaultValue = -1)
    val statusDesc = dynamic.model.query.mq.ModelField(null, "status_desc", dynamic.model.query.mq.FieldType.STRING, "状态")
    val msgID = dynamic.model.query.mq.ModelField(null, "msg_id", dynamic.model.query.mq.FieldType.STRING, "信息标识")
    val ip = dynamic.model.query.mq.ModelField(null, "ip", dynamic.model.query.mq.FieldType.STRING, "IP")
    override fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}