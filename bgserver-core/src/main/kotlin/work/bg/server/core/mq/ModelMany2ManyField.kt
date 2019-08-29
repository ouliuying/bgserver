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

package work.bg.server.core.mq

class ModelMany2ManyField constructor(model:ModelBase?,
                                      name:String,
                                      fieldType:FieldType,
                                      title:String?,
                                      override val relationModelTable:String?,
                                      override val relationModelFieldName: String?,
                                      override val targetModelTable: String?=null,
                                      override val targetModelFieldName: String?=null,
                                      override val foreignKey: FieldForeignKey?=null,
                                      override val paging:Boolean?=true,
                                      override val pageSize: Int?=10
                                      ):
        ModelField(model,name,fieldType,title),Many2ManyField,PagingField{

}