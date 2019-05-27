package work.bg.server.core.model

import work.bg.server.core.RefSingleton
import work.bg.server.core.mq.FieldPrimaryKey
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelField

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

    val action= ModelField(null,
            "action",
            FieldType.STRING,
            "动作")

    val data= ModelField(null,
            "data",
            FieldType.STRING,
            "操作")
}