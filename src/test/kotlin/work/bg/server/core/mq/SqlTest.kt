

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

import dynamic.model.query.mq.eq
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.query.mq.select
import dynamic.model.query.mq.update
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.reflect.KClass

class UserModel : ModelBase("user_table") {
    val id= dynamic.model.query.mq.ModelField(null, "id", dynamic.model.query.mq.FieldType.INT, "标示")
    val userName= dynamic.model.query.mq.ModelField(null, "user_name", dynamic.model.query.mq.FieldType.STRING, "用户名")
    val password= dynamic.model.query.mq.ModelField(null, "password", dynamic.model.query.mq.FieldType.STRING, "用户密码")
}

class SelectTest : StringSpec({
    // tests here
    var model = UserModel()
    var totalFields=model.getModelFields(null as KClass<ModelBase>)
    var fields = totalFields.toArray()
    var sel = select(*fields!!, fromModel = model).where(eq(fields!!.first(), 1))
    var ret = sel.render(null)
    ret?.first shouldBe "SELECT public.user_table.id,public.user_table.user_name,public.user_table.password,public.user_table.create_time,public.user_table.last_modify_time,public.user_table.create_partner_id,public.user_table.last_modify_partner_id,public.user_table.create_corp_id,public.user_table.last_modify_corp_id FROM public.user_table WHERE public.user_table.id=:public.user_table.id"
})

class UpdateTest : StringSpec({
    var model = UserModel()
    var totalFields=model.getModelFields(null as KClass<ModelBase>)
    var fields = totalFields.toArray()
    var uField= dynamic.model.query.mq.FieldValue(fields!!.first(), 1)
    var upd= update(uField,setModel = model).where(eq(fields!!.get(2),"password123"))
    var ret=upd.render(null)
    ret?.first shouldBe "UPDATE public.user_table SET public.user_table.id = :public.user_table.id WHERE public.user_table.password=:public.user_table.password"
})

open class K1(open val m1:Any?)
class K2(override var m1:ArrayList<Long>):K1(m1)

