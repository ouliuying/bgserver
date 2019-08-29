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