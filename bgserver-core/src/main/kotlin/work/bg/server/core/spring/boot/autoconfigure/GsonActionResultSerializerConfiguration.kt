/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *it under the terms of the GNU Affero General Public License as published by
t *  *  *he Free Software Foundation, either version 3 of the License.

 *  *  *This program is distributed in the hope that it will be useful,
 *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *GNU Affero General Public License for more details.

 *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  *
  *
  */

package work.bg.server.core.spring.boot.autoconfigure

import com.google.gson.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer
import org.springframework.boot.autoconfigure.gson.GsonProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Type
import com.google.gson.JsonObject
import dynamic.model.query.constant.ModelReservedKey
import dynamic.model.query.mq.model.AppModel
import dynamic.model.query.mq.specialized.ConstGetRecordRefField
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import dynamic.model.query.mq.specialized.ConstSetRecordRefField
import dynamic.model.web.spring.boot.model.ActionResult
import work.bg.server.core.context.JsonClauseResolver
import dynamic.model.query.exception.ModelErrorException
import dynamic.model.query.mq.ModelData
import dynamic.model.web.spring.boot.model.ReadActionParam
import work.bg.server.core.ui.MenuNode
import work.bg.server.core.ui.MenuTree
import work.bg.server.core.ui.ModelView
import dynamic.model.web.errorcode.ErrorCode
import java.text.SimpleDateFormat

//Gson convert  ActionResult ，spring application.properties/prefer gson

@Configuration
@ConditionalOnClass(Gson::class)
@EnableConfigurationProperties(GsonProperties::class)
class GsonActionResultSerializerConfiguration {
    @Bean
    fun jsonActionResultGsonBuilderCustomizer(
            gsonProperties: GsonProperties): JsonActionResultGsonBuilderCustomizer {
        return JsonActionResultGsonBuilderCustomizer(gsonProperties)
    }
    class JsonActionResultGsonBuilderCustomizer(val  properties:GsonProperties): GsonBuilderCustomizer {
        override fun customize(gsonBuilder: GsonBuilder?) {
            gsonBuilder?.registerTypeHierarchyAdapter(ActionResult::class.java,
                    ActionResultAdapter())
            gsonBuilder?.registerTypeHierarchyAdapter(dynamic.model.query.mq.ModelData::class.java,
                    ModelDataDeserializerAdapter())
            gsonBuilder?.registerTypeHierarchyAdapter(dynamic.model.query.mq.ModelData::class.java,
                    ModelDataSerializerAdapter())


            gsonBuilder?.registerTypeAdapter(MenuTree::class.java,
                    MenuTreeAdapter())
            gsonBuilder?.registerTypeAdapter(MenuNode::class.java,
                    MenuNodeAdapter())

            gsonBuilder?.registerTypeAdapter(ModelView.Field::class.java,
                    ModelViewFieldAdapter())

            gsonBuilder?.registerTypeAdapter(ModelView::class.java,
                    ModelViewAdapter())

            gsonBuilder?.registerTypeAdapter(ReadActionParam::class.java,ReadActionParamAdapter())
        }
    }
    class ModelDataDeserializerAdapter: JsonDeserializer<dynamic.model.query.mq.ModelData>{
        override fun deserialize(json: JsonElement?,
                                 typeOfT: Type?,
                                 context: JsonDeserializationContext?): dynamic.model.query.mq.ModelData? {
            var jsonObj=json?.asJsonObject
            return fillModelDataObject(jsonObj)
        }

        private fun fillModelDataObject(jsonObj:JsonObject?): dynamic.model.query.mq.ModelData?{
            if(jsonObj!= null){
                var app=jsonObj?.get("app")?.asString
                var model=jsonObj?.get("model")?.asString
                var fromField=jsonObj?.get("fromField")?.asJsonObject
                var toField=jsonObj?.get("toField")?.asJsonObject
                var fromIdValue=jsonObj?.get("fromIdValue")?.asLong
                var record=jsonObj?.get("record")
                if(record.isJsonArray){
                    return this.fillModelDataArrayImp(app,model,fromField,toField,fromIdValue,record.asJsonArray)
                }
                else if(record.isJsonObject){
                    return this.fillModelDataObjectImp(app,model,fromField,toField,fromIdValue,record.asJsonObject)
                }

            }
            return null
        }

        private fun fillModelDataObjectImp(appName:String?,
                                                     modelName:String?,
                                                     fromField:JsonObject?,
                                                     toField:JsonObject?,
                                                     fromIdValue:Long?,
                                                     record:JsonObject?): dynamic.model.query.mq.ModelData?{
            var model=AppModel.ref?.getModel(appName!!,modelName!!)
            var fieldValues=fillModelFieldValueArrayFromOneRecord(appName,modelName,record)
            if(!fieldValues.isEmpty()){
                var mdo= dynamic.model.query.mq.ModelDataObject(fieldValues, model)
                mdo.fromIdValue=fromIdValue
                if(fromField!=null){
                    var fromApp=fromField.get("app")?.asString
                    var fromModel=fromField.get("model")?.asString
                    var fromField=fromField.get("field")?.asString
                    if(fromApp!=null && fromModel!=null){
                        var model=AppModel.ref?.getModel(fromApp,fromModel)
                        if(fromField!=null){
                            mdo.fromField=model?.fields?.getFieldByPropertyName(fromField)
                        }
                    }
                }
                if(toField!=null){
                    var toApp=toField.get("app")?.asString
                    var toModel=toField.get("model")?.asString
                    var toField=toField.get("field")?.asString
                    if(toApp!=null && toModel!=null){
                        var model=AppModel.ref?.getModel(toApp,toModel)
                        if(toField!=null){
                            mdo.toField=model?.fields?.getFieldByPropertyName(toField)
                        }
                    }
                }
                return mdo
            }
            return null
        }
        private  fun fillModelFieldValueArrayFromOneRecord(appName:String?,
                                                       modelName:String?,
                                                       record:JsonObject?): dynamic.model.query.mq.FieldValueArray {
            var model=AppModel.ref?.getModel(appName!!,modelName!!)
            var fieldValues= dynamic.model.query.mq.FieldValueArray()
            if(model!=null){
                record?.entrySet()?.forEach {
                    var fd=model.getFieldByPropertyName(it.key)
                    if(fd!=null){
                        if(it.value.isJsonObject){
                            var jsonObj=it.value.asJsonObject
                            var app=jsonObj?.get("app")?.asString
                            var model=jsonObj?.get("model")?.asString
                            var fromField=jsonObj?.get("fromField")?.asJsonObject
                            var toField=jsonObj?.get("toField")?.asJsonObject
                            var fromIdValue=jsonObj?.get("fromIdValue")?.asLong
                            var record=jsonObj?.get("record")
                            if(record!=null){
                                when {
                                    record.isJsonArray -> {
                                        var mmfc=this.fillModelDataArrayImp(app!!,model!!,fromField,toField,fromIdValue,record.asJsonArray)
                                        fieldValues.add(dynamic.model.query.mq.FieldValue(fd, mmfc))
                                    }
                                    record.isJsonObject -> {

                                        var mfc=this.fillModelDataObjectImp(app!!,model!!,fromField,toField,fromIdValue,record.asJsonObject)
                                        fieldValues.add(dynamic.model.query.mq.FieldValue(fd, mfc))
                                    }
                                    else ->{

                                    }
                                }
                            }
                        }
                        else if(it.value.isJsonPrimitive){
                            var fdValue=when{
                                fd.fieldType == dynamic.model.query.mq.FieldType.INT->{
                                    it.value.asInt
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.BIGINT->{
                                    it.value.asBigInteger
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.DATE ->{
                                    var df = SimpleDateFormat("yyyy-MM-dd")
                                    df.parse(it.value.asString)
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.DATETIME->{
                                    var df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    df.parse(it.value.asString)
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.NUMBER->{
                                    it.value.asNumber
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.STRING || fd.fieldType == dynamic.model.query.mq.FieldType.TEXT->{
                                    it.value.asString
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.TIME->{
                                    var df = SimpleDateFormat("HH:mm:ss")
                                    df.parse(it.value.asString)
                                }
                                fd.fieldType == dynamic.model.query.mq.FieldType.DECIMAL->{
                                    it.value.asBigDecimal
                                }
                                else->{
                                    it.value.asString
                                }
                            }
                            fieldValues.add(dynamic.model.query.mq.FieldValue(fd, fdValue))
                        }
                    }
                    else{
                        when {
                            it.key.compareTo(ModelReservedKey.relRegistriesFieldKey,true)==0 -> {
                                var relModelSet=it.value.asJsonObject
                                var mDSO = dynamic.model.query.mq.ModelDataSharedObject()
                                relModelSet.entrySet().forEach {je->
                                    var jsonObj=je.value.asJsonObject
                                    var app=jsonObj?.get("app")?.asString
                                    var model=jsonObj?.get("model")?.asString
                                    var fromField=jsonObj?.get("fromField")?.asJsonObject
                                    var toField=jsonObj?.get("toField")?.asJsonObject
                                    var fromIdValue=jsonObj?.get("fromIdValue")?.asLong
                                    var record=jsonObj?.get("record")
                                    var modelKey= AppModel.ref?.getModel(app!!, model!!)
                                    if(record!=null)
                                    {
                                        if(record.isJsonArray){
                                            var mmfv=this.fillModelDataArrayImp(app,model,fromField,toField,fromIdValue,record!!.asJsonArray)
                                            if(mmfv!=null){
                                                mDSO.data[modelKey]=mmfv
                                            }

                                        }
                                        else if(record!!.isJsonObject){
                                            var mfv=this.fillModelDataObjectImp(app,model,fromField,toField,fromIdValue,record!!.asJsonObject)
                                            if(mfv!=null){
                                                mDSO.data[modelKey]=mfv
                                            }
                                        }
                                    }

                                }
                                fieldValues.add(dynamic.model.query.mq.FieldValue(ConstRelRegistriesField.ref, mDSO))
                            }
                            it.key.compareTo(ModelReservedKey.getRecordRefKey,true)==0 -> {
                                fieldValues.add(dynamic.model.query.mq.FieldValue(ConstGetRecordRefField.ref, it.value.asString))
                            }
                            it.key.compareTo(ModelReservedKey.setRecordRefKey,true)==0 -> {
                                fieldValues.add(dynamic.model.query.mq.FieldValue(ConstSetRecordRefField.ref, it.value.asString))
                            }
                        }
                    }
                }
            }
            return fieldValues
        }

        private fun fillModelDataArrayImp(appName:String?,
                                          modelName:String?,
                                          fromField:JsonObject?,
                                          toField:JsonObject?,
                                          fromIdValue:Long?,

                                                          record:JsonArray?): dynamic.model.query.mq.ModelDataArray?{
            var model=AppModel.ref?.getModel(appName!!,modelName!!)
            var mmfkv= dynamic.model.query.mq.ModelDataArray(model = model)
            mmfkv.fromIdValue=fromIdValue
            if(fromField!=null){
                var fromApp=fromField.get("app")?.asString
                var fromModel=fromField.get("model")?.asString
                var fromField=fromField.get("field")?.asString
                if(fromApp!=null && fromModel!=null){
                    var model=AppModel.ref?.getModel(fromApp,fromModel)
                    if(fromField!=null){
                        mmfkv.fromField=model?.fields?.getFieldByPropertyName(fromField)
                    }
                }
            }
            if(toField!=null){
                var toApp=toField.get("app")?.asString
                var toModel=toField.get("model")?.asString
                var toField=toField.get("field")?.asString
                if(toApp!=null && toModel!=null){
                    var model=AppModel.ref?.getModel(toApp,toModel)
                    if(toField!=null){
                        mmfkv.toField=model?.fields?.getFieldByPropertyName(toField)
                    }
                }
            }
            record?.forEach {
                var jsonObj=it.asJsonObject
                var fieldValues=this.fillModelFieldValueArrayFromOneRecord(appName,
                        modelName,jsonObj)
                mmfkv.data.add(fieldValues)
            }
            return mmfkv
        }
    }
    class ModelDataSerializerAdapter:JsonSerializer<dynamic.model.query.mq.ModelData>{
        override fun serialize(src: dynamic.model.query.mq.ModelData?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            var ret= buildModelData(src,typeOfSrc,context)
            return ret?:JsonObject()
        }
        private  fun buildFields(fields:ArrayList<dynamic.model.query.mq.FieldBase>?, typeOfSrc: Type?, context: JsonSerializationContext?):JsonArray?{
            var ja=JsonArray()
            if(fields!=null){
                fields.forEach {
                    var fo=this.buildField(it, typeOfSrc, context)
                    if(fo==null){
                        fo= JsonObject()
                    }
                    ja.add(fo)
                }
                return ja
            }
            return null
        }
        private fun buildField(field: dynamic.model.query.mq.FieldBase?, typeOfSrc: Type?, context: JsonSerializationContext?):JsonObject?{
            if(field!=null){
                var model=field.model
                var jo=JsonObject()
                jo.addProperty("app",model?.meta?.appName)
                jo.addProperty("model",model?.meta?.name)
                jo.addProperty("name",field.propertyName)
                jo.addProperty("type",field.fieldType.toString())
                when(field){
                    is dynamic.model.query.mq.ModelField ->{
                        jo.addProperty("title",field.title)
                        jo.addProperty("comment",field.comment)
                    }
                }
                return jo
            }
            return null
        }
        private  fun buildObject(fvArr: dynamic.model.query.mq.FieldValueArray, typeOfSrc: Type?, context: JsonSerializationContext?):JsonObject?{
            if(fvArr.count()>0){
                var jo=JsonObject()
                fvArr.forEach {
                    when(it.value){
                        is dynamic.model.query.mq.ModelData ->{
                            jo.add(it.field.propertyName,this.buildModelData(it.value as ModelData,typeOfSrc,context))
                        }
                        else->{
                            if(it.value!=null){
                                jo.add(it.field.propertyName,context?.serialize(it.value))
                            }
                            else{
                                jo.add(it.field.propertyName,null as JsonElement?)
                            }
                        }
                    }
                }
                return jo
            }
            return null
        }
        private  fun buildArray(fvArrs:ArrayList<dynamic.model.query.mq.FieldValueArray>, typeOfSrc: Type?, context: JsonSerializationContext?):JsonArray?{
            if(fvArrs.count()>0){
                var ja=JsonArray()
                fvArrs.forEach {
                    var jo=this.buildObject(it,typeOfSrc,context)
                    if(jo!=null){
                        ja.add(jo)
                    }
                }
                return ja
            }
            return null
        }
        private  fun buildModelData(modelData: dynamic.model.query.mq.ModelData?, typeOfSrc: Type?, context: JsonSerializationContext?):JsonObject?{
            val obj = JsonObject()
            if(modelData!=null){
                when(modelData){
                    is dynamic.model.query.mq.ModelDataObject,is dynamic.model.query.mq.ModelDataArray ->{
                        var model=modelData.model
                        var app=model?.meta?.appName
                        var modelName=model?.meta?.name
                        if(app!=null){
                            obj.addProperty("app",app)
                        }
                        if(modelName!=null){
                            obj.addProperty("model",modelName)
                        }
                        if(modelData.fields!=null){
                            var fieldsJsonObject=this.buildFields(modelData.fields, typeOfSrc, context)
                            if(fieldsJsonObject!=null){
                                obj.add("fields",fieldsJsonObject)
                            }
                        }
                        if(modelData.fromField!=null){
                            var fieldJsonObject=this.buildField(modelData.fromField, typeOfSrc, context)
                            if(fieldJsonObject!=null){
                                obj.add("fromField",fieldJsonObject)
                            }
                        }
                        if(modelData.toField!=null){
                            var fieldJsonObject=this.buildField(modelData.toField, typeOfSrc, context)
                            if(fieldJsonObject!=null){
                                obj.add("toField",fieldJsonObject)
                            }
                        }
                        if(modelData.fromIdValue!=null){
                            obj.addProperty("fromIdValue",modelData.fromIdValue!!)
                        }
                        if(modelData.isObject()){
                            var record=buildObject((modelData as dynamic.model.query.mq.ModelDataObject).data, typeOfSrc, context)
                            if(record!=null){
                                obj.add("record",record)
                            }
                        }
                        else{
                            var record=buildArray((modelData as dynamic.model.query.mq.ModelDataArray).data, typeOfSrc, context)
                            if(record!=null){
                                obj.add("record",record)
                            }
                        }
                    }
                    is dynamic.model.query.mq.ModelDataSharedObject ->{
                        var regJson=JsonObject()
                        modelData.data.forEach { t, u ->
                            var jd=this.buildModelData(u, typeOfSrc, context)
                            regJson.add(t?.meta?.name!!,jd)
                        }
                        obj.add(ModelReservedKey.relRegistriesFieldKey,regJson)
                    }
                }
            }
            return obj
        }
    }
    class MenuTreeAdapter:JsonSerializer<MenuTree>{
        override fun serialize(src: MenuTree?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return this.buildTreeJson(src, context)
        }
        private fun buildTreeJson(tree:MenuTree?, context: JsonSerializationContext?):JsonElement{
            var jObj=JsonObject()
            if(tree!=null){
                jObj.addProperty("app",tree.app)
                jObj.addProperty("name",tree.name)
                jObj.addProperty("title",tree.title)
                jObj.addProperty("icon",tree.icon)
                var jSubMenu=JsonArray()
                jObj.add("subMenu",jSubMenu)
                tree?.children?.forEach {
                    when(it){
                        is MenuTree->{
                            var mnElement=this.buildTreeJson(it,context)
                            if(mnElement!=null){
                                jSubMenu.add(mnElement)
                            }
                        }
                        is MenuNode->{
                            var mnElement=context?.serialize(it,MenuNode::class.java)
                            if(mnElement!=null){
                                jSubMenu.add(mnElement)
                            }
                        }
                    }
                }
            }
            return jObj
        }
    }
    class MenuNodeAdapter:JsonSerializer<MenuNode>{
        override fun serialize(src: MenuNode?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return this.buildNodeJson(src,context)
        }
        private fun buildNodeJson(node:MenuNode?,context: JsonSerializationContext?):JsonElement{
            var jObj=JsonObject()
            if(node!=null){
                jObj.addProperty("app",node.app)
                jObj.addProperty("model",node.model)
                jObj.addProperty("title",node.title)
                jObj.addProperty("viewType",node.viewType)
                jObj.addProperty("icon",node.icon)
                if(!(node.redirectApp.isNullOrEmpty() || node.redirectApp.isNullOrBlank())){
                    jObj.addProperty("redirectApp",node.redirectApp)
                }
            }
            return jObj
        }
    }
    class ModelViewAdapter:JsonSerializer<ModelView>{
        override fun serialize(src: ModelView?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return this.buildView(src,context)
        }
        private fun buildView(view:ModelView?,context: JsonSerializationContext?):JsonElement{
            var jObj=JsonObject()
            if(view!=null){
                jObj.addProperty("app",view.app)
                jObj.addProperty("model",view.model)
                jObj.addProperty("viewType",view.viewType)
                var jFieldArr=JsonArray()
                jObj.add("fields",jFieldArr)
                view.fields.forEach {
                    var jf=context?.serialize(it,ModelView.Field::class.java)
                    if(jf!=null){
                        jFieldArr.add(jf)
                    }
                }
            }
            return jObj
        }
    }
    class ModelViewFieldAdapter:JsonSerializer<ModelView.Field>{
        override fun serialize(src: ModelView.Field?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
           var jObj=JsonObject()
            if(src!=null){
                jObj.addProperty("name",this.toPropertyName(src.name))
                jObj.addProperty("title",src.title)
                jObj.addProperty("type",src.type)
                jObj.addProperty("style",src.style)
                jObj.addProperty("colSpan",src.colSpan)
                jObj.addProperty("rowSpan",src.rowSpan)
                jObj.addProperty("visible",src.visible)
                jObj.addProperty("enable",src.enable)
                jObj.addProperty("viewType",src.viewType)
                jObj.addProperty("icon",src.icon)
                jObj.addProperty("app",src.app)
                jObj.addProperty("model",src.model)
                if(src.meta!=null){
                    jObj.add("meta",src.meta)
                }
                if(src.ctrlProps!=null){
                    jObj.add("ctrlProps",src.ctrlProps)
                }
                if(src?.fieldView!=null){
                    jObj.add("fieldView",context?.serialize(src.fieldView))
                }
                if(src.relationData!=null){
                    jObj.add("relationData",context?.serialize(src.relationData))
                }
            }
            return jObj
        }
        private  fun toPropertyName(name:String):String{
            var index=name.indexOf('.')
            return if(index<0) name else name.substring(0,index)
        }
    }
    class ActionResultAdapter: JsonSerializer<ActionResult> {
        override fun serialize(value: ActionResult?, typeOfSrc: Type?,
                               context: JsonSerializationContext?): JsonElement {

            val obj = JsonObject()
            if (value != null) {

                obj.addProperty("errorCode",value.errorCode.code)
                obj.addProperty("description", value.description?:value.errorCode.description)
                if (value.bag.count() > 0) {
                    val subObj = JsonObject()
                    value.bag.forEach { t, u ->
                        if(u !is JsonElement){
                            subObj.add(t,context?.serialize(u))
                        }
                        else{
                            subObj.add(t,u)
                        }
                    }
                    obj.add("bag",subObj)
                }
            }
            else{
                obj.addProperty("errorCode", ErrorCode.UNKNOW.code)
                obj.addProperty("description", ErrorCode.UNKNOW.description)
            }
            return obj
        }
    }

    class ReadActionParamAdapter: JsonDeserializer<ReadActionParam>{
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ReadActionParam {
           // var p=ReadActionParam()
            if(json!=null){
                var obj=json.asJsonObject
                val app=obj["app"].asString
                val model=obj["model"].asString
                val modelObj=AppModel.ref.getModel(app,model)
                var raFields=null as ArrayList<dynamic.model.query.mq.FieldBase>?
                var raOrderBy= null as dynamic.model.query.mq.OrderBy?
                var pageIndex=1
                var pageSize=10
                var raCriteria=null as dynamic.model.query.mq.ModelExpression?
                var raAttachedFields:ArrayList<dynamic.model.query.mq.AttachedField>?=null
                if(modelObj!=null){
                    if(obj.has("fields")){
                        var fields= arrayListOf<dynamic.model.query.mq.FieldBase>()
                        obj["fields"].asJsonArray.forEach {
                            var name=it.asString
                            var f=modelObj.fields.getFieldByPropertyName(name)
                            if(f!=null){
                                fields.add(f)
                            }
                        }
                        raFields=fields
                    }
                    if(obj.has("orderByFields")){
                        var orderFields= arrayListOf<dynamic.model.query.mq.OrderBy.OrderField>()
                        obj["orderByFields"].asJsonArray.forEach {
                            var o=it.asJsonObject
                            var fName=o["field"].asString
                            var fd=modelObj.fields.getFieldByPropertyName(fName)
                            var fdOT= dynamic.model.query.mq.OrderBy.Companion.OrderType.ASC
                            if(o.has("orderType")){
                                var oType=o["orderType"].asString
                                if(oType.compareTo("desc",true)==0){
                                    fdOT= dynamic.model.query.mq.OrderBy.Companion.OrderType.DESC
                                }
                            }
                            if(fd!=null){
                                orderFields.add(dynamic.model.query.mq.OrderBy.OrderField(fd,fdOT))
                            }
                        }
                        if(orderFields.count()>0){
                            raOrderBy= dynamic.model.query.mq.OrderBy(*orderFields.toTypedArray())
                        }
                    }
                    if(obj.has("clause")){
                        raCriteria= JsonClauseResolver(obj["clause"].asJsonObject,modelObj).criteria()
                    }
                    if(obj.has("includeFields")){
                        var fCs=obj["includeFields"].asJsonArray
                        raAttachedFields= arrayListOf()
                       fCs.forEach {
                           var fobj=it.asJsonObject
                           var attachedField=modelObj.fields.getFieldByPropertyName(fobj["name"].asString)
                           if(attachedField!=null && attachedField is dynamic.model.query.mq.RefTargetField){
                               var c=null as dynamic.model.query.mq.ModelExpression?
                               var canBeEmpty=true
                               if(fobj.has("clause")){
                                    c=JsonClauseResolver(fobj["clause"].asJsonObject,modelObj).criteria()
                               }
                               if(fobj.has("canBeEmpty")){
                                   var canV=fobj["canBeEmpty"].asString
                                   if(canV=="0" || canV.compareTo("true",true)==0){
                                       canBeEmpty=false
                                   }
                               }
                               raAttachedFields.add(dynamic.model.query.mq.AttachedField(attachedField, c, canBeEmpty))
                           }
                       }
                    }
                }
                if(obj.has("pageIndex")){
                    pageIndex=obj["pageIndex"].asInt
                }
                if(obj.has("pageSize")){
                    pageSize=obj["pageSize"].asInt
                }
                return ReadActionParam(raFields, raCriteria, raAttachedFields, raOrderBy, pageSize, pageIndex)
            }
            else{
                throw ModelErrorException("解析clause错误")
            }

        }
    }
}