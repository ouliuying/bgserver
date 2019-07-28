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

@Model("smsSendHistory")
class SmsSendHistory:ContextModel("sms_send_history","public") {
    companion object : RefSingleton<SmsSendHistory> {
        override lateinit var ref: SmsSendHistory
    }
    val id= ModelField(null,"id", FieldType.BIGINT,"标识",primaryKey = FieldPrimaryKey())
    val sendPartner = ModelMany2OneField(null,"partner_id",FieldType.BIGINT,"发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")
    val mobile = ModelField(null,"mobile",FieldType.STRING,"号码")
    val message = ModelField(null,"message",FieldType.STRING,"信息")
    val smsCount = ModelField(null,"sms_count",FieldType.INT,"短信条数")
    val sendType = ModelField(null,"send_type",FieldType.INT,"类型",comment = "0：普通，1：定时")
    val sendTime = ModelField(null,"send_time",FieldType.DATETIME,"发送时间")
    val status = ModelField(null,"status",FieldType.INT,"状态",defaultValue = -1)
    val statusDesc = ModelField(null,"status_desc",FieldType.STRING,"状态")
    val msgID = ModelField(null,"msg_id",FieldType.STRING,"信息标识")
    val ip = ModelField(null,"ip",FieldType.STRING,"IP")
    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}