

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

package work.bg.server.core.model

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheRegistry
import work.bg.server.core.context.JsonClauseResolver
import work.bg.server.core.mq.*
import work.bg.server.core.mq.specialized.ConstRelRegistriesField
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.core.spring.boot.model.ReadActionParam
import work.bg.server.core.ui.*
import work.bg.server.errorcode.ErrorCode

abstract  class ContextModel(tableName:String,schemaName:String):AccessControlModel(tableName,schemaName) {
    private val logger = LogFactory.getLog(javaClass)
    @Autowired
    var partnerCacheRegistry:PartnerCacheRegistry?=null
    @Autowired
    lateinit var gson: Gson
    init {

    }

    /**
     *  actions begin
     */
    @Action(name="edit")
    open fun editAction(@RequestBody modelData:ModelData?,pc:PartnerCache): ActionResult?{
        var ar=ActionResult()
        if(modelData!=null){
            var ret=this.acEdit(modelData,criteria = null,partnerCache = pc)
            if(ret.first!=null){
                ar.bag["result"]=ret.first!!
                return ar
            }
        }
        ar.errorCode=ErrorCode.UPDATEMODELFAIL
        return ar
    }

    @Action(name="list")
    open fun listAction(@RequestBody param:ReadActionParam,pc:PartnerCache):ActionResult?{
        var ar=ActionResult()
        var fields= param.fields?:arrayListOf()
        var attachFields:Array<AttachedField>? = if(param.attachedFields!=null) arrayOf(*param.attachedFields.toTypedArray()) else null
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
    open fun createAction(@RequestBody modelData:ModelData?,pc:PartnerCache):ActionResult?{
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
    open fun readAction(@RequestBody param:ReadActionParam,pc:PartnerCache):ActionResult?{
        var ar=ActionResult()
        var fields= param.fields?:arrayListOf()
        var attachFields:Array<AttachedField>? = if(param.attachedFields!=null) arrayOf(*param.attachedFields.toTypedArray()) else null
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
            var fdarr = FieldValueArray()
            val idField = this.fields!!.getIdField()!!
            fdarr.setValue(idField,id)
            var  mo = ModelDataObject(model=this,data=fdarr)
            val criteria = eq(idField,id)
            var ret= this.acDelete(mo,criteria=criteria,useAccessControl = true,partnerCache = pc)
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
    private  fun getMany2ManyFieldByRelationModel(model:ModelBase,relationModel:ModelBase,ownerField:FieldBase):ModelMany2ManyField?{
            val ownerModel = ownerField.model!!
            model.fields?.forEach {
                if(it is ModelMany2ManyField){
                    if(it.relationModelTable==relationModel.fullTableName && it.targetModelFieldName=="id" &&
                            it.targetModelTable==ownerModel.fullTableName){
                        return it
                    }
                }
            }
        return null
    }

    private  fun getRelationModelMany2OneFieldToOwnerFieldModel(relationModel:ModelBase,ownerField:FieldBase):FieldBase?{
        val ownerModel = ownerField.model!!
        relationModel.fields?.forEach {
            if(it is ModelMany2OneField && it.targetModelTable==ownerModel.fullTableName && it.targetModelFieldName=="id"){
                return it
            }
        }
        return null
    }

    private fun getOwnerFieldAndRefField(ownerField: JsonObject?,app:String,model:String):Pair<FieldValue?,FieldBase?>{
        ownerField?.let {
            val ownerApp = it["app"]?.asString
            val ownerModel = it["model"]?.asString
            val ownerFieldName = it["name"]?.asString
            if(!ownerApp.isNullOrEmpty() && !ownerModel.isNullOrEmpty()
                    &&!ownerFieldName.isNullOrEmpty()){
                val ownerFieldObject = this.appModel.getModel(ownerApp,ownerModel)?.fields?.getFieldByPropertyName(ownerFieldName)
                if(ownerFieldObject!=null){
                    var refField = when(ownerFieldObject){
                        is ModelMany2ManyField->{
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
                        is ModelOne2ManyField,is ModelMany2OneField,is ModelOne2OneField->{
                            var targetModel = this.appModel.getModel((ownerFieldObject as RefTargetField).targetModelTable!!)
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
                        Pair(FieldValue(ownerFieldObject,ownerValue),refField)
                    } else{
                        Pair(FieldValue(ownerFieldObject,Undefined),refField)
                    }
                }
            }
        }
        return Pair(null,null)
    }

    private fun loadMainViewType(app:String,model:String,
                                 viewType:String,
                                 ownerFieldValue:FieldValue?,
                                 toField:FieldBase?,
                                 ownerModelID:Long?,
                                 pc:PartnerCache,
                                 reqData:JsonObject?,viewRefType:String?=null):MutableMap<String,Any>{
        var reqRefType= viewRefType?:if(ownerFieldValue!=null) ModelViewRefType.Sub else ModelViewRefType.Main
        var mv=pc.getAccessControlModelView(app,model,viewType)
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
                                ownerFieldValue:FieldValue?,
                                toField:FieldBase?,
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

    protected  open fun fillModelViewMeta(mv:ModelView,modelData:ModelData?,viewData:MutableMap<String,Any>,
                                          pc:PartnerCache,
                                          ownerFieldValue:FieldValue?,
                                          toField:FieldBase?,
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
                                                modelData:ModelData?,
                                                viewData:MutableMap<String,Any>,
                                                pc:PartnerCache,
                                                ownerFieldValue:FieldValue?,
                                                toField:FieldBase?,
                                                reqData: JsonObject?):ModelView{
        var modelDataObject  = modelData as ModelDataObject?
        mv.fields.forEach {
            when (it.type) {
                ModelView.Field.ViewFieldType.many2ManyDataSetSelect,
                ModelView.Field.ViewFieldType.many2OneDataSetSelect-> {
                    if (it.relationData != null &&
                            it.meta == null &&
                            it.style != ModelView.Field.Style.relation &&
                            (it.relationData as ModelView.RelationData).type==ModelView.RelationType.Many2Many &&
                            modelData != null) {

                        var modelObject = this.appModel.getModel(mv.app!!,mv.model!!)
                        var rField = modelObject?.getFieldByPropertyName(it.name) as ModelMany2ManyField?
                        var relationModel = this.appModel.getModel(rField?.relationModelTable)
                        var targetModel=this.appModel.getModel((it.relationData as ModelView.RelationData).targetApp,
                                (it.relationData as ModelView.RelationData).targetModel)
                        if (modelDataObject != null &&
                                relationModel!=null &&
                                targetModel!=null) {
                            var jArr = JsonArray()
                            var dataArray = (modelDataObject?.getFieldValue(ConstRelRegistriesField.ref) as ModelDataSharedObject?)?.data?.get(relationModel) as ModelDataArray?
                            dataArray?.data?.forEach {fvs->
                                fvs.forEach { fv->
                                    if(fv.value is ModelDataObject){
                                        if(targetModel.isSame(fv.value.model)){
                                            jArr.add(this.gson.toJsonTree(fv.value))
                                        }
                                    }
                                }
                            }
                            var metaObj=JsonObject()
                            metaObj.add("options",jArr)
                            it.meta=metaObj
                        }
                    }
                }
            }
        }
        return mv
    }
    protected  open fun fillEditModelViewMeta(mv:ModelView,
                                              modelData:ModelData?,
                                                viewData:MutableMap<String,Any>,
                                                pc:PartnerCache,
                                                ownerFieldValue:FieldValue?,
                                                toField:FieldBase?,
                                                reqData: JsonObject?):ModelView{
        var modelDataObject  = modelData as ModelDataObject?
        mv.fields.forEach {
            when (it.type) {
                ModelView.Field.ViewFieldType.many2ManyDataSetSelect,
                ModelView.Field.ViewFieldType.many2OneDataSetSelect-> {
                    if (it.relationData != null &&
                            it.meta == null &&
                            it.style != ModelView.Field.Style.relation &&
                            (it.relationData as ModelView.RelationData).type==ModelView.RelationType.Many2Many &&
                            modelData != null) {

                        var modelObject = this.appModel.getModel(mv.app!!,mv.model!!)
                        var rField = modelObject?.getFieldByPropertyName(it.name) as ModelMany2ManyField?
                        var relationModel = this.appModel.getModel(rField?.relationModelTable)
                        var targetModel=this.appModel.getModel((it.relationData as ModelView.RelationData).targetApp,
                                (it.relationData as ModelView.RelationData).targetModel)
                        if (modelDataObject != null &&
                                relationModel!=null &&
                                targetModel!=null) {
                            var jArr = JsonArray()
                            var dataArray = (modelDataObject?.getFieldValue(ConstRelRegistriesField.ref) as ModelDataSharedObject?)?.data?.get(relationModel) as ModelDataArray?
                            dataArray?.data?.forEach {fvs->
                                fvs.forEach { fv->
                                    if(fv.value is ModelDataObject){
                                        if(targetModel.isSame(fv.value.model)){
                                            jArr.add(this.gson.toJsonTree(fv.value))
                                        }
                                    }
                                }
                            }
                            var metaObj=JsonObject()
                            metaObj.add("options",jArr)
                            it.meta=metaObj
                        }
                    }
                }
            }
        }
        return mv
    }
    protected  open fun fillListModelViewMeta(mv:ModelView,
                                              modelData:ModelData?,
                                              viewData:MutableMap<String,Any>,
                                              pc:PartnerCache,
                                              ownerFieldValue:FieldValue?,
                                              toField:FieldBase?,
                                              reqData: JsonObject?):ModelView{


        return mv
    }

    protected  open fun fillCreateModelViewMeta(mv:ModelView,
                                                modelData:ModelData?,
                                                viewData:MutableMap<String,Any>,
                                                pc:PartnerCache,
                                                ownerFieldValue:FieldValue?,
                                                toField:FieldBase?,
                                                reqData: JsonObject?):ModelView{
        mv.fields.forEach {
            when(it.type){
                ModelView.Field.ViewFieldType.many2OneDataSetSelect,
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
        }
        return mv
    }

    protected open fun loadModelViewData(mv:ModelView,
                                         viewData:MutableMap<String,Any>,
                                         pc:PartnerCache,
                                         ownerFieldValue:FieldValue?,
                                         toField:FieldBase?,
                                         ownerModelID: Long?,
                                         reqData:JsonObject?):ModelData?{
        when(mv.viewType){
            ModelView.ViewType.CREATE->{
               return this.loadCreateModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.DETAIL->{
                return this.loadDetailModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.EDIT->{
                return this.loadEditModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
            ModelView.ViewType.LIST->{
                return this.loadListModelViewData(mv,viewData,pc,ownerFieldValue,toField,ownerModelID,reqData)
            }
        }
        return null
    }
    private fun getModelViewFields(mv:ModelView):ArrayList<FieldBase>{
        var fields = arrayListOf<FieldBase>()
        var fFields = arrayListOf<FunctionField<*>>()
        if(mv.app.isNullOrEmpty() || mv.model.isNullOrEmpty()){
            return fields
        }
        var modelObj: ModelBase? = this.appModel.getModel(mv.app,mv.model) ?: return fields
        mv.fields.forEach {
            if(it.relationData==null){
                var field = modelObj?.getFieldByPropertyName(it.name)
                field?.let {fd->
                    if(fd !is FunctionField<*>){
                        if(fd !in fields){
                            fields.add(fd)
                        }
                    }
                    else{
                        if(fd !in fFields){
                            fFields.add(fd)
                        }
                        fd.depFields.forEach {dFD->
                            if(dFD !is FunctionField<*>){
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
                                               ownerFieldValue:FieldValue?,
                                               toField:FieldBase?,
                                               ownerModelID: Long?,
                                               reqData:JsonObject?):ModelDataObject? {

        return null
    }

    protected open fun loadDetailModelViewData(mv:ModelView,
                                               viewData:MutableMap<String,Any>,
                                               pc:PartnerCache,
                                               ownerFieldValue:FieldValue?,
                                               toField:FieldBase?,
                                               ownerModelID: Long?,
                                               reqData:JsonObject?):ModelDataObject?{
        val id = reqData?.get("id")?.asLong
        val fields= this.getModelViewFields(mv)
        id?.let {
            val idField = this.fields.getIdField()
            idField?.let { idf->
                var data = this.acRead(*fields.toTypedArray(),criteria = eq(idf,it), partnerCache = pc)
                data?.let {
                    if(it.data.count()>0){
                        return this.toClientModelData(it.firstOrNull(),arrayListOf(*fields.filter {_f->
                            _f is ModelMany2ManyField
                        }.toTypedArray())) as ModelDataObject?
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
                        _f is ModelMany2ManyField
                    }.toTypedArray())) as ModelDataObject?
                }
            }
        }
        return null
    }
    private fun getCriteriaByOwnerModelParam(ownerFieldValue: FieldValue?,toField: FieldBase?,ownerModelID: Long?):Pair<Boolean,ModelExpression?>{

        if(ownerFieldValue!=null && toField!=null){
            if(this.isPersistField(toField)){
                if(ownerFieldValue.value!=Undefined){
                    return Pair(true,eq(toField,ownerFieldValue.value))
                }
                else if(ownerModelID!=null){
                    return Pair(true,eq(toField,ownerModelID))
                }
            }
            else{
                if(toField is ModelMany2ManyField){
                    if(ownerFieldValue.field is ModelMany2ManyField){
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
                    else if(ownerFieldValue.field is ModelMany2OneField){
                        if(ownerFieldValue.value!=Undefined){
                            return Pair(true,eq(toField?.model?.fields?.getIdField()!!,(ownerFieldValue.value as String?)?.toLong()))
                        }
                    }
                }
                else if(toField is ModelOne2ManyField){
                        if(ownerFieldValue.value!=Undefined){
                            return Pair(true,eq(toField.model?.fields?.getIdField()!!,(ownerFieldValue.value as String?)?.toLong()))
                        }
                }
                else if(toField is One2OneField && toField.isVirtualField){
                    if(ownerFieldValue.value!=Undefined){
                        return Pair(true,eq(toField.model?.fields?.getIdField()!!,(ownerFieldValue.value as String?)?.toLong()))
                    }
                }
            }
        }
        return Pair(false,null)
    }
    fun isPersistField(field:FieldBase?):Boolean{
        field?.let {
            if((it !is FunctionField<*>) && (it !is RefRelationField) && it !is One2ManyField){
                if(it !is ModelOne2OneField || !it.isVirtualField){
                    return true
                }
            }
        }
       return false
    }
    protected open fun loadEditModelViewData(mv:ModelView,
                                            viewData:MutableMap<String,Any>,
                                            pc:PartnerCache,
                                            ownerFieldValue:FieldValue?,
                                            toField:FieldBase?,
                                             ownerModelID: Long?,
                                            reqData:JsonObject?):ModelDataObject?{
        val id = reqData?.get("id")?.asInt
        val fields = this.getModelViewFields(mv)
        id?.let {
            val idField = this.fields.getIdField()
            idField?.let { idf->
                var data = this.acRead(*fields.toTypedArray(), criteria = eq(idf,it), partnerCache = pc)
                data?.let {
                    if(it.data.count()>0){
                        return this.toClientModelData(it.firstOrNull(), arrayListOf(*fields.filter {
                            it is ModelMany2ManyField
                        }.toTypedArray())) as ModelDataObject?
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
                        _f is ModelMany2ManyField
                    }.toTypedArray())) as ModelDataObject?
                }
            }
        }
        return null
    }

    private  fun setM2MFieldValue(model:ModelBase,
                                  fvs:FieldValueArray,
                                  fd:FieldBase,
                                  relModel:ModelBase,
                                  relFvs:FieldValueArray){
        val fRelModel = this.appModel.getModel((fd as ModelMany2ManyField).relationModelTable)
        if(relModel.isSame(fRelModel)){
            relFvs.forEach {
                when(it.field){
                    is ModelMany2OneField->{
                        if(it.value is ModelDataObject){
                            var tTargetModel = this.appModel.getModel(it.field.targetModelTable)
                            tTargetModel?.let {_->
                                if(tTargetModel.isSame(model)){
                                    fvs.setValue(fd,it.value)
                                    return@forEach
                                }
                            }
                            var fTargetModel = this.appModel.getModel(fd.targetModelTable)
                            var rTargetModel = this.appModel.getModel(it.field.targetModelTable)
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

    private fun toClientModelData(modelData:ModelData?,m2mFields:ArrayList<FieldBase>):ModelData?{
        modelData?.let {
            when(it){
                is ModelDataObject->{
                    var mso = it.getFieldValue(ConstRelRegistriesField.ref) as ModelDataSharedObject?
                    mso?.let {msoIt->
                        m2mFields.forEach {fd->
                            if(fd is ModelMany2ManyField){
                                var relModel = this.appModel.getModel(fd.relationModelTable)
                                relModel?.let {mRelIt->
                                    msoIt.data[mRelIt]?.let {mdaIt->
                                        this.setM2MFieldValue(fd.model!!,it.data,fd,relModel,(mdaIt as ModelDataArray).data.first())
                                    }
                                }
                            }
                        }
                    }
                }
                is ModelDataArray->{
                    it.data?.forEach {fvs->
                        var mso = fvs.getValue(ConstRelRegistriesField.ref) as ModelDataSharedObject?
                        mso?.let {msoIt->
                            m2mFields.forEach {fd->
                               if(fd is ModelMany2ManyField){
                                   var relModel = this.appModel.getModel(fd.relationModelTable)
                                   relModel?.let {mRelIt->
                                       msoIt.data[mRelIt]?.let {mdaIt->
                                           this.setM2MFieldValue(fd.model!!,fvs,fd,relModel,(mdaIt as ModelDataArray).data.first())
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
    protected open fun processFunctionFieldAfterRead(modelArray:ModelDataArray?,funFields:ArrayList<FunctionField<*>>){

    }
    protected  open fun loadListModelViewData(mv:ModelView,
                                              viewData:MutableMap<String,Any>,
                                              pc:PartnerCache,
                                              ownerFieldValue:FieldValue?,
                                              toField:FieldBase?,
                                              ownerModelID: Long?,
                                              reqData:JsonObject?):ModelDataArray?{
        if(ownerFieldValue!=null){
            if(ownerFieldValue.value==Undefined && ownerModelID==null){
                return null
            }
        }
        val fields = this.getModelViewFields(mv).toTypedArray()
        val pageIndex = reqData?.get("pageIndex")?.asInt?:1
        val pageSize = reqData?.get("pageSize")?.asInt?:10
        val jCriteria = reqData?.get("criteria")?.asJsonObject
        //TODO parse javascript criteria

        var criteria = null as ModelExpression?
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
            _f is ModelMany2ManyField
        }.toTypedArray())) as ModelDataArray?
    }
}