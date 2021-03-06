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

package work.bg.server.sms.model

import com.google.gson.JsonObject
import dynamic.model.query.mq.ModelDataObject
import org.springframework.web.bind.annotation.RequestBody
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.ui.ModelView
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.model.ActionResult

@Model("smsSetting","短信设置")
class SmsSetting:ContextModel("sms_setting","public") {
    companion object : RefSingleton<SmsSetting> {
        override lateinit var ref: SmsSetting
    }

    val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.BIGINT, "标识", primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val apiKey = dynamic.model.query.mq.ModelField(null,
            "apikey",
            dynamic.model.query.mq.FieldType.STRING,
            title = "ApiKey")

    val apiSecret = dynamic.model.query.mq.ModelField(null,
            "apisecret",
            dynamic.model.query.mq.FieldType.STRING,
            title = "ApiSecret")

    @Action(name="edit")
    override fun editAction(@RequestBody modelData: dynamic.model.query.mq.ModelData?, pc: PartnerCache): ActionResult?{
        var ar= ActionResult()
        if(modelData!=null){
            if(modelData is dynamic.model.query.mq.ModelDataObject){
                if(modelData.idFieldValue?.value!=null){
                    var ret=this.safeEdit(modelData,criteria = null,partnerCache = pc,useAccessControl = true)
                    if(ret.first !=null && ret.first!! > 0){
                        ar.bag["result"]=ret.first!!
                        return ar
                    }
                    ar.description= ret.second
                }
                else{
                    var ret=this.safeCreate(modelData,partnerCache = pc,useAccessControl = true)
                    if(ret.first !=null && ret.first!! > 0){
                        ar.bag["result"]=ret.first!!
                        return ar
                    }
                    ar.description= ret.second
                }
            }
        }
        ar.errorCode= ErrorCode.UPDATEMODELFAIL
        return ar
    }


    override fun loadEditModelViewData(mv: ModelView,
                                                 viewData:MutableMap<String,Any>,
                                                 pc:PartnerCache,
                                                 ownerFieldValue: dynamic.model.query.mq.FieldValue?,
                                                 toField: dynamic.model.query.mq.FieldBase?,
                                                 ownerModelID: Long?,
                                                 reqData: JsonObject?): dynamic.model.query.mq.ModelDataObject?{
        val fields = this.getModelViewFields(mv)
        var data = this.rawRead(*fields.toTypedArray(), criteria = null, partnerCache = pc,useAccessControl = true)
        data?.let {
            if(it.data.count()>0){
                return this.toClientModelData(it.firstOrNull(), arrayListOf(*fields.filter {
                    it is dynamic.model.query.mq.ModelMany2ManyField
                }.toTypedArray())) as dynamic.model.query.mq.ModelDataObject?
            }
        }
        val (ret,criteria) = this.getCriteriaByOwnerModelParam(ownerFieldValue,toField,ownerModelID)
        if(ret) {
            var data = this.rawRead(*fields.toTypedArray(), criteria = criteria, partnerCache = pc,useAccessControl = true)
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
    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }

    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}