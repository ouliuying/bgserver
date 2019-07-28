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