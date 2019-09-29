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

package work.bg.server.chat.field

import dynamic.model.query.mq.*
import dynamic.model.query.mq.model.ModelBase
import work.bg.server.chat.model.ChatModelJoinChannelRel
import work.bg.server.core.cache.PartnerCache
import work.bg.server.util.TypeConvert

class PartnerJoinStatusField(model: ModelBase?,
                             name:String,
                             title:String?,
                             val owner: FieldBase?): FunctionField<Int,PartnerCache>(model,
        name,
        FieldType.STRING,
        title,
        null,
        null,
        depFields= arrayOf(owner)){
    override fun compute(fieldValueArray: FieldValueArray,
                         partnerCache: PartnerCache?,
                         data: Any?): Int? {
        if(owner!=null){
            val currPartnerID = partnerCache?.partnerID
           val ownerObj =  fieldValueArray.getValue(owner)
            if(ownerObj!=null){
                val mo = ownerObj as ModelDataObject
                var idValue = mo.idFieldValue
                val partnerID = TypeConvert.getLong(idValue?.value as Number)
                if(currPartnerID!=null && currPartnerID == partnerID){
                    return 1
                }
            }
        }
        val idField = this.model?.fields?.getIdField()
        if(idField!=null){
            val id = work.bg.server.util.TypeConvert.getLong(fieldValueArray.getValue(idField) as Number?)
            val currPartnerID = partnerCache?.partnerID
            if(id!=null && id>0 && currPartnerID!=null && currPartnerID>0){
               if(ChatModelJoinChannelRel.ref.rawCount(and(eq(ChatModelJoinChannelRel.ref.joinChannel,currPartnerID),
                               eq(ChatModelJoinChannelRel.ref.joinChannel,id)),partnerCache, partnerCache!=null)>0){
                   return 2
               }
            }
        }
        return 0
    }
}