

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

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class UserModel : ModelBase("user_table") {


    val id=ModelField(null, "id",  FieldType.INT, "标示")
    val userName=ModelField(null, "user_name", FieldType.STRING, "用户名")
    val password=ModelField(null, "password",  FieldType.STRING, "用户密码")
}

class SelectTest : StringSpec({
    // tests here
    var model = UserModel()
    var fields = model.fields?.toArray()
    var sel = select(*fields!!, fromModel = model).where(eq(fields!!.first(), 1)!!)
    var ret = sel.render(null)
    ret?.first shouldBe "SELECT public.user_table.id,public.user_table.user_name,public.user_table.password,public.user_table.create_time,public.user_table.last_modify_time,public.user_table.create_partner_id,public.user_table.last_modify_partner_id,public.user_table.create_corp_id,public.user_table.last_modify_corp_id FROM public.user_table WHERE public.user_table.id=:public.user_table.id"
})

class UpdateTest : StringSpec({
    var model = UserModel()
    var fields = model.fields?.toArray()
    var uField=FieldValue(fields!!.first(),1)
    var upd= update(uField,setModel = model).where(eq(fields!!.get(2),"password123"))
    var ret=upd.render(null)
    ret?.first shouldBe "UPDATE public.user_table SET public.user_table.id = :public.user_table.id WHERE public.user_table.password=:public.user_table.password"
})

