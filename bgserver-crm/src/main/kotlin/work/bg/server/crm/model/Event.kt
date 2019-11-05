/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
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
import work.bg.server.core.model.field.EventLogField

@Model(name="event",title = "活动")
class Event:ContextModel("crm_event","public") {
    companion object : RefSingleton<Event> {
        override lateinit var ref: Event
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val name = dynamic.model.query.mq.ModelField(null, "name", dynamic.model.query.mq.FieldType.STRING, title = "名称", defaultValue = "")
    val budgetCost = dynamic.model.query.mq.ModelField(null, "budget_cost", dynamic.model.query.mq.FieldType.DECIMAL, title = "预算", defaultValue = 0)
    val actualCost = dynamic.model.query.mq.ModelField(null, "actual_cost", dynamic.model.query.mq.FieldType.DECIMAL, title = "实际花费", defaultValue = 0)
    val comment = dynamic.model.query.mq.ModelField(null, "comment", dynamic.model.query.mq.FieldType.STRING, title = "注释", defaultValue = "")
    val startTime = dynamic.model.query.mq.ModelField(null, "start_time", dynamic.model.query.mq.FieldType.STRING, title = "开始时间")
    val endTime = dynamic.model.query.mq.ModelField(null, "end_time", dynamic.model.query.mq.FieldType.STRING, title = "结束时间")

    //负责人
    val magPartners = dynamic.model.query.mq.ModelMany2ManyField(null,
            "manager_partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            title = "负责人",
            relationModelTable = "public.crm_partner_event_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")

    val leads = dynamic.model.query.mq.ModelOne2ManyField(null, "leads", dynamic.model.query.mq.FieldType.BIGINT, "线索",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "event_id")

    val customers = dynamic.model.query.mq.ModelOne2ManyField(null,
            "customers",
            dynamic.model.query.mq.FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "event_id")
    val eventLogs = EventLogField(null,"event_logs","跟踪日志")


    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "名称必须填写"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空！")
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "名称必须填写"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空！")
        )
    }
    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {

        return arrayOf(
                ModelFieldUnique(this.name,advice = "名称必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "名称必须唯一",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }
}