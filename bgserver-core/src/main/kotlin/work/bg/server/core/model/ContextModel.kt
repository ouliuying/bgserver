

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

package work.bg.server.core.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.util.TypeConvert
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheRegistry
import work.bg.server.core.context.JsonClauseResolver
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.model.ReadActionParam
import work.bg.server.core.ui.*
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.model.ActionResult
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

abstract  class ContextModel(tableName:String,schemaName:String):AccessControlModel(tableName,schemaName) {
    private val logger = LogFactory.getLog(javaClass)
    @Autowired
    var partnerCacheRegistry:PartnerCacheRegistry?=null
//    @Autowired
//    lateinit var gson: Gson
    init {

    }

    /**
     *  actions begin
     */
    @Action(name="edit")
    open fun editAction(@RequestBody modelData: dynamic.model.query.mq.ModelData?, pc:PartnerCache): ActionResult?{
        var ar= ActionResult()
        if(modelData!=null){
            var ret=this.acEdit(modelData,criteria = null,partnerCache = pc)
            if(ret?.first!=null && ret?.first!! > 0){
                ar.bag["result"]=ret.first!!
                return ar
            }
            ar.description=ret?.second
        }
        ar.errorCode=ErrorCode.UPDATEMODELFAIL
        return ar
    }

    @Action(name="list")
    open fun listAction(@RequestBody param: ReadActionParam, pc:PartnerCache):ActionResult?{
        var ar=ActionResult()
        var fields= param.fields?:arrayListOf()
        var attachFields:Array<dynamic.model.query.mq.AttachedField>? = if(param.attachedFields!=null) arrayOf(*param.attachedFields!!.toTypedArray()) else null
        var modelData=this.acRead(*fields.toTypedArray(),
                model=this,
                criteria = param.criteria,
                partnerCache = pc,
                orderBy = param.orderBy,
                attachedFields = attachFields,
                pageIndex = param.pageIndex,
                pageSize = param.pageSize)
        return ar
    }

    @Action(name="create")
    open fun createAction(@RequestBody modelData: dynamic.model.query.mq.ModelData?, pc:PartnerCache):ActionResult?{
        var ar=ActionResult()
        if(modelData!=null){
            var ret=this.acCreate(modelData,pc)
            if(ret.first!=null && ret.first!!>0){
                ar.bag["id"]=ret.first!!
                return ar
            }
            ar.description=ret.second
        }
        print(ar.description)
        ar.errorCode=ErrorCode.CREATEMODELFAIL
        return ar
    }

    @Action(name="read")
    open fun readAction(@RequestBody param: ReadActionParam, pc:PartnerCache):ActionResult?{
        var ar=ActionResult()
        var fields= param.fields?:arrayListOf()
        var attachFields:Array<dynamic.model.query.mq.AttachedField>? = if(param.attachedFields!=null) arrayOf(*param.attachedFields!!.toTypedArray()) else null
        var modelData=this.acRead(*fields.toTypedArray(),
                model=this,
                criteria = param.criteria,
                partnerCache = pc,
                orderBy = param.orderBy,
                attachedFields = attachFields,
                pageIndex = param.pageIndex,
                pageSize = param.pageSize)
        return ar
    }


    @Action(name="delete")
    open fun deleteAction(@RequestBody data:JsonObject, pc:PartnerCache): ActionResult?{
        var ar = ActionResult()
        val id = data["id"]?.asLong
        id?.let {
            var fdarr = dynamic.model.query.mq.FieldValueArray()
            val idField = this.fields!!.getIdField()!!
            fdarr.setValue(idField,id)
            var  mo = dynamic.model.query.mq.ModelDataObject(model = this, data = fdarr)
            val criteria = eq(idField,id)
            var ret= this.acDelete(mo,criteria=criteria,partnerCache = pc)
            ret?.first?.let {
                if(it>0){
                  return ar
                }
                ar.description=ret.second
                ar.errorCode=ErrorCode.UNKNOW
            }
        }
        return ar
    }

    @Action(name="loadModelViewType")
    open fun loadModelViewType(@RequestBody data:JsonObject, pc:PartnerCache):ActionResult{
        var ar=ActionResult()
        val app =this.meta.appName
        val model = this.meta.name
        val viewType = data["viewType"]?.asString?:""
        val viewRefType =data["viewRefType"]?.asString
        val reqData = data["reqData"]?.asJsonObject
        val ownerField = data["ownerField"]?.asJsonObject
        val ownerModelID = data["ownerModelID"]?.asLong
        val (ownerFieldValue,toField) = this.getOwnerFieldAndRefField(ownerField,app,model)
        ar.bag = this.loadMainViewType(app,model,viewType,ownerFieldValue,toField,ownerModelID,pc,reqData,viewRefType)
        return ar
    }
    private  fun getMany2ManyFieldByRelationModel(model: ModelBase, relationModel:ModelBase, ownerField: dynamic.model.query.mq.FieldBase): dynamic.model.query.mq.ModelMany2ManyField?{
            val ownerModel = ownerField.model!!
            model.fields?.forEach {
                if(it is dynamic.model.query.mq.ModelMany2ManyField){
                    if(it.relationModelTable==relationModel.fullTableName && it.targetModelFieldName=="id" &&
                            it.targetModelTable==ownerModel.fullTableName){
                        return it
                    }
                }
            }
        return null
    }

    private  fun getRelationModelMany2OneFieldToOwnerFieldModel(relationModel:ModelBase,ownerField: dynamic.model.query.mq.FieldBase): dynamic.model.query.mq.FieldBase?{
        val ownerModel = ownerField.model!!
        relationModel.fields?.forEach {
            if(it is dynamic.model.query.mq.ModelMany2OneField && it.targetModelTable==ownerModel.fullTableName && it.targetModelFieldName=="id"){
                return it
            }
        }
        return null
    }

    private fun getOwnerFieldAndRefField(ownerField: JsonObject?,
                                         app:String,model:String):Pair<FieldValue?, FieldBase?>{
        ownerField?.let {
            val ownerApp = it["app"]?.asString
            val ownerModel = it["model"]?.asString
            val ownerFieldName = it["name"]?.asString
            if(!ownerApp.isNullOrEmpty() && !ownerModel.isNullOrEmpty()
                    &&!ownerFieldName.isNullOrEmpty()){
                val ownerFieldObject = this.appModel.getModel(ownerApp,ownerModel)?.fields?.getFieldByPropertyName(ownerFieldName)
                if(ownerFieldObject!=null){
                    var refField = when(ownerFieldObject){
                        is dynamic.model.query.mq.ModelMany2ManyField ->{
                            var relationModel = this.appModel.getModel(ownerFieldObject.relationModelTable!!)
                            var targetModel = this.appModel.getModel(ownerFieldObject.targetModelTable!!)
                            if(relationModel!=null && relationModel.meta.appName == app && relationModel.meta.name == model){
                                this.getRelationModelMany2OneFieldToOwnerFieldModel(relationModel,ownerFieldObject)
                            }
                            else if(targetModel!=null && targetModel.meta.appName == app && targetModel.meta.name == model){
                               this.getMany2ManyFieldByRelationModel(targetModel,relationModel!!,ownerFieldObject)
                               // targetModel.fields.getField(ownerFieldObject.targetModelFieldName)
                            }
                            else {
                                null
                            }
                        }
                        is dynamic.model.query.mq.ModelOne2ManyField,is dynamic.model.query.mq.ModelMany2OneField,is dynamic.model.query.mq.ModelOne2OneField ->{
                            var targetModel = this.appModel.getModel((ownerFieldObject as dynamic.model.query.mq.RefTargetField).targetModelTable!!)
                            if(targetModel!=null && targetModel.meta.appName == app && targetModel.meta.name==model){
                                targetModel.fields?.getField(ownerFieldObject.targetModelFieldName)
                            }
                            else{
                                null
                            }
                        }
                        else->null
                    }
                    if(refField!=null) return if(it.has("value")){
                        val ownerValue =  it["value"]?.asLong
                        Pair(dynamic.model.query.mq.FieldValue(ownerFieldObject, ownerValue),refField)
                    } else{
                        Pair(dynamic.model.query.mq.FieldValue(ownerFieldObject, dynamic.model.query.mq.Undefined),refField)
                    }
                }
            }
        }
        return Pair(null,null)
    }

    protected open fun controlModelViewStatusWithOwnerField(mv:ModelView?, toField: dynamic.model.query.mq.FieldBase?, ownerFieldValue: dynamic.model.query.mq.FieldValue?, ownerModelID:Long?, pc:PartnerCache){
        if(mv!=null && toField!=null){
            mv.fields.map {
                if(it.name == toField.propertyName){
                    it.enable="enable=false"
                }
            }
        }
    }
    private fun loadMainViewType(app:String, model:String,
                                 viewType:String,
                                 ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                 toField: dynamic.model.query.mq.FieldBase?,
                                 ownerModelID:Long?,
                                 pc:PartnerCache,
                                 reqData:JsonObject?, viewRefType:String?=null):MutableMap<String,Any>{

        var reqRefType= viewRefType?:if(ownerFieldValue!=null) ModelViewRefType.Sub else ModelViewRefType.Main
        var mv=pc.getAccessControlModelView(app,model,viewType)
        controlModelViewStatusWithOwnerField(mv,toField,ownerFieldValue,ownerModelID,pc)
        var bag = LinkedHashMap<String,Any>()
        if(mv!=null){
            var mvData = this.loadModelViewData(mv,bag, pc,ownerFieldValue,toField,ownerModelID, reqData)
            mv=this.fillModelViewMeta(mv,mvData,bag,pc,ownerFieldValue,toField,ownerModelID,reqData)
            bag["view"] = mv
            if(mvData!=null){
                bag["data"]=mvData
            }
            var refActionGroups = arrayListOf<ModelView.RefActionGroup>()
            var reqRefTypeArray = reqRefType.split(",")
            mv.refActionGroups.forEach {
                if(it.refTypes.intersect(reqRefTypeArray).count()>0){
                    refActionGroups.add(it)
                }
            }
            var triggerGroups = this.loadModelViewActionTriggerGroups(mv,refActionGroups.toTypedArray(),pc,reqData)
            if(triggerGroups!=null){
                bag["triggerGroups"]=triggerGroups
            }
            mv.refViews.forEach {
                var refMV = this.loadRefViewType(it,pc,ownerFieldValue,toField,ownerModelID,reqData,reqRefTypeArray)
                refMV?.let {
                    if(bag.containsKey("subViews")){
                        (bag["subViews"] as ArrayList<Map<String,Any>>).add(refMV)
                    }
                    else{
                        var subViews = arrayListOf<Map<String,Any>>()
                        subViews.add(refMV)
                        bag["subViews"]=subViews
                    }
                }
            }
        }
        return bag
    }

    private fun loadRefViewType(refView:ModelView.RefView,
                                pc:PartnerCache,
                                ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                toField: dynamic.model.query.mq.FieldBase?,
                                ownerModelID: Long?,
                                reqData:JsonObject?,
                                reqRefTypeArray:List<String>,
                                fullLoad:Boolean=false
                                ):MutableMap<String,Any>?{
        var mv=pc.getAccessControlModelView(refView.app,refView.model,refView.viewType)
        if(mv!=null){
            var bag = LinkedHashMap<String,Any>()
            bag["refView"]=refView
            if(refView.refTypes.contains(ModelViewRefType.Embedded) || fullLoad){
                bag["view"] = mv
                var mvData = this.loadModelViewData(mv, bag,pc, ownerFieldValue,toField,ownerModelID,reqData)
                mv=this.fillModelViewMeta(mv,mvData,bag,pc,ownerFieldValue,toField,ownerModelID,reqData)
                if(mvData!=null){
                    bag["data"]=mvData
                }
                var refActionGroups = arrayListOf<ModelView.RefActionGroup>()
                mv.refActionGroups.forEach {
                    if(it.refTypes.intersect(reqRefTypeArray).count()>0){
                        refActionGroups.add(it)
                    }
                }
                var triggerGroups = this.loadModelViewActionTriggerGroups(mv,refActionGroups.toTypedArray(),pc,reqData)
                if(triggerGroups!=null){
                    bag["triggerGroups"]=triggerGroups
                }
                mv.refViews.forEach {
                    var refMV= this.loadRefViewType(it,pc,ownerFieldValue,toField,ownerModelID,reqData, arrayListOf(ModelViewRefType.Sub))
                    refMV?.let {
                        if(bag.containsKey("subViews")){
                            (bag["subViews"] as ArrayList<Map<String,Any>>).add(refMV)
                        }
                        else{
                            var subViews = arrayListOf<Map<String,Any>>()
                            subViews.add(refMV)
                            bag["subViews"]=subViews
                        }
                    }
                }
            }
            return bag
        }
        return null
    }
    /**
     * actions end
     */

    protected open fun loadModelViewActionTriggerGroups(mv:ModelView,refActionGroups:Array<ModelView.RefActionGroup>,pc:PartnerCache,reqData: JsonObject?):Array<TriggerGroup>?{
        var triggerGroups = arrayListOf<TriggerGroup>()
        refActionGroups.forEach {
            val tg=pc.getAccessControlModelViewActionGroup(it)
            if(tg!=null){
                triggerGroups.add(tg)
            }
        }
        return triggerGroups.toTypedArray()
    }

    protected  open fun fillModelViewMeta(mv:ModelView, modelData: dynamic.model.query.mq.ModelData?, viewData:MutableMap<String,Any>,
                                          pc:PartnerCache,
                                          ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                          toField: dynamic.model.query.mq.FieldBase?,
                                          ownerModelID: Long?,
                                          reqData: JsonObject?):ModelView{


        when(mv.viewType){
            ModelView.ViewType.CREATE->{
                return this.fillCreateModelViewMeta(mv,modelData,viewData,pc,ownerFieldValue,toField,reqData)
            }
            ModelView.ViewType.DETAIL->{
                return this.fillDetailModelViewMeta(mv,modelData,viewData,pc,ownerFieldValue,toField,reqData)
            }
            ModelView.ViewType.EDIT->{
                return this.fillEditModelViewMeta(mv,modelData,viewData,pc,ownerFieldValue,toField,reqData)
            }
            ModelView.ViewType.LIST->{
                return this.fillListModelViewMeta(mv,modelData,viewData,pc,ownerFieldValue,toField,reqData)
            }
        }
        return mv
    }
    protected  open fun fillDetailModelViewMeta(mv:ModelView,
                                                modelData: dynamic.model.query.mq.ModelData?,
                                                viewData:MutableMap<String,Any>,
                                                pc:PartnerCache,
                                                ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                                toField: dynamic.model.query.mq.FieldBase?,
                                                reqData: JsonObject?):ModelView{
        var modelDataObject  = modelData as dynamic.model.query.mq.ModelDataObject?
        mv.fields.forEach {mvi->
            when (mvi.type) {
                ModelView.Field.ViewFieldType.many2ManyDataSetSelect,
                ModelView.Field.ViewFieldType.selectModelFromListView,
                ModelView.Field.ViewFieldType.many2OneDataSetSelect-> {
                    if (mvi.relationData != null &&
                            mvi.meta == null &&
                            mvi.style != ModelView.Field.Style.relation &&
                            modelData != null) {
                        if((mvi.relationData as ModelView.RelationData).type==ModelView.RelationType.Many2Many){
                            var modelObject = this.appModel.getModel(mv.app!!,mv.model!!)
                            var rField = modelObject?.getFieldByPropertyName(mvi.name) as dynamic.model.query.mq.ModelMany2ManyField?
                            var relationModel = this.appModel.getModel(rField?.relationModelTable)
                            var targetModel=this.appModel.getModel((mvi.relationData as ModelView.RelationData).targetApp,
                                    (mvi.relationData as ModelView.RelationData).targetModel)
                            if (modelDataObject != null &&
                                    relationModel!=null &&
                                    targetModel!=null) {
                                var jArr = JsonArray()
                                var dataArray = (modelDataObject?.getFieldValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject?)?.data?.get(relationModel) as dynamic.model.query.mq.ModelDataArray?
                                dataArray?.data?.forEach {fvs->
                                    fvs.forEach { fv->
                                        if(fv.value is dynamic.model.query.mq.ModelDataObject){
                                            if(targetModel.isSame((fv.value as ModelDataObject).model)){
                                                jArr.add(this.gson.toJsonTree(fv.value))
                                            }
                                        }
                                    }
                                }
                                var metaObj=JsonObject()
                                metaObj.add("options",jArr)
                                mvi.meta=metaObj
                            }
                        }
                        else if((mvi.relationData as ModelView.RelationData).type==ModelView.RelationType.Many2One){
                            var modelObject = this.appModel.getModel(mv.app!!,mv.model!!)
                            var targetModel=this.appModel.getModel((mvi.relationData as ModelView.RelationData).targetApp,
                                    (mvi.relationData as ModelView.RelationData).targetModel)
                            var tField = modelObject?.getFieldByPropertyName(mvi.name)
                            tField?.let {
                                var tValue = modelDataObject?.getFieldValue(tField)
                                if(targetModel!=null && tValue is dynamic.model.query.mq.ModelDataObject){
                                    var jArr = JsonArray()
                                    if(targetModel.isSame(tValue.model)){
                                        jArr.add(this.gson.toJsonTree(tValue))
                                    }
                                    var metaObj=JsonObject()
                                    metaObj.add("options",jArr)
                                    mvi.meta=metaObj
                                }
                            }

                        }
                    }
                }
            }
            mvi.source?.let {fs->
                val tMeta = ModelViewFieldSourceCache.run(fs)
                tMeta?.let { m->

                    mvi.meta = if(m is JsonElement)  m else this.gson.toJsonTree(m)
                }
            }
        }
        return mv
    }

    protected  open fun fillEditModelViewMeta(mv:ModelView,
                                              modelData: dynamic.model.query.mq.ModelData?,
                                              viewData:MutableMap<String,Any>,
                                              pc:PartnerCache,
                                              ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                              toField: dynamic.model.query.mq.FieldBase?,
                                              reqData: JsonObject?):ModelView{
        var modelDataObject  = modelData as dynamic.model.query.mq.ModelDataObject?
        mv.fields.forEach {mvi->
            when (mvi.type) {
                ModelView.Field.ViewFieldType.many2ManyDataSetSelect,
                ModelView.Field.ViewFieldType.selectModelFromListView,
                ModelView.Field.ViewFieldType.many2OneDataSetSelect-> {
                    if (mvi.relationData != null &&
                            mvi.meta == null &&
                            mvi.style != ModelView.Field.Style.relation &&
                            modelData != null) {
                        if((mvi.relationData as ModelView.RelationData).type==ModelView.RelationType.Many2Many){
                            var modelObject = this.appModel.getModel(mv.app!!,mv.model!!)
                            var rField = modelObject?.getFieldByPropertyName(mvi.name) as dynamic.model.query.mq.RefRelationField?
                            var relationModel = this.appModel.getModel(rField?.relationModelTable)
                            var targetModel=this.appModel.getModel((mvi.relationData as ModelView.RelationData).targetApp,
                                    (mvi.relationData as ModelView.RelationData).targetModel)
                            if (modelDataObject != null &&
                                    relationModel!=null &&
                                    targetModel!=null) {
                                var jArr = JsonArray()
                                var dataArray = (modelDataObject?.getFieldValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject?)?.data?.get(relationModel) as dynamic.model.query.mq.ModelDataArray?
                                dataArray?.data?.forEach {fvs->
                                    fvs.forEach { fv->
                                        if(fv.value is dynamic.model.query.mq.ModelDataObject){
                                            if(targetModel.isSame((fv.value as ModelDataObject).model)){
                                                jArr.add(this.gson.toJsonTree(fv.value))
                                            }
                                        }
                                    }
                                }
                                var metaObj=JsonObject()
                                metaObj.add("options",jArr)
                                mvi.meta=metaObj
                            }
                        }
                        else if((mvi.relationData as ModelView.RelationData).type==ModelView.RelationType.Many2One){
                            var modelObject = this.appModel.getModel(mv.app!!,mv.model!!)
                            var targetModel=this.appModel.getModel((mvi.relationData as ModelView.RelationData).targetApp,
                                    (mvi.relationData as ModelView.RelationData).targetModel)
                            var tField = modelObject?.getFieldByPropertyName(mvi.name)
                            tField?.let {
                                var tValue = modelDataObject?.getFieldValue(tField)
                                if(targetModel!=null && tValue is dynamic.model.query.mq.ModelDataObject){
                                    var jArr = JsonArray()
                                    if(targetModel.isSame(tValue.model)){
                                        jArr.add(this.gson.toJsonTree(tValue))
                                    }
                                    var metaObj=JsonObject()
                                    metaObj.add("options",jArr)
                                    mvi.meta=metaObj
                                }
                            }

                        }
                    }
                }
            }
            mvi.source?.let {fs->
                val tMeta = ModelViewFieldSourceCache.run(fs)
                tMeta?.let { m->
                    mvi.meta = if(m is JsonElement)  m else this.gson.toJsonTree(m)
                }
            }
        }
        return mv
    }
    protected  open fun fillListModelViewMeta(mv:ModelView,
                                              modelData: dynamic.model.query.mq.ModelData?,
                                              viewData:MutableMap<String,Any>,
                                              pc:PartnerCache,
                                              ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                              toField: dynamic.model.query.mq.FieldBase?,
                                              reqData: JsonObject?):ModelView{

        mv.fields.forEach {
            it.source?.let {fs->
                val tMeta = ModelViewFieldSourceCache.run(fs)
                tMeta?.let { m->

                    it.meta = if(m is JsonElement)  m else this.gson.toJsonTree(m)
                }
            }
        }
        return mv
    }

    protected  open fun fillCreateModelViewMeta(mv:ModelView,
                                                modelData: dynamic.model.query.mq.ModelData?,
                                                viewData:MutableMap<String,Any>,
                                                pc:PartnerCache,
                                                ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                                toField: dynamic.model.query.mq.FieldBase?,
                                                reqData: JsonObject?):ModelView{
        mv.fields.forEach {
            when(it.type){
                ModelView.Field.ViewFieldType.many2OneDataSetSelect,
                ModelView.Field.ViewFieldType.selectModelFromListView,
                ModelView.Field.ViewFieldType.many2ManyDataSetSelect->{
                    if(it.relationData!=null &&
                            it.meta==null &&
                            it.style!= ModelView.Field.Style.relation){
                        var tModel = this.appModel.getModel(it.relationData!!.targetApp,it.relationData!!.targetModel)
                        if(tModel!=null){
                            var idField = tModel.fields.getIdField()
                            var toField = tModel.getFieldByPropertyName(it.relationData!!.toName!!)
                            if(idField!=null && toField!=null){
                                var dataArray=(tModel as ContextModel).acRead(partnerCache = pc,
                                        criteria = null,
                                        pageIndex = 1,
                                        pageSize = 10)
                                if(dataArray!=null){
                                    var jArr = JsonArray()
                                    dataArray.toModelDataObjectArray().forEach {it->
                                        jArr.add(this.gson.toJsonTree(it))
                                    }
                                    var metaObj=JsonObject()
                                    metaObj.add("options",jArr)
                                    it.meta=metaObj
                                }
                            }
                        }
                    }
                }
            }
           it.source?.let {fs->
               val tMeta = ModelViewFieldSourceCache.run(fs)
               tMeta?.let { m->

                   it.meta = if(m is JsonElement)  m else this.gson.toJsonTree(m)
               }
           }
        }
        return mv
    }

    protected open fun loadModelViewData(mv:ModelView,
                                         viewData:MutableMap<String,Any>,
                                         pc:PartnerCache,
                                         ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                         toField: dynamic.model.query.mq.FieldBase?,
                                         ownerModelID: Long?,
                                         reqData:JsonObject?): dynamic.model.query.mq.ModelData?{
        when(mv.viewType){
            ModelView.ViewType.CREATE->{
               return this.loadCreateModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.DETAIL->{
                return this.loadDetailModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.EDIT,ModelView.ViewType.MODEL_ACTION->{
                return this.loadEditModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.LIST->{
                return this.loadListModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.MODEL_ACTION_CONFIRM->{
                return null
            }
        }
        return null
    }

    protected fun getModelViewFields(mv:ModelView):ArrayList<dynamic.model.query.mq.FieldBase>{
        var fields = arrayListOf<dynamic.model.query.mq.FieldBase>()
        var fFields = arrayListOf<dynamic.model.query.mq.FunctionField<*,*>>()
        if(mv.app.isNullOrEmpty() || mv.model.isNullOrEmpty()){
            return fields
        }
        var modelObj: ModelBase? = this.appModel.getModel(mv.app,mv.model) ?: return fields
        mv.fields.forEach {
            if(it.relationData==null){
                var field = modelObj?.getFieldByPropertyName(it.name)
                field?.let {fd->
                    if(fd !is dynamic.model.query.mq.FunctionField<*,*>){
                        if(fd !in fields){
                            fields.add(fd)
                        }
                        else{
                            //pass
                        }
                    }
                    else{
                        if(fd !in fFields){
                            fFields.add(fd)
                        }
                        fd.depFields?.forEach {dFD->
                            if(dFD !is dynamic.model.query.mq.FunctionField<*,*>){
                                if(dFD !in fields){
                                    fields.add(dFD!!)
                                }
                            }
                        }
                    }
                }
            }
            else{
                val rd = it.relationData as ModelView.RelationData
                if(it.style!=ModelView.Field.Style.relation ||
                        rd.type==ModelView.RelationType.Many2One||
                        rd.type==ModelView.RelationType.VirtualOne2One||
                        rd.type==ModelView.RelationType.One2One){
                    var field= modelObj?.getFieldByPropertyName(it.name)
                    field?.let { rFD->
                        if(rFD !in fields){
                            fields.add(rFD)
                        }
                    }
                }
            }
        }
        var idField = modelObj?.fields?.getIdField()
        idField?.let {
            if(fields.count {
                it.isSame(idField)
            }<1){
                fields.add(idField)
            }
        }
        return fields
    }
    protected open fun loadCreateModelViewData(mv:ModelView,
                                               viewData:MutableMap<String,Any>,
                                               pc:PartnerCache,
                                               ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                               toField: dynamic.model.query.mq.FieldBase?,
                                               ownerModelID: Long?,
                                               reqData:JsonObject?): dynamic.model.query.mq.ModelDataObject? {
        var mo = dynamic.model.query.mq.ModelDataObject(model = this)
        if(ownerFieldValue!=null && ownerFieldValue.value!= dynamic.model.query.mq.Undefined && toField!=null){
            var ownerModel = ownerFieldValue.field.model as ContextModel?
            ownerModel?.let {
                if(it.isPersistField(ownerFieldValue.field)){
                    val ownerData = it.acRead(criteria =eq(ownerFieldValue.field,ownerFieldValue.value),partnerCache = pc)

                    if(ownerData!=null){
                        val d =  ownerData.firstOrNull()
                        d?.let {
                            mo.setFieldValue(toField!!,d)
                        }
                    }
                }
            }
        }
        if(ownerModelID!=null && ownerModelID>0){
            var ownerModel = ownerFieldValue?.field?.model as ContextModel?
            ownerModel?.let {
               val ma= ownerModel.acRead(model=ownerModel,partnerCache = pc,criteria = eq(ownerModel.fields?.getIdField()!!,ownerModelID))
               ma?.firstOrNull()?.let {mit->
                   this.fields.map {
                       if (it is dynamic.model.query.mq.RefTargetField) {
                            if(ownerModel.fullTableName == it.targetModelTable &&
                                    dynamic.model.query.mq.FieldConstant.id==it.targetModelFieldName) {
                                mo.setFieldValue(it, mit)
                                return@map
                            }
                       }
                   }

               }
            }
        }
        return mo
    }

    protected open fun loadDetailModelViewData(mv:ModelView,
                                               viewData:MutableMap<String,Any>,
                                               pc:PartnerCache,
                                               ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                               toField: dynamic.model.query.mq.FieldBase?,
                                               ownerModelID: Long?,
                                               reqData:JsonObject?): dynamic.model.query.mq.ModelDataObject?{
        val id = reqData?.get("id")?.asLong
        val fields= this.getModelViewFields(mv)
        id?.let {
            val idField = this.fields.getIdField()
            idField?.let { idf->
                var data = this.acRead(*fields.toTypedArray(),criteria = eq(idf,it), partnerCache = pc)
                data?.let {
                    if(it.data.count()>0){
                        return this.toClientModelData(it.firstOrNull(),arrayListOf(*fields.filter {_f->
                            _f is dynamic.model.query.mq.ModelMany2ManyField
                        }.toTypedArray())) as dynamic.model.query.mq.ModelDataObject?
                    }
                }
            }
        }
        val (ret,criteria) = this.getCriteriaByOwnerModelParam(ownerFieldValue,
                toField,
                ownerModelID)
        if(ret) {
            var data = this.acRead(*fields.toTypedArray(), criteria = criteria, partnerCache = pc)
            data?.let {
                if (it.data.count() > 0) {
                    return this.toClientModelData(it.firstOrNull(),arrayListOf(*fields.filter {_f->
                        _f is dynamic.model.query.mq.ModelMany2ManyField
                    }.toTypedArray())) as dynamic.model.query.mq.ModelDataObject?
                }
            }
        }
        return null
    }
    protected fun getCriteriaByOwnerModelParam(ownerFieldValue: FieldValue?,
                                               toField: FieldBase?,
                                               ownerModelID: Long?):Pair<Boolean, ModelExpression?>{

        if(ownerFieldValue!=null && toField!=null){
            if(this.isPersistField(toField)){
                if(ownerFieldValue.value!= dynamic.model.query.mq.Undefined){
                    return Pair(true,eq(toField,ownerFieldValue.value))
                }
                else if(ownerModelID!=null){
                    return Pair(true,eq(toField,ownerModelID))
                }
            }
            else{
                if(toField is dynamic.model.query.mq.ModelMany2ManyField){
                    if(ownerFieldValue.field is dynamic.model.query.mq.ModelMany2ManyField){
                        if(ownerModelID!=null){
                           val mf= this.getRelationModelField(ownerFieldValue.field)
                            if(mf?.first!=null){
                                val tmf = this.getRelationModelField(toField)
                                if(tmf?.first!=null){
                                    val subSelect = select(mf.second!!,fromModel = mf.first!!).where(eq(tmf.second!!,ownerModelID))
                                    return Pair(true,`in`(toField.model!!.fields.getIdField()!!,subSelect)!!)
                                }
                            }
                        }
                    }
                    else if(ownerFieldValue.field is dynamic.model.query.mq.ModelMany2OneField){
                        if(ownerFieldValue.value!= dynamic.model.query.mq.Undefined){
                            return Pair(true,eq(toField?.model?.fields?.getIdField()!!,(ownerFieldValue.value as String?)?.toLong()))
                        }
                    }
                }
                else if(toField is dynamic.model.query.mq.ModelOne2ManyField){
                        if(ownerFieldValue.value!= dynamic.model.query.mq.Undefined){
                            return Pair(true,eq(toField.model?.fields?.getIdField()!!,(ownerFieldValue.value as String?)?.toLong()))
                        }
                }
                else if(toField is dynamic.model.query.mq.One2OneField && toField.isVirtualField){
                    if(ownerFieldValue.value!= dynamic.model.query.mq.Undefined){
                        return Pair(true,eq(toField.model?.fields?.getIdField()!!,(ownerFieldValue.value as String?)?.toLong()))
                    }
                }
            }
        }
        return Pair(false,null)
    }
    fun isPersistField(field: dynamic.model.query.mq.FieldBase?):Boolean{
        field?.let {
            if((it !is dynamic.model.query.mq.FunctionField<*,*>) &&
                    (it !is dynamic.model.query.mq.RefRelationField) &&
                    it !is dynamic.model.query.mq.One2ManyField){
                if(it !is dynamic.model.query.mq.ModelOne2OneField || !it.isVirtualField){
                    return true
                }
            }
        }
       return false
    }
    protected open fun loadEditModelViewData(mv:ModelView,
                                             viewData:MutableMap<String,Any>,
                                             pc:PartnerCache,
                                             ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                             toField: dynamic.model.query.mq.FieldBase?,
                                             ownerModelID: Long?,
                                             reqData:JsonObject?): dynamic.model.query.mq.ModelDataObject?{
        val id = reqData?.get("id")?.asInt
        val fields = this.getModelViewFields(mv)
        id?.let {
            val idField = this.fields.getIdField()
            idField?.let { idf->
                var data = this.acRead(*fields.toTypedArray(), criteria = eq(idf,it), partnerCache = pc)
                data?.let {
                    if(it.data.count()>0){
                        return this.toClientModelData(it.firstOrNull(), arrayListOf(*fields.filter {
                            it is dynamic.model.query.mq.ModelMany2ManyField
                        }.toTypedArray())) as dynamic.model.query.mq.ModelDataObject?
                    }
                }
            }
        }
        val (ret,criteria) = this.getCriteriaByOwnerModelParam(ownerFieldValue,toField,ownerModelID)
        if(ret) {
            var data = this.acRead(*fields.toTypedArray(), criteria = criteria, partnerCache = pc)
            data?.let {
                if (it.data.count() > 0) {
                    return this.toClientModelData(it.firstOrNull(),arrayListOf(*fields.filter {_f->
                        _f is dynamic.model.query.mq.ModelMany2ManyField
                    }.toTypedArray())) as dynamic.model.query.mq.ModelDataObject?
                }
            }
        }
        return null
    }

    private  fun setM2MFieldValue(model:ModelBase,
                                  fvs: dynamic.model.query.mq.FieldValueArray,
                                  fd: dynamic.model.query.mq.FieldBase,
                                  relModel:ModelBase,
                                  relFvs: dynamic.model.query.mq.FieldValueArray){
        val fRelModel = this.appModel.getModel((fd as dynamic.model.query.mq.ModelMany2ManyField).relationModelTable)
        if(relModel.isSame(fRelModel)){
            relFvs.forEach {
                when(it.field){
                    is dynamic.model.query.mq.ModelMany2OneField ->{
                        if(it.value is dynamic.model.query.mq.ModelDataObject){
                            var tTargetModel = this.appModel.getModel((it.field as ModelMany2OneField).targetModelTable)
                            tTargetModel?.let {_->
                                if(tTargetModel.isSame(model)){
                                    fvs.setValue(fd,it.value)
                                    return@forEach
                                }
                            }
                            var fTargetModel = this.appModel.getModel(fd.targetModelTable)
                            var rTargetModel = this.appModel.getModel((it.field as ModelMany2OneField).targetModelTable)
                            fTargetModel?.let {_->
                                if(fTargetModel.isSame(rTargetModel)){
                                    fvs.setValue(fd,it.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected fun toClientModelData(modelData: dynamic.model.query.mq.ModelData?, m2mFields:ArrayList<dynamic.model.query.mq.FieldBase>): dynamic.model.query.mq.ModelData?{
        modelData?.let {
            when(it){
                is dynamic.model.query.mq.ModelDataObject ->{
                    var mso = it.getFieldValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject?
                    mso?.let {msoIt->
                        m2mFields.forEach {fd->
                            if(fd is dynamic.model.query.mq.ModelMany2ManyField){
                                var relModel = this.appModel.getModel(fd.relationModelTable)
                                relModel?.let {mRelIt->
                                    msoIt.data[mRelIt]?.let {mdaIt->
                                        (mdaIt as dynamic.model.query.mq.ModelDataArray).data.firstOrNull()?.let { sit->
                                            this.setM2MFieldValue(fd.model!!,it.data,fd,relModel,sit)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is dynamic.model.query.mq.ModelDataArray ->{
                    it.data?.forEach {fvs->
                        var mso = fvs.getValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject?
                        mso?.let {msoIt->
                            m2mFields.forEach {fd->
                               if(fd is dynamic.model.query.mq.ModelMany2ManyField){
                                   var relModel = this.appModel.getModel(fd.relationModelTable)
                                   relModel?.let {mRelIt->
                                       msoIt.data[mRelIt]?.let {mdaIt->
                                           (mdaIt as dynamic.model.query.mq.ModelDataArray).data.firstOrNull()?.let { nfvs->
                                               this.setM2MFieldValue(fd.model!!,fvs,fd,relModel,nfvs)
                                           }

                                       }
                                   }
                               }
                            }
                        }
                    }
                }
                else->null
            }
        }
        return modelData
    }
    protected open fun processFunctionFieldAfterRead(modelArray: dynamic.model.query.mq.ModelDataArray?, funFields:ArrayList<dynamic.model.query.mq.FunctionField<*,*>>){

    }
    protected  open fun loadListModelViewData(mv:ModelView,
                                              viewData:MutableMap<String,Any>,
                                              pc:PartnerCache,
                                              ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                              toField: dynamic.model.query.mq.FieldBase?,
                                              ownerModelID: Long?,
                                              reqData:JsonObject?): dynamic.model.query.mq.ModelDataArray?{
        if(ownerFieldValue!=null){
            if(ownerFieldValue.value== dynamic.model.query.mq.Undefined && ownerModelID==null){
                return null
            }
        }
        val fields = this.getModelViewFields(mv).toTypedArray()
        val pageIndex = reqData?.get("pageIndex")?.asInt?:1
        val pageSize = reqData?.get("pageSize")?.asInt?:10
        val jCriteria = reqData?.get("criteria")?.asJsonObject
        //TODO parse javascript criteria

        var criteria = null as dynamic.model.query.mq.ModelExpression?
        jCriteria?.let {
            criteria = JsonClauseResolver(it,this,pc.modelExpressionContext).criteria()
        }
        val (ret,ownerCriteria) = this.getCriteriaByOwnerModelParam(ownerFieldValue,toField,ownerModelID)
        if(ret){
            criteria = if(criteria!=null && ownerCriteria!=null) and(ownerCriteria,criteria!!) else if(criteria!=null) criteria  else ownerCriteria
        }
        var data = this.acRead(*fields,partnerCache = pc,pageIndex = pageIndex,pageSize = pageSize,criteria = criteria)
        var totalCount = this.acCount(criteria = criteria,partnerCache = pc)
        viewData["totalCount"]=totalCount
        return this.toClientModelData(data,arrayListOf(*fields.filter {_f->
            _f is dynamic.model.query.mq.ModelMany2ManyField
        }.toTypedArray())) as dynamic.model.query.mq.ModelDataArray?
    }

    override fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {
        modelDataObject.idFieldValue?.value?.let {
            var modelID = TypeConvert.getLong(it as Number)
            val modelTitle = this.meta.title
            val embeddedCtlType=JsonObject()
            val ctlData=JsonObject()
            ctlData.addProperty("modelID",modelID)
            ctlData.addProperty("app",this.meta.appName)
            ctlData.addProperty("model",this.meta.name)
            ctlData.addProperty("viewType","detail")
            ctlData.addProperty("title",this.meta.title)
            embeddedCtlType.addProperty("viewType","logView")
            embeddedCtlType.add("data",ctlData)
            this.addModelLogImp("创建$modelTitle",embeddedCtlType,"时间：${Date()}",modelID=modelID,pc=pc)
        }
    }

    override fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {
        modelDataObject.idFieldValue?.value?.let {
            val modelID = TypeConvert.getLong(it as Number)
            val modelTitle = this.meta.title
            val embeddedCtlType=JsonObject()
            val ctlData=JsonObject()
            ctlData.addProperty("modelID",modelID)
            ctlData.addProperty("app",this.meta.appName)
            ctlData.addProperty("model",this.meta.name)
            ctlData.addProperty("viewType","detail")
            ctlData.addProperty("title",this.meta.title)
            embeddedCtlType.addProperty("viewType","logView")
            embeddedCtlType.add("data",ctlData)
            this.addModelLogImp("更新$modelTitle",embeddedCtlType,"时间：${Date()}",modelID=modelID,pc=pc)
        }
    }

    private  fun addModelLogImp(vararg args:Any,modelID:Long?,pc:PartnerCache?){
        var modelLog= BaseModelLog.ref
        var mo= dynamic.model.query.mq.ModelDataObject(model = modelLog)
        mo.setFieldValue(modelLog.app,this.meta.appName)
        mo.setFieldValue(modelLog.model,this.meta.name)
        mo.setFieldValue(modelLog.modelID,modelID)
        pc?.let {
            val partnerObj= dynamic.model.query.mq.ModelDataObject(model = BasePartner.ref)
            partnerObj.setFieldValue(BasePartner.ref.id,pc?.partnerID)
            mo.setFieldValue(modelLog.partner,partnerObj)
        }
        if(args.isNotEmpty()){
            val argArray = JsonArray()
            for (arg in args) {
                when(arg){
                    is String->argArray.add(arg)
                    is JsonElement->argArray.add(arg)
                    else->argArray.add(this.gson.toJsonTree(arg))
                }
            }
            mo.setFieldValue(modelLog.data,argArray.toString())
        }
        modelLog.rawCreate(mo,useAccessControl = false,partnerCache = null)
    }
}