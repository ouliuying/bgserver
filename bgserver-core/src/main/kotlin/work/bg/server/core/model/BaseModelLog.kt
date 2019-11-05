package work.bg.server.core.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.web.spring.boot.annotation.Action
import work.bg.server.core.cache.PartnerCache
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.ui.ModelView
import work.bg.server.core.ui.TriggerGroup
import work.bg.server.util.Time
import work.bg.server.util.TypeConvert
import java.util.*

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

    val icon = ModelField(null,
            "icon",
            FieldType.STRING,
            "图标")

    val partner = ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "操作人",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
    // {"controlType":"modelCommentControl","icon":"/svg/event-log-comment.svg","data":{"comment":"343434"}}
    @Action("addControlTypeData")
    fun addControlTypeData(@RequestBody data:JsonObject?,
                           partnerCache: PartnerCache):Any{
        var ar = ActionResult()
        var modelID = TypeConvert.getLong(data?.get("modelID")?.asNumber)
        val modelLog = ModelDataObject(model = ref)
        val app = data?.get("app")?.asString
        val model = data?.get("model")?.asString
        modelLog.setFieldValue(ref.app,app)
        modelLog.setFieldValue(ref.model,model)
        modelLog.setFieldValue(ref.modelID,modelID)
        modelLog.setFieldValue(ref.partner,partnerCache.partnerID)
        modelLog.setFieldValue(ref.icon,data?.get("icon")?.asString)
        val controlData = JsonObject()
        controlData.addProperty("controlType",data?.get("controlType")?.asString)
        controlData.add("props",data?.get("data"))
        val modelType = AppModelWeb.ref.getModel(app,model)
        val jArray = JsonArray()
        jArray.add("备注")
        val controlData2 = JsonObject()
        controlData2.addProperty("controlType","modelLogControl")
        val controlDataProps =  JsonObject()
        controlDataProps.addProperty("text",modelType?.meta?.title)
        controlDataProps.addProperty("viewType","detail")
        controlDataProps.addProperty("app",app)
        controlDataProps.addProperty("model",model)
        controlDataProps.addProperty("modelID",modelID)
        controlData2.add("props",controlDataProps)
        jArray.add(controlData2)
        jArray.add(controlData)
        modelLog.setFieldValue(ref.data,jArray.toString())
        ref.rawCreate(modelLog,partnerCache = partnerCache)
        return ar
    }
    @Action("loadPageData")
    fun loadPageData(@RequestBody data:JsonObject?,partnerCache: PartnerCache):Any{
        var ar = ActionResult()
        val app = data?.get("app")?.asString
        val model = data?.get("model")?.asString
        val page = data?.get("page")?.asInt?:1
        val pageSize =  data?.get("pageSize")?.asInt?:10
        val modelID = data?.get("modelID")?.asLong
        var criteria = if(!app.isNullOrEmpty() && !model.isNullOrEmpty())
           and(eq(this.app,app),eq(this.model,model))
        else null
        criteria = if(criteria!=null && modelID!=null && modelID >0) and(criteria,eq(this.modelID,modelID))
        else criteria
        val datas = this.rawRead(criteria=criteria,
                pageIndex = page,
                pageSize = pageSize,
                partnerCache = partnerCache,useAccessControl = true)?.toModelDataObjectArray()
        ar.bag["totalCount"] = this.rawCount(criteria=criteria, partnerCache = partnerCache,useAccessControl = true)
        var eventLogs = JsonArray()
        datas?.let { aIT ->
            aIT.forEach {
                var jo = JsonObject()
                val partner =  it.getFieldValue(ref.partner) as ModelDataObject?
                jo.addProperty("icon",partner?.getFieldValue(BasePartner.ref.userIcon) as String?)
                jo.addProperty("text",(partner?.getFieldValue(BasePartner.ref.userName) as String).substring(0,1))
                jo.addProperty("title", partner.getFieldValue(BasePartner.ref.userName) as String)
                jo.addProperty("id",it.idFieldValue?.value as Number?)
                jo.addProperty("date", Time.formatDate(it.getFieldValue(ref.createTime) as Date))
                jo.addProperty("ctrlIcon",it.getFieldValue(ref.icon) as String?)
                jo.addProperty("data",it.getFieldValue(ref.data) as String?)
                eventLogs.add(jo)
            }
        }
        ar.bag["triggerGroups"]= arrayOf<TriggerGroup>()
        ar.bag["eventLogs"]=eventLogs
        return ar
    }
}