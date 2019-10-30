

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

package dynamic.model.query.mq

import dynamic.model.query.mq.model.ModelBase

class ModelTableColumnAliasNameGenerator : ModelTableColumnNameGenerator {
    private var namedParameterIndex = 0
    override fun generateColumnName(field: FieldBase?, onlyFieldName:Boolean): String {
        //val name=

        return if(!onlyFieldName){

                "${field?.model?.schemaName}.${field?.model?.tableName}.${field?.name}"

        }
        else{
                "${field?.name}"
        }

    }

    override fun generateTableName(model: ModelBase?): String {
        return "${model?.schemaName}.${model?.tableName}"
    }

    override fun generateNamedParameter(columnName: String): Pair<String,String> {
        namedParameterIndex++
        return Pair(":$columnName$namedParameterIndex","$columnName$namedParameterIndex")
    }
}