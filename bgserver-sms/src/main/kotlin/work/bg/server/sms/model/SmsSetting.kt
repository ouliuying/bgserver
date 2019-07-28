/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.sms.model

import com.google.gson.JsonObject
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.ContextModel
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.core.ui.ModelView
import work.bg.server.errorcode.ErrorCode

@Model("smsSetting")
class SmsSetting:ContextModel("sms_setting","public") {
    companion object : RefSingleton<SmsSetting> {
        override lateinit var ref: SmsSetting
    }
    val id=ModelField(null,"id",FieldType.BIGINT,"标识",primaryKey = FieldPrimaryKey())
    val userName = ModelField(null,
            "userName",
            FieldType.STRING,
            title = "账号")

    val password = ModelField(null,
            "password",
            FieldType.STRING,
            title = "密码")

    @Action(name="edit")
    override fun editAction(@RequestBody modelData: ModelData?, pc: PartnerCache): ActionResult?{
        var ar= ActionResult()
        if(modelData!=null){
            if(modelData is ModelDataObject){
                if(modelData.idFieldValue?.value!=null){
                    var ret=this.acEdit(modelData,criteria = null,partnerCache = pc)
                    if(ret?.first!=null && ret?.first!! > 0){
                        ar.bag["result"]=ret.first!!
                        return ar
                    }
                    ar.description=ret?.second
                }
                else{
                    var ret=this.acCreate(modelData,partnerCache = pc)
                    if(ret?.first!=null && ret?.first!! > 0){
                        ar.bag["result"]=ret.first!!
                        return ar
                    }
                    ar.description=ret?.second
                }
            }
        }
        ar.errorCode= ErrorCode.UPDATEMODELFAIL
        return ar
    }


    protected override fun loadEditModelViewData(mv: ModelView,
                                             viewData:MutableMap<String,Any>,
                                             pc:PartnerCache,
                                             ownerFieldValue: FieldValue?,
                                             toField: FieldBase?,
                                             ownerModelID: Long?,
                                             reqData: JsonObject?):ModelDataObject?{
        val fields = this.getModelViewFields(mv)
        var data = this.acRead(*fields.toTypedArray(), criteria = null, partnerCache = pc)
        data?.let {
            if(it.data.count()>0){
                return this.toClientModelData(it.firstOrNull(), arrayListOf(*fields.filter {
                    it is ModelMany2ManyField
                }.toTypedArray())) as ModelDataObject?
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
}