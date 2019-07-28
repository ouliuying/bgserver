package work.bg.server.core.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("modelLog")
class BaseModelLog:ContextModel("base_model_log","public") {
    companion object: RefSingleton<BaseModelLog> {
        override lateinit var ref: BaseModelLog
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val app= ModelField(null,
            "app",
            FieldType.STRING,
            "应用")

    val model= ModelField(null,
            "model",
            FieldType.STRING,
            "模型")
    val modelID= ModelField(null,
            "model_id",
            FieldType.BIGINT,
            "模型ID")

    val data= ModelField(null,
            "data",
            FieldType.STRING,
            "数据")

    val partner = ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "操作人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action=ForeignKeyAction.CASCADE))

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}