package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.BasePartner
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelMany2ManyField
import work.bg.server.core.mq.ModelOne2ManyField
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.corp.model.DepartmentPartner

@Model("partner")
class CrmPartner:DepartmentPartner() {
    companion object : RefSingleton<CrmPartner> {
        override lateinit var ref: CrmPartner
    }

    val events = ModelMany2ManyField(null,
            "crm_event_id",
            FieldType.BIGINT,
            "促销活动",
            relationModelTable = "public.crm_partner_rel",
            relationModelFieldName ="partner_id",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id")

    val  customers = ModelMany2ManyField(null,
            "crm_customer_id",
            FieldType.BIGINT,
            "我的客户",
            relationModelTable = "public.crm_partner_customer_rel",
            relationModelFieldName = "customer_id",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id")

    val  leads = ModelMany2ManyField(null,
            "crm_lead_id",
            FieldType.BIGINT,
            "我的线索",
            relationModelTable = "public.crm_partner_lead_rel",
            relationModelFieldName = "lead_id",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "id")
    val orderInvoices = ModelOne2ManyField(null,
            "invoice",
            FieldType.BIGINT,
            "发票",
            targetModelTable = "public.crm_customer_order_invoice",
            targetModelFieldName = "accountPartner")
    val orderReceipts = ModelOne2ManyField(null,
            "receipt",
            FieldType.BIGINT,
            "收据",
            targetModelTable = "public.crm_customer_order_receipt",
            targetModelFieldName = "accountPartner")

}