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

class ModelEnumField constructor(model: ModelBase, name:String, fieldTyp: dynamic.model.query.mq.FieldType, title:String?, override val enumMetas: Array<dynamic.model.query.mq.EnumFieldMeta>, val defautlValue: dynamic.model.query.mq.EnumFieldMeta?=null, val ignoreValue: dynamic.model.query.mq.EnumFieldMeta?=null): dynamic.model.query.mq.ModelField(model,name,fieldTyp,title), dynamic.model.query.mq.EnumField