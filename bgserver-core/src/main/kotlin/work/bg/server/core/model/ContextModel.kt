

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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheRegistry
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.core.spring.boot.model.ReadActionParam
import work.bg.server.core.ui.ModelView
import work.bg.server.core.ui.UICache
import work.bg.server.errorcode.ErrorCode

abstract  class ContextModel(tableName:String,schemaName:String):AccessControlModel(tableName,schemaName) {
    @Autowired
    var partnerCacheRegistry:PartnerCacheRegistry?=null
    @Autowired
    val gson: Gson?=null
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
            if(ret.first!=null){
                ar.bag["id"]=ret.first!!
                return ar
            }
        }
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
    open fun deleteAction(@RequestBody modelData:ModelData?,pc:PartnerCache): ActionResult?{
        var ar=ActionResult()


        return ar
    }
    @Action(name="feedViewField")
    open fun feedViewFieldAction(@RequestBody param: JsonArray, pc:PartnerCache):ActionResult?{
        //todo remove constant word
        var ar=ActionResult()
        if(param.count()>0){
            param.forEach {
                var viewFieldType=(it as JsonObject)["viewFieldType"].asString
                when{
                    viewFieldType.compareTo(ModelView.Field.ViewFieldType.many2OneDataSetSelect,
                            true)==0->{
                        if(!it.has("parentName")){
                            var name=it["name"].asString
                            var field=this.fields.getFieldByPropertyName(name)
                            if(field!=null){
                                var tf=this.getTargetModelField(field)
                                if(tf!=null){
                                    if(tf.first!=null){
                                        var acModel=tf.first as AccessControlModel
                                        var modelData=acModel.acRead(
                                                *acModel.fields.getAllPersistFields().values.toTypedArray(),criteria = null,partnerCache = pc, pageIndex = 1,pageSize=10
                                        )
                                        var feedData=this.gson?.toJsonTree(modelData)
                                        it.add("feedData",feedData)

                                    }
                                }
                            }
                        }
                        else{
                            var pname=it["parentName"].asString
                            var field=this.fields.getFieldByPropertyName(pname)
                            if(field!=null){
                                var tf=this.getTargetModelField(field)
                                if(tf!=null) {
                                    var subModel=tf.first as ContextModel?
                                    if(subModel!=null){
                                        var name=it["name"].asString
                                        var field=subModel.fields.getFieldByPropertyName(name)
                                        if(field!=null){
                                            var subtf=subModel.getTargetModelField(field)
                                            if(subtf!=null){
                                                if(subtf.first!=null){
                                                    var acModel=subtf.first as AccessControlModel
                                                    var modelData=acModel.acRead(
                                                            *acModel.fields.getAllPersistFields().values.toTypedArray(),criteria = null,partnerCache = pc, pageIndex = 1,pageSize=10
                                                    )
                                                    var feedData=this.gson?.toJsonTree(modelData)
                                                    it.add("feedData",feedData)

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ar.bag["feedData"]=param
        }
        return ar
    }

    @Action(name="loadModelViewType")
    fun loadModelViewType(@RequestBody data:JsonObject, pc:PartnerCache):ActionResult{
        var ar=ActionResult()
        val app =this.meta.appName
        val model = this.meta.name
        val viewType = data["viewType"].asString
        val reqData = data["reqData"].asJsonObject
        var mv=pc.getAccessControlModelView(app,model,viewType)
        if(mv!=null){
            mv=this.fillModelViewMeta(mv,pc,reqData)
            var mvData = this.loadModelViewData(mv, pc, reqData)
            ar.bag["view"] = mv
            if(mvData!=null){
                ar.bag["data"]=mvData
            }
        }
        return ar
    }
    private fun fillModelViewMeta(mv:ModelView,pc:PartnerCache,reqData: JsonObject):ModelView{
        when(mv.viewType){
            ModelView.ViewType.CREATE->{
                return this.fillCreateModelViewMeta(mv,pc,reqData)
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
    private fun fillCreateModelViewMeta(mv:ModelView,pc:PartnerCache,reqData: JsonObject):ModelView{

        return mv
    }
    private fun loadModelViewData(mv:ModelView,pc:PartnerCache,reqData:JsonObject):JsonObject?{
        when(mv.viewType){
            ModelView.ViewType.CREATE->{
               return this.loadCreateModelViewData(mv,pc,reqData)
            }
            ModelView.ViewType.DETAIL->{

            }
            ModelView.ViewType.EDIT->{

            }
            ModelView.ViewType.LIST->{

            }
        }
        return null
    }
    private fun loadCreateModelViewData(mv:ModelView,pc:PartnerCache,reqData:JsonObject):JsonObject? {
        return null
    }


    /**
     * actions end
     */

}