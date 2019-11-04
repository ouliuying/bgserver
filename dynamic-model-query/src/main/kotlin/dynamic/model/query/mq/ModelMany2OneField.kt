

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

class ModelMany2OneField(model: ModelBase?,
                         name:String,
                         fieldType: dynamic.model.query.mq.FieldType,
                         title:String?,
                         override val targetModelTable: String?=null,
                         override val targetModelFieldName: String?=null,
                         override val foreignKey: dynamic.model.query.mq.FieldForeignKey?=null,
                         defaultValue:Any?=null
                         ): dynamic.model.query.mq.ModelField(model,name,fieldType,title,defaultValue=defaultValue), dynamic.model.query.mq.Many2OneField