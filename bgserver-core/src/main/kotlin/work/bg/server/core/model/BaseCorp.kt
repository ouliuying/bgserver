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

import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.ui.ModelView
import kotlin.reflect.KClass

@Model(name = "corp", title="公司")
open class BaseCorp(table:String, schema:String):ContextModel(table,schema){
  companion object: RefSingleton<BaseCorp> {
    override lateinit var ref: BaseCorp
  }

  constructor():this("base_corp","public")

  override fun <T : ModelBase> getModelFields(overrideBaseCls: KClass<T>?): FieldCollection {
    return super.getModelFields(BaseCorp::class)
  }

  override fun corpIsolationFields(): Array<ModelField>? {
    return null
  }
  val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.BIGINT, "标识", primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
  val name= dynamic.model.query.mq.ModelField(null, "name", dynamic.model.query.mq.FieldType.STRING, "名称")
  val website= dynamic.model.query.mq.ModelField(null, "website", dynamic.model.query.mq.FieldType.STRING, "网站")
  val address= dynamic.model.query.mq.ModelField(null, "address", dynamic.model.query.mq.FieldType.STRING, "地址")
  val telephone= dynamic.model.query.mq.ModelField(null, "telephone", dynamic.model.query.mq.FieldType.STRING, "电话")
  val fax= dynamic.model.query.mq.ModelField(null, "fax", dynamic.model.query.mq.FieldType.STRING, "传真")
  val comment= dynamic.model.query.mq.ModelField(null, "comment", dynamic.model.query.mq.FieldType.TEXT, "注释")
  val partners= dynamic.model.query.mq.ModelMany2ManyField(null, "partner_id", dynamic.model.query.mq.FieldType.BIGINT, "用户", "public.base_corp_partner_rel", "partner_id",
          "base_partner", "id")
  val partnerRoles= dynamic.model.query.mq.ModelOne2ManyField(null,
          "m_corp_id",
          dynamic.model.query.mq.FieldType.BIGINT, "角色",
          "public.base_partner_role",
          "corp_id")
}
