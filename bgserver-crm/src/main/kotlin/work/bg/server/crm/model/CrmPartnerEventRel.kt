package work.bg.server.crm.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("crmPartnerEventRel")
class CrmPartnerEventRel:ContextModel("crm_partner_event_rel","public") {
    companion object : RefSingleton<CrmPartnerEventRel> {
        override lateinit var ref: CrmPartnerEventRel
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val partner=ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "用户",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    val event = ModelMany2OneField(null,
            "event_id",
            FieldType.BIGINT,
            "活动",
            targetModelTable = "public.crm_event",
            targetModelFieldName = "id",foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val mainFlag= ModelField(null,
            "main_flag",
            FieldType.INT,
            "主要负责人",
            defaultValue = 0)

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.event,this.partner,advice = "负责人已经存在！",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }


    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.event,this.partner,advice = "负责人已经存在！",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }
}