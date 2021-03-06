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

package work.bg.server.core.model

import dynamic.model.query.mq.FieldBase
import dynamic.model.query.mq.model.ModelBase

class ModelRelationMatcher {
    private  var matchMap= mutableMapOf<MatchDataKey,MatchField>()
    fun addMatchData(fromModel: ModelBase?,
                     fromField: dynamic.model.query.mq.FieldBase?,
                     toModel: ModelBase?,
                     toField: dynamic.model.query.mq.FieldBase?,
                     realFromField: dynamic.model.query.mq.FieldBase?=null){
        this.matchMap[MatchDataKey(fromModel, toModel)]= MatchField(fromField,toField,realFromField)
    }
    fun getRelationMatchField(fromModel: ModelBase?, toModel: ModelBase?):MatchField?{
        var key= MatchDataKey(fromModel, toModel)
        return this.matchMap[key]
    }
    data class MatchDataKey(
            val fromModel: ModelBase?,
            val toModel: ModelBase?
    )
    data class MatchField(
            val fromField: dynamic.model.query.mq.FieldBase?,
            val toField: dynamic.model.query.mq.FieldBase?,
            val realFromField: dynamic.model.query.mq.FieldBase?
    )
}