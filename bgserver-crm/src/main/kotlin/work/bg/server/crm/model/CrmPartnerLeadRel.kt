package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("crmPartnerLeadRel")
class CrmPartnerLeadRel:ContextModel("crm_partner_lead_rel","public") {
    companion object : RefSingleton<CrmPartnerLeadRel> {
        override lateinit var ref: CrmPartnerLeadRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val partner= ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "用户",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))

    val lead= ModelMany2OneField(null,
            "lead_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_lead",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))
    val ownFlag = ModelField(null,
            "ownFlag",
            FieldType.INT,
            "占有",
            defaultValue = 0,comment = "0,默认不占有，1占有")
}