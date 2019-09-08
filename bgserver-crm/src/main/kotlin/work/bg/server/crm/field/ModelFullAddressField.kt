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

package work.bg.server.crm.field

import com.google.gson.Gson
import dynamic.model.query.mq.model.ModelBase
import org.springframework.boot.json.GsonJsonParser
import work.bg.server.core.cache.PartnerCache

class ModelFullAddressField(model: ModelBase?, name:String,
                            title:String?,
                            val province: dynamic.model.query.mq.FieldBase,
                            val city: dynamic.model.query.mq.FieldBase,
                            val district: dynamic.model.query.mq.FieldBase,
                            val streetAddress: dynamic.model.query.mq.FieldBase, val json:Gson): dynamic.model.query.mq.FunctionField<String,PartnerCache>(model,
        name,
        dynamic.model.query.mq.FieldType.STRING,
        title,null,null,depFields= arrayOf(province, city, district, streetAddress)) {
    //before to ui
    override fun compute(fieldValueArray: dynamic.model.query.mq.FieldValueArray, partnerCache: PartnerCache?, data: Any?): String? {
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
    override fun inverse(fieldValueArray: dynamic.model.query.mq.FieldValueArray, partnerCache: PartnerCache?, value: String?, data: Any?) {
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