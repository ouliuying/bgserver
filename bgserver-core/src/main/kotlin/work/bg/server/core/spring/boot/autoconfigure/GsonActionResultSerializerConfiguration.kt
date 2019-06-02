/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  * GNU Lesser General Public License Usage
 *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  * General Public License version 3 as published by the Free Software
 *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  * project of this file. Please review the following information to
 *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
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
import work.bg.server.core.spring.boot.model.ActionResult
import java.lang.reflect.Type
import com.google.gson.JsonObject
import work.bg.server.core.constant.ModelReservedKey
import work.bg.server.core.context.JsonClauseResolver
import work.bg.server.core.exception.ModelErrorException
import work.bg.server.core.mq.*
import work.bg.server.core.mq.specialized.ConstGetRecordRefField
import work.bg.server.core.mq.specialized.ConstRelRegistriesField
import work.bg.server.core.mq.specialized.ConstSetRecordRefField
import work.bg.server.core.spring.boot.model.AppModel
import work.bg.server.core.spring.boot.model.ReadActionParam
import work.bg.server.core.ui.MenuNode
import work.bg.server.core.ui.MenuTree
import work.bg.server.core.ui.ModelView
import work.bg.server.errorcode.ErrorCode
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
            gsonBuilder?.registerTypeHierarchyAdapter(ModelData::class.java,
                    ModelDataDeserializerAdapter())
            gsonBuilder?.registerTypeHierarchyAdapter(ModelData::class.java,
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
    class ModelDataDeserializerAdapter: JsonDeserializer<ModelData>{
        override fun deserialize(json: JsonElement?,
                                 typeOfT: Type?,
                                 context: JsonDeserializationContext?): ModelData? {
            var jsonObj=json?.asJsonObject
            return fillModelDataObject(jsonObj)
        }

        private fun fillModelDataObject(jsonObj:JsonObject?):ModelData?{
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
                                                     record:JsonObject?):ModelData?{
            var model=AppModel.ref?.getModel(appName!!,modelName!!)
            var fieldValues=fillModelFieldValueArrayFromOneRecord(appName,modelName,record)
            if(!fieldValues.isEmpty()){
                var mdo= ModelDataObject(fieldValues,model)
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
                                                       record:JsonObject?):FieldValueArray{
            var model=AppModel.ref?.getModel(appName!!,modelName!!)
            var fieldValues= FieldValueArray()
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
                                        fieldValues.add(FieldValue(fd,mmfc))
                                    }
                                    record.isJsonObject -> {

                                        var mfc=this.fillModelDataObjectImp(app!!,model!!,fromField,toField,fromIdValue,record.asJsonObject)
                                        fieldValues.add(FieldValue(fd,mfc))
                                    }
                                    else ->{

                                    }
                                }
                            }
                        }
                        else if(it.value.isJsonPrimitive){
                            var fdValue=when{
                                fd.fieldType == FieldType.INT->{
                                    it.value.asInt
                                }
                                fd.fieldType == FieldType.BIGINT->{
                                    it.value.asBigInteger
                                }
                                fd.fieldType == FieldType.DATE ->{
                                    var df = SimpleDateFormat("yyyy-MM-dd")
                                    df.parse(it.value.asString)
                                }
                                fd.fieldType == FieldType.DATETIME->{
                                    var df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    df.parse(it.value.asString)
                                }
                                fd.fieldType == FieldType.NUMBER->{
                                    it.value.asNumber
                                }
                                fd.fieldType == FieldType.STRING || fd.fieldType == FieldType.TEXT->{
                                    it.value.asString
                                }
                                fd.fieldType == FieldType.TIME->{
                                    var df = SimpleDateFormat("HH:mm:ss")
                                    df.parse(it.value.asString)
                                }
                                fd.fieldType == FieldType.DECIMAL->{
                                    it.value.asBigDecimal
                                }
                                else->{
                                    it.value.asString
                                }
                            }
                            fieldValues.add(FieldValue(fd,fdValue))
                        }
                    }
                    else{
                        when {
                            it.key.compareTo(ModelReservedKey.relRegistriesFieldKey,true)==0 -> {
                                var relModelSet=it.value.asJsonObject
                                var mDSO = ModelDataSharedObject()
                                relModelSet.entrySet().forEach {je->
                                    var jsonObj=je.value.asJsonObject
                                    var app=jsonObj?.get("app")?.asString
                                    var model=jsonObj?.get("model")?.asString
                                    var fromField=jsonObj?.get("fromField")?.asJsonObject
                                    var toField=jsonObj?.get("toField")?.asJsonObject
                                    var fromIdValue=jsonObj?.get("fromIdValue")?.asLong
                                    var record=jsonObj?.get("record")
                                    var modelKey=AppModel.ref?.getModel(app!!, model!!)
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
                                fieldValues.add(FieldValue(ConstRelRegistriesField.ref,mDSO))
                            }
                            it.key.compareTo(ModelReservedKey.getRecordRefKey,true)==0 -> {
                                fieldValues.add(FieldValue(ConstGetRecordRefField.ref,it.value.asString))
                            }
                            it.key.compareTo(ModelReservedKey.setRecordRefKey,true)==0 -> {
                                fieldValues.add(FieldValue(ConstSetRecordRefField.ref,it.value.asString))
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

                                                          record:JsonArray?):ModelDataArray?{
            var model=AppModel.ref?.getModel(appName!!,modelName!!)
            var mmfkv=ModelDataArray(model=model)
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
    class ModelDataSerializerAdapter:JsonSerializer<ModelData>{
        override fun serialize(src: ModelData?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            var ret= buildModelData(src,typeOfSrc,context)
            return ret?:JsonObject()
        }
        private  fun buildFields(fields:ArrayList<FieldBase>?, typeOfSrc: Type?, context: JsonSerializationContext?):JsonArray?{
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
        private fun buildField(field:FieldBase?, typeOfSrc: Type?, context: JsonSerializationContext?):JsonObject?{
            if(field!=null){
                var model=field.model
                var jo=JsonObject()
                jo.addProperty("app",model?.meta?.appName)
                jo.addProperty("model",model?.meta?.name)
                jo.addProperty("name",field.propertyName)
                jo.addProperty("type",field.fieldType.toString())
                when(field){
                    is ModelField->{
                        jo.addProperty("title",field.title)
                        jo.addProperty("comment",field.comment)
                    }
                }
                return jo
            }
            return null
        }
        private  fun buildObject(fvArr:FieldValueArray, typeOfSrc: Type?, context: JsonSerializationContext?):JsonObject?{
            if(fvArr.count()>0){
                var jo=JsonObject()
                fvArr.forEach {
                    when(it.value){
                        is ModelData->{
                            jo.add(it.field.propertyName,this.buildModelData(it.value,typeOfSrc,context))
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
        private  fun buildArray(fvArrs:ArrayList<FieldValueArray>, typeOfSrc: Type?, context: JsonSerializationContext?):JsonArray?{
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
        private  fun buildModelData(modelData:ModelData?, typeOfSrc: Type?, context: JsonSerializationContext?):JsonObject?{
            val obj = JsonObject()
            if(modelData!=null){
                when(modelData){
                    is ModelDataObject,is ModelDataArray->{
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
                            var record=buildObject((modelData as ModelDataObject).data, typeOfSrc, context)
                            if(record!=null){
                                obj.add("record",record)
                            }
                        }
                        else{
                            var record=buildArray((modelData as ModelDataArray).data, typeOfSrc, context)
                            if(record!=null){
                                obj.add("record",record)
                            }
                        }
                    }
                    is ModelDataSharedObject->{
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
                var raFields=null as ArrayList<FieldBase>?
                var raOrderBy= null as OrderBy?
                var pageIndex=1
                var pageSize=10
                var raCriteria=null as ModelExpression?
                var raAttachedFields:ArrayList<AttachedField>?=null
                if(modelObj!=null){
                    if(obj.has("fields")){
                        var fields= arrayListOf<FieldBase>()
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
                        var orderFields= arrayListOf<OrderBy.OrderField>()
                        obj["orderByFields"].asJsonArray.forEach {
                            var o=it.asJsonObject
                            var fName=o["field"].asString
                            var fd=modelObj.fields.getFieldByPropertyName(fName)
                            var fdOT= OrderBy.Companion.OrderType.ASC
                            if(o.has("orderType")){
                                var oType=o["orderType"].asString
                                if(oType.compareTo("desc",true)==0){
                                    fdOT=OrderBy.Companion.OrderType.DESC
                                }
                            }
                            if(fd!=null){
                                orderFields.add(OrderBy.OrderField(fd,fdOT))
                            }
                        }
                        if(orderFields.count()>0){
                            raOrderBy= OrderBy(*orderFields.toTypedArray())
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
                           if(attachedField!=null && attachedField is RefTargetField){
                               var c=null as ModelExpression?
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
                               raAttachedFields.add(AttachedField(attachedField,c,canBeEmpty))
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
                return ReadActionParam(raFields,raCriteria,raAttachedFields,raOrderBy,pageSize,pageIndex)
            }
            else{
                throw ModelErrorException("解析clause错误")
            }

        }
    }
}