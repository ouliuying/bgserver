/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  * GNU Lesser General Public License Usage
 *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  * General Public License version 3 as published by the Free Software
 *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  * project of this file. Please review the following information to
 *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *
 *
 */

package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="customerStatus",title = "线索状态")
class LeadInteractionStatus:ContextModel("crm_lead_interaction_status","public") {
    companion object : RefSingleton<LeadInteractionStatus> {
        override lateinit var ref: LeadInteractionStatus
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name = ModelField(null,
            "name",
            FieldType.STRING,
            title = "名称",
            defaultValue = "")

    val leads=ModelOne2ManyField(null,
            "leads",FieldType.INT,
            "线索",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "interaction_status_id")
}