

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

