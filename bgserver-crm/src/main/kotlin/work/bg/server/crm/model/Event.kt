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

import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="event",title = "活动")
class Event:ContextModel("crm_event","public") {
    companion object : RefSingleton<Event> {
        override lateinit var ref: Event
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val name = ModelField(null,"name", FieldType.STRING,title = "名称",defaultValue = "")
    val budgetCost = ModelField(null,"budget_cost", FieldType.DECIMAL,title = "预算",defaultValue=0)
    val actualCost = ModelField(null,"actual_cost", FieldType.DECIMAL,title = "实际花费",defaultValue=0)
    val comment = ModelField(null,"comment", FieldType.STRING,title = "注释",defaultValue = "")
    val startTime = ModelField(null,"start_time", FieldType.STRING,title = "开始时间")
    val endTime = ModelField(null,"end_time", FieldType.STRING,title = "结束时间")

    //负责人
    val magPartners = ModelMany2ManyField(null,
            "manager_partner_id",
            FieldType.BIGINT,
            title = "负责人",
            relationModelTable = "public.crm_partner_event_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")

    val leads = ModelOne2ManyField(null,"leads",FieldType.BIGINT,"线索",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "event_id")

    val customers = ModelOne2ManyField(null,
            "customers",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "event_id")

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