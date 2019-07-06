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

package work.bg.server.crm.field

import com.google.gson.Gson
import org.springframework.boot.json.GsonJsonParser
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.*

class ModelFullAddressField(model:ModelBase?,name:String,
                            title:String?,
                            val province:FieldBase,
                            val city:FieldBase,
                            val district:FieldBase,
                            val streetAddress:FieldBase,val json:Gson):FunctionField<String>(model,
        name,
        FieldType.STRING,
        title,null,null,depFields= arrayOf(province, city, district, streetAddress)) {
    //before to ui
    override fun compute(fieldValueArray: FieldValueArray, partnerCache: PartnerCache?, data: Any?): String? {
        val str= this.json.toJson(mapOf(
                province.propertyName to fieldValueArray.getValue(this.province),
                city.propertyName to fieldValueArray.getValue(this.city),
                district.propertyName to fieldValueArray.getValue(this.district),
                streetAddress.propertyName to fieldValueArray.getValue(this.streetAddress)
        ))
        fieldValueArray.setValue(this,str)
        return str
    }

    //before to database
    override fun inverse(fieldValueArray: FieldValueArray, partnerCache: PartnerCache?, value: String?, data: Any?) {
      val fullValue = fieldValueArray.getValue(this)
        if(fullValue!=null){
            val pMap = this.json.fromJson(fullValue as String,Map::class.java) as Map<String,String?>
            if(pMap!=null){
                fieldValueArray.setValue(this.province,pMap[this.province.propertyName]?:"")
                fieldValueArray.setValue(this.city,pMap[this.city.propertyName]?:"")
                fieldValueArray.setValue(this.district,pMap[this.district.propertyName]?:"")
                fieldValueArray.setValue(this.streetAddress,pMap[this.streetAddress.propertyName]?:"")
            }
        }
    }
}