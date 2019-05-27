package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model(name="lead",title = "活动")
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
    val comment = ModelField(null,"comment", FieldType.STRING,title = "名称",defaultValue = "")
    val startTime = ModelField(null,"start_time", FieldType.STRING,title = "开始时间")
    val endTime = ModelField(null,"end_time", FieldType.STRING,title = "结束时间")

    //负责人
    val magPargtners = ModelMany2ManyField(null,
            "manager_partner_id",
            FieldType.BIGINT,
            title = "负责人",
            relationModelTable = "public.crm_partner_event_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")

    val leads = ModelOne2ManyField(null,"leads",FieldType.BIGINT,"线索",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "event")

    val customers = ModelOne2ManyField(null,
            "customers",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "event")
}