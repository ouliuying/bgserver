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

import dynamic.model.query.mq.FieldForeignKey
import dynamic.model.query.mq.ForeignKeyAction
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model("smsTimerQueue","定时短信")
class SmsTimerQueue:ContextModel("sms_timer_queue","public") {
    companion object : RefSingleton<SmsTimerQueue> {
        override lateinit var ref: SmsTimerQueue
    }
    val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.BIGINT, "标识", primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val sendPartner = dynamic.model.query.mq.ModelMany2OneField(null, "partner_id", dynamic.model.query.mq.FieldType.BIGINT, "发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))
    val mobiles = dynamic.model.query.mq.ModelField(null, "mobiles", dynamic.model.query.mq.FieldType.STRING, "号码")
    val message = dynamic.model.query.mq.ModelField(null, "message", dynamic.model.query.mq.FieldType.STRING, "信息")
    val timerType = dynamic.model.query.mq.ModelField(null, "timer_type", dynamic.model.query.mq.FieldType.INT, "定时类型", comment = "")
    val timerValue = dynamic.model.query.mq.ModelField(null, "timer_value", dynamic.model.query.mq.FieldType.DATETIME, "定时")
    val addTime = dynamic.model.query.mq.ModelField(null, "add_time", dynamic.model.query.mq.FieldType.DATETIME, "添加时间")
    val ip = dynamic.model.query.mq.ModelField(null, "ip", dynamic.model.query.mq.FieldType.STRING, "IP")
    override fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

}