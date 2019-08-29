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
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Model

@Model("partnerAppShortcut","快捷应用")
class BasePartnerAppShortcut(tableName:String,schemaName:String):ContextModel(tableName,schemaName) {
    companion object: RefSingleton<BasePartnerAppShortcut> {
        override lateinit var ref: BasePartnerAppShortcut
    }
    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val index = ModelField(null,"shortcut_index",FieldType.INT,"次序",defaultValue = 0)

    val partner= ModelMany2OneField(null,
            "partner_id",
            FieldType.BIGINT,
            "公司人员",
             "public.base_partner",
            "id",
            FieldForeignKey(action = ForeignKeyAction.CASCADE)
            )
    var app = ModelOne2OneField(null,"app_id",FieldType.BIGINT,
            "app",targetModelTable = "public.base_app",targetModelFieldName = "id",foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))


    constructor():this("base_partner_app_shortcut","public")

    fun getPartnerApps(partnerID:Long):ArrayList<String>{
        var apps= arrayListOf<String>()
        var dataObjectArray=this.rawRead(*this.fields.getAllPersistFields().values.toTypedArray(),
                criteria = eq(this.partner,partnerID),
                orderBy = OrderBy(OrderBy.OrderField(this.index)))
        dataObjectArray?.data?.forEach {
            var mobj=it.getValue(BasePartnerAppShortcut.ref.app) as ModelDataObject?
            if(mobj!=null){
                var name=mobj.data.getValue(BaseApp.ref.name) as String?
                name?.let {
                    apps.add(name)
                }
            }
        }
        return apps
    }

    override fun addCreateModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
    override fun addEditModelLog(modelDataObject: ModelDataObject, useAccessControl: Boolean, pc: PartnerCache?) {

    }
}