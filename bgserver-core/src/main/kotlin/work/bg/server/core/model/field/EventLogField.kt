/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.core.model.field

import dynamic.model.query.mq.FieldType
import dynamic.model.query.mq.FieldValueArray
import dynamic.model.query.mq.FunctionField
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.web.context.ContextType
import work.bg.server.core.cache.PartnerCache

class EventLogField (model: ModelBase?, name:String,
                     title:String? ):FunctionField<Long,PartnerCache>(model,name,FieldType.BIGINT,title) {
    override fun compute(fieldValueArray: FieldValueArray, context: PartnerCache?, data: Any?): Long? {
        val idField = model?.fields?.getIdField()
        fieldValueArray.setValue(this,if(idField!=null) fieldValueArray.getValue(idField) else null)
        return 1
    }

    override fun inverse(fieldValueArray: FieldValueArray, partnerCache: PartnerCache?, value: Long?, data: Any?) {
        fieldValueArray.removeIf {
            it.field.name == this.name
        }
    }
}