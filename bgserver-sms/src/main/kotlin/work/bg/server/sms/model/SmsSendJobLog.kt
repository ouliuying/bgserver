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
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import dynamic.model.web.spring.boot.annotation.Model

@Model("smsSendJobLog","提交日志")
class SmsSendJobLog:ContextModel("sms_send_job_log","public") {
    companion object : RefSingleton<SmsSendJobLog> {
        override lateinit var ref: SmsSendJobLog
    }
    val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.BIGINT, "标识", primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val jobName = dynamic.model.query.mq.ModelField(null, "job_name", dynamic.model.query.mq.FieldType.STRING, "job名字")
    val jobGroup = dynamic.model.query.mq.ModelField(null, "job_group", dynamic.model.query.mq.FieldType.STRING, "job组名")
    val triggerName = dynamic.model.query.mq.ModelField(null, "trigger_name", dynamic.model.query.mq.FieldType.STRING, "trigger名字")
    val triggerGroup = dynamic.model.query.mq.ModelField(null, "trigger_group", dynamic.model.query.mq.FieldType.STRING, "trigger组名")
    val sendPartner = dynamic.model.query.mq.ModelMany2OneField(null, "partner_id", dynamic.model.query.mq.FieldType.BIGINT, "发送人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id", defaultValue = CurrPartnerBillboard(),foreignKey = FieldForeignKey(action = ForeignKeyAction.SET_NULL))

    override fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}