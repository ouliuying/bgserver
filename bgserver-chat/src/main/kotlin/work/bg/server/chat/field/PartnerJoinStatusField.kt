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

package work.bg.server.chat.field

import com.google.gson.Gson
import work.bg.server.chat.model.ChatModelJoinChannelRel
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.*

class PartnerJoinStatusField(model: ModelBase?,
                             name:String,
                             title:String?,
                             val owner:FieldBase?): FunctionField<Int>(model,
        name,
        FieldType.STRING,
        title,null,null,depFields= arrayOf(owner)){
    override fun compute(fieldValueArray: FieldValueArray, partnerCache: PartnerCache?, data: Any?): Int? {
        if(owner!=null){
            val currPartnerID = partnerCache?.partnerID
           val ownerObj =  fieldValueArray.getValue(owner)
            if(ownerObj!=null){
                val mo = ownerObj as ModelDataObject
                var idValue = mo.idFieldValue
                val partnerID = work.bg.server.util.TypeConvert.getLong(idValue?.value as Number)
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