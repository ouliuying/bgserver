package work.bg.server.core.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.and
import dynamic.model.query.mq.eq
import dynamic.model.web.spring.boot.annotation.Action
import work.bg.server.core.cache.PartnerCache
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.util.Time
import java.util.*

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
    @Action("loadPageData")
    fun loadPageData(@RequestBody data:JsonObject?,partnerCache: PartnerCache):Any{
        var ar = ActionResult();
        val app = data?.get("app")?.asString
        val model = data?.get("model")?.asString
        val page = data?.get("page")?.asInt?:1
        val pageSize = 10
        var datas = if(!app.isNullOrEmpty() && !model.isNullOrEmpty())
            this.rawRead(criteria = and(eq(this.app,app),eq(this.model,model)),pageSize = pageSize,pageIndex= page)?.toModelDataObjectArray()
        else this.rawRead(criteria = null,pageSize = pageSize,pageIndex = page)?.toModelDataObjectArray()
        ar.bag["more"] = (datas!=null && datas.count()==10)
        var eventLogs = JsonArray()
        datas?.let { aIT ->
            aIT.forEach {
                var jo = JsonObject()
                val partner =  it.getFieldValue(BaseModelLog.ref.partner) as ModelDataObject?
                jo.addProperty("icon",partner?.getFieldValue(BasePartner.ref.userIcon) as String?)
                jo.addProperty("text",(partner?.getFieldValue(BasePartner.ref.userName) as String).substring(0,1))
                jo.addProperty("title",partner?.getFieldValue(BasePartner.ref.userName) as String)
                jo.addProperty("id",it.idFieldValue?.value as Number?)
                jo.addProperty("date", Time.formatDate(it.getFieldValue(BaseModelLog.ref.createTime) as Date))
                jo.addProperty("data",it.getFieldValue(BaseModelLog.ref.data) as String?)
                eventLogs.add(jo)
            }
        }
        ar.bag["eventLogs"]=eventLogs
        return ar
    }
}