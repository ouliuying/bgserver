package work.bg.server.core.model

import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.cache.PartnerCache
import dynamic.model.web.spring.boot.annotation.Model

@Model("modelLog")
class BaseModelLog:ContextModel("base_model_log","public") {
    companion object: RefSingleton<BaseModelLog> {
        override lateinit var ref: BaseModelLog
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val app= dynamic.model.query.mq.ModelField(null,
            "app",
            dynamic.model.query.mq.FieldType.STRING,
            "应用")

    val model= dynamic.model.query.mq.ModelField(null,
            "model",
            dynamic.model.query.mq.FieldType.STRING,
            "模型")
    val modelID= dynamic.model.query.mq.ModelField(null,
            "model_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "模型ID")

    val data= dynamic.model.query.mq.ModelField(null,
            "data",
            dynamic.model.query.mq.FieldType.STRING,
            "数据")

    val partner = dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "操作人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    override fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}