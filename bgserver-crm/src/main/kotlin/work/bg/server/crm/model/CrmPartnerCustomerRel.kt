package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("crmPartnerCustomerRel")
class CrmPartnerCustomerRel:ContextModel("crm_partner_customer_rel","public") {
    companion object : RefSingleton<CrmPartnerCustomerRel> {
        override lateinit var ref: CrmPartnerCustomerRel
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val partner= ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))

    val customer= ModelMany2OneField(null,
            "customer_id",
            FieldType.BIGINT,
            "客户",
            targetModelTable = "public.crm_customer",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action= ForeignKeyAction.CASCADE))

    val ownFlag = ModelField(null,
            "ownFlag",
            FieldType.INT,
            "占有",
            defaultValue = 0,comment = "0,默认不占有，1占有")

}