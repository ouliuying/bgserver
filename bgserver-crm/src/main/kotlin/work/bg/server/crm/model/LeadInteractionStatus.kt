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

package work.bg.server.crm.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Model

@Model(name="leadInteractionStatus",title = "线索状态")
class LeadInteractionStatus:ContextModel("crm_lead_interaction_status","public") {
    companion object : RefSingleton<LeadInteractionStatus> {
        override lateinit var ref: LeadInteractionStatus
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val name = dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            title = "名称",
            defaultValue = "")

    val leads= dynamic.model.query.mq.ModelOne2ManyField(null,
            "leads", dynamic.model.query.mq.FieldType.BIGINT,
            "线索",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "interaction_status_id")

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "必须输入名称"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空")
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "必须输入名称"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空")
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "名称必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "名称必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }
}