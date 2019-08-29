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

import work.bg.server.core.RefSingleton
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model
@Model(name = "corp", title="公司")
class BaseCorp(table:String,schema:String):ContextModel(table,schema){
  companion object:RefSingleton<BaseCorp>{
    override lateinit var ref: BaseCorp
  }

  constructor():this("base_corp","public")

  override fun skipCorpIsolationFields(): Boolean {
      return true
  }

  val id=ModelField(null,"id",FieldType.BIGINT,"标识",primaryKey = FieldPrimaryKey())
  val name=ModelField(null,"name",FieldType.STRING,"名称")
  val website=ModelField(null,"website",FieldType.STRING,"网站")
  val address=ModelField(null,"address",FieldType.STRING,"地址")
  val telephone=ModelField(null,"telephone",FieldType.STRING,"电话")
  val fax=ModelField(null,"fax",FieldType.STRING,"传真")
  val comment=ModelField(null,"comment",FieldType.TEXT,"注释")
  val partners=ModelMany2ManyField(null,"partner_id",FieldType.BIGINT,"用户","public.base_corp_partner_rel","partner_id",
          "base_partner","id")
  val partnerRoles=ModelOne2ManyField(null,
          "m_corp_id",
          FieldType.BIGINT,"角色",
          "public.base_partner_role",
          "corp_id")
}
