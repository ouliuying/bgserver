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
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("smsSendJobLog")
class SmsSendJobLog:ContextModel("sms_send_job_log","public") {
    companion object : RefSingleton<SmsSendJobLog> {
        override lateinit var ref: SmsSendJobLog
    }
    val id= ModelField(null,"id", FieldType.BIGINT,"标识",primaryKey = FieldPrimaryKey())
    val jobName = ModelField(null,"job_name",FieldType.STRING,"job名字")
    val jobGroup = ModelField(null,"job_group",FieldType.STRING,"job组名")
    val triggerName = ModelField(null,"trigger_name",FieldType.STRING,"trigger名字")
    val triggerGroup = ModelField(null,"trigger_group",FieldType.STRING,"trigger组名")
    val sendPartner = ModelMany2OneField(null,"partner_id",FieldType.BIGINT,"发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",defaultValue = CurrPartnerBillboard())

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}