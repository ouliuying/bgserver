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

import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("smsTimerQueue")
class SmsTimerQueue:ContextModel("sms_timer_queue","public") {
    companion object : RefSingleton<SmsTimerQueue> {
        override lateinit var ref: SmsTimerQueue
    }
    val id= ModelField(null,"id", FieldType.BIGINT,"标识",primaryKey = FieldPrimaryKey())
    val sendPartner = ModelMany2OneField(null,"partner_id", FieldType.BIGINT,"发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")
    val mobiles = ModelField(null,"mobiles", FieldType.STRING,"号码")
    val message = ModelField(null,"message", FieldType.STRING,"信息")
    val timerType = ModelField(null,"timer_type", FieldType.INT,"定时类型",comment = "")
    val timerValue = ModelField(null,"timer_value", FieldType.DATETIME,"定时")
    val addTime = ModelField(null,"add_time", FieldType.DATETIME,"添加时间")
    val ip = ModelField(null,"ip",FieldType.STRING,"IP")
    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

}