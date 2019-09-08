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

import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.eq
import work.bg.server.core.cache.PartnerCache
import dynamic.model.web.spring.boot.annotation.Model

@Model("partnerAppShortcut","快捷应用")
class BasePartnerAppShortcut(tableName:String,schemaName:String):ContextModel(tableName,schemaName) {
    companion object: RefSingleton<BasePartnerAppShortcut> {
        override lateinit var ref: BasePartnerAppShortcut
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val index = dynamic.model.query.mq.ModelField(null, "shortcut_index", dynamic.model.query.mq.FieldType.INT, "次序", defaultValue = 0)

    val partner= dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "公司人员",
            "public.base_partner",
            "id",
            dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE)
    )
    var app = dynamic.model.query.mq.ModelOne2OneField(null, "app_id", dynamic.model.query.mq.FieldType.BIGINT,
            "app", targetModelTable = "public.base_app", targetModelFieldName = "id", foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))


    constructor():this("base_partner_app_shortcut","public")

    fun getPartnerApps(partnerID:Long):ArrayList<String>{
        var apps= arrayListOf<String>()
        var dataObjectArray=this.rawRead(*this.fields.getAllPersistFields().values.toTypedArray(),
                criteria = eq(this.partner,partnerID),
                orderBy = dynamic.model.query.mq.OrderBy(dynamic.model.query.mq.OrderBy.OrderField(this.index)))
        dataObjectArray?.data?.forEach {
            var mobj=it.getValue(BasePartnerAppShortcut.ref.app) as dynamic.model.query.mq.ModelDataObject?
            if(mobj!=null){
                var name=mobj.data.getValue(BaseApp.ref.name) as String?
                name?.let {
                    apps.add(name)
                }
            }
        }
        return apps
    }

    override fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
    override fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}