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