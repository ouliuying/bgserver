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

import work.bg.server.core.mq.FieldBase
import work.bg.server.core.mq.ModelBase

class ModelRelationMatcher {
    private  var matchMap= mutableMapOf<MatchDataKey,MatchField>()
    fun addMatchData(fromModel: ModelBase?,
                     fromField:FieldBase?,
                     toModel: ModelBase?,
                     toField:FieldBase?,
                     realFromField:FieldBase?=null){
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
            val fromField:FieldBase?,
            val toField:FieldBase?,
            val realFromField:FieldBase?
    )
}