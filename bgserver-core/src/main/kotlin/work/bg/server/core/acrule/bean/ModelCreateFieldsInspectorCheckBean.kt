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

package work.bg.server.core.acrule.bean

import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelCreateRecordFieldsValueCheckRule
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldMustCoexist
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.FieldType
import work.bg.server.core.mq.ModelDataObject
import work.bg.server.core.mq.ModelField
@Component
class ModelCreateFieldsInspectorCheckBean :ModelCreateRecordFieldsValueCheckRule<Array<ModelFieldInspector>>{
    private lateinit var _config:String
    override  var config: String
        get() {
            return this._config
        }
        set(value) {
            this._config=value
        }
    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Array<ModelFieldInspector>?): Pair<Boolean, String> {
        data?.forEach {
            when(it){
                is ModelFieldRequired->{
                    if(!this.fieldsExist(it.targetFields,modelData)){
                        return Pair(false,it.advice)
                    }
                }
                is ModelFieldNotNullOrEmpty->{
                    if(!this.fieldsNotNullOrEmpty(it.targetFields,modelData,it.canBeBlank)){
                        return Pair(false,it.advice)
                    }
                }
                is ModelFieldMustCoexist->{
                    if(!this.fieldsCoexist(it.targetFields,modelData)){
                        return Pair(false,it.advice)
                    }
                }
            }
        }
        return Pair(true,"")
    }
    private fun fieldsExist(fields:Array<out ModelField>,modelData:ModelDataObject):Boolean{
        fields.forEach {
            var fv=modelData.data.filter {mFV->
                mFV.field.isSame(it)
            }
            if(fv.count()<1){
                return false
            }
        }
        return true
    }

    private fun fieldsCoexist(fields:Array<out ModelField>,modelData:ModelDataObject):Boolean{
        val existCount = modelData.data.count m@{
           return fields.firstOrNull {
               m@it.isSame(it)
           }!=null
        }
        return (existCount==0||existCount==fields.count())
    }

    private  fun fieldsNotNullOrEmpty(fields:Array<out ModelField>,modelData:ModelDataObject,canBeBlank:Boolean):Boolean{
        fields.forEach {
            var fv=modelData.data.filter {mFV->
                mFV.field.isSame(it)
            }
            if(fv.count()<1){
                return@forEach
            }
            if(fv[0].field.fieldType!= FieldType.STRING && fv[0].value==null){
                return false
            }
            else if(fv[0].field.fieldType==FieldType.STRING){
                if(fv[0].value==null || (fv[0].value as String).isNullOrEmpty()){
                    return false
                }
                else if(!canBeBlank){
                    if((fv[0].value as String).isNullOrBlank()){
                        return false
                    }
                }
            }
        }
        return true
    }
}