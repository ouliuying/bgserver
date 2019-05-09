

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
        val (ownerFieldValue,toField) = this.getOwnerFieldAndRefField(ownerField,app,model)
        ar.bag = this.loadMainViewType(app,model,viewType,ownerField,pc,reqData,viewRefType)
        return ar
    }
    private fun getOwnerFieldAndRefField(ownerField: JsonObject?,app:String,model:String):Pair<FieldValue?,FieldBase?>{
        ownerField?.let {
            val ownerApp = it["app"]?.asString
            val ownerModel = it["model"]?.asString
            val ownerFieldName = it["name"]?.asString
            val ownerValue = it["value"]?.asLong
        }
        return Pair(null,null)
    }

    private fun loadMainViewType(app:String,model:String,
                                 viewType:String,
                                 ownerField:JsonObject?,
                                 pc:PartnerCache,
                                 reqData:JsonObject?,viewRefType:String?=null):MutableMap<String,Any>{
        var reqRefType= viewRefType?:if(ownerField!=null) ModelViewRefType.Sub else ModelViewRefType.Main
        var mv=pc.getAccessControlModelView(app,model,viewType)
        var bag = mutableMapOf<String,Any>()
        if(mv!=null){
            mv=this.fillModelViewMeta(mv,bag,pc,ownerField,reqData)
            var mvData = this.loadModelViewData(mv,bag, pc,ownerField, reqData)
            bag["view"] = mv
            if(mvData!=null){
                bag["data"]=mvData
            }
            var actionNameArray = arrayListOf<String>()
            mv.refActionGroups.forEach {
                if(it.refTypes.contains(reqRefType)){
                    actionNameArray.add(it.groupName)
                }
            }
            var triggerGroups = this.loadModelViewActionTriggerGroups(mv,actionNameArray.toTypedArray(),pc,reqData)
            if(triggerGroups!=null){
                bag["triggerGroups"]=triggerGroups
            }
            mv.refViews.forEach {
                var refMV= this.loadRefViewType(it,pc,reqData,ownerField,reqRefType)
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

    private fun loadRefViewType(refView:ModelView.RefView, pc:PartnerCache,ownerField:JsonObject?, reqData:JsonObject?, reqRefType:String):Map<String,Any>?{
        var mv=pc.getAccessControlModelView(refView.app,refView.model,refView.viewType)
        if(mv!=null){
            var bag = mutableMapOf<String,Any>()
            bag["refView"]=refView
            if(refView.refTypes.contains(ModelViewRefType.Embedded)){
                bag["view"] = mv
                mv=this.fillModelViewMeta(mv,bag,pc,ownerField,reqData)
                var mvData = this.loadModelViewData(mv, bag,pc, ownerField,reqData)
                if(mvData!=null){
                    bag["data"]=mvData
                }
                var actionNameArray = arrayListOf<String>()
                mv.refActionGroups.forEach {
                    if(it.refTypes.contains(ModelViewRefType.Sub)){
                        actionNameArray.add(it.groupName)
                    }
                }
                var triggerGroups = this.loadModelViewActionTriggerGroups(mv,actionNameArray.toTypedArray(),pc,reqData)
                if(triggerGroups!=null){
                    bag["triggerGroups"]=triggerGroups
                }
                mv.refViews.forEach {
                    var refMV= this.loadRefViewType(it,pc,ownerField,reqData,ModelViewRefType.Sub)
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

    protected open fun loadModelViewActionTriggerGroups(mv:ModelView,actionNames:Array<String>,pc:PartnerCache,reqData: JsonObject?):Array<TriggerGroup>?{
        var triggerGroups = arrayListOf<TriggerGroup>()
        actionNames.forEach {
            val tg=pc.getAccessControlModelViewActionGroup(mv.app!!,mv.model!!,mv.viewType!!,it)
            if(tg!=null){
                triggerGroups.add(tg)
            }
        }
        return triggerGroups.toTypedArray()
    }

    protected  open fun fillModelViewMeta(mv:ModelView,viewData:MutableMap<String,Any>,pc:PartnerCache,ownerField:JsonObject?,reqData: JsonObject?):ModelView{


        when(mv.viewType){
            ModelView.ViewType.CREATE->{
                return this.fillCreateModelViewMeta(mv,viewData,pc,ownerField,reqData)
            }
            ModelView.ViewType.DETAIL->{

            }
            ModelView.ViewType.EDIT->{

            }
            ModelView.ViewType.LIST->{

            }
        }
        return mv
    }

    protected  open fun fillCreateModelViewMeta(mv:ModelView,viewData:MutableMap<String,Any>,pc:PartnerCache,ownerField:JsonObject?,reqData: JsonObject?):ModelView{
        mv.fields.forEach {
            when(it.type){
                ModelView.Field.ViewFieldType.many2OneDataSetSelect,ModelView.Field.ViewFieldType.many2ManyDataSetSelect->{
                    if(it.relationData!=null && it.meta==null){
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

    protected open fun loadModelViewData(mv:ModelView,viewData:MutableMap<String,Any>,pc:PartnerCache,ownerField:JsonObject?, reqData:JsonObject?):JsonObject?{
        when(mv.viewType){
            ModelView.ViewType.CREATE->{
               return this.loadCreateModelViewData(mv,viewData,pc,ownerField,reqData)
            }
            ModelView.ViewType.DETAIL->{
                return this.loadDetailModelViewData(mv,viewData,pc,ownerField,reqData)
            }
            ModelView.ViewType.EDIT->{
                return this.loadEditModelViewData(mv,viewData,pc,ownerField,reqData)
            }
            ModelView.ViewType.LIST->{
                return this.loadListModelViewData(mv,viewData,pc,ownerField,reqData)
            }
        }
        return null
    }

    protected open fun loadCreateModelViewData(mv:ModelView,viewData:MutableMap<String,Any>,pc:PartnerCache,ownerField:JsonObject?,reqData:JsonObject?):JsonObject? {

        return JsonObject()
    }

    protected open fun loadDetailModelViewData(mv:ModelView,
                                               viewData:MutableMap<String,Any>,
                                               pc:PartnerCache,
                                               ownerField:JsonObject?,
                                               reqData:JsonObject?):JsonObject?{
        val id = reqData?.get("id")?.asInt
        id?.let {
            val idField = this.fields.getIdField()
            idField?.let { idf->
                var data = this.acRead(criteria = eq(idf,it), partnerCache = pc)
                data?.let {
                    if(it.data.count()>0){
                        return this.gson.toJsonTree(it.firstOrNull()) as JsonObject
                    }
                }
            }
        }
        return null
    }

    protected open fun loadEditModelViewData(mv:ModelView,
                                               viewData:MutableMap<String,Any>,
                                               pc:PartnerCache,
                                             ownerField:JsonObject?,
                                               reqData:JsonObject?):JsonObject?{
        val id = reqData?.get("id")?.asInt
        id?.let {
            val idField = this.fields.getIdField()
            idField?.let { idf->
                var data = this.acRead(criteria = eq(idf,it), partnerCache = pc)
                data?.let {
                    if(it.data.count()>0){
                        return this.gson.toJsonTree(it.firstOrNull()) as JsonObject
                    }
                }
            }
        }
        return null
    }

    protected  open fun loadListModelViewData(mv:ModelView,
                                              viewData:MutableMap<String,Any>,
                                              pc:PartnerCache,
                                              ownerField:JsonObject?,
                                              reqData:JsonObject?):JsonObject?{
        val pageIndex = reqData?.get("pageIndex")?.asInt?:1
        val pageSize = reqData?.get("pageSize")?.asInt?:10
        val jCriteria = reqData?.get("criteria")?.asJsonObject
        //TODO parse javascript criteria
        var criteria = null as ModelExpression?
        jCriteria?.let {
            criteria = JsonClauseResolver(it,this,pc.modelExpressionContext).criteria()
        }
        var data = this.acRead(partnerCache = pc,pageIndex = pageIndex,pageSize = pageSize,criteria = criteria)
        var totalCount = this.acCount(criteria = criteria,partnerCache = pc)
        viewData["totalCount"]=totalCount
        return if(data!=null) this.gson.toJsonTree(data) as JsonObject? else null
    }
}