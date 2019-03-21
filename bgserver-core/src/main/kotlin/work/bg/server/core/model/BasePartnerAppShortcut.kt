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

package work.bg.server.core.model

import work.bg.server.core.RefSingleton
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
                var name=mobj?.data?.getValue(BaseApp.ref.name) as String
                apps.add(name)
            }
        }
        return apps
    }
}