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
import work.bg.server.core.config.AppPackageManifest
import work.bg.server.core.mq.*
import work.bg.server.core.mq.billboard.CurrCorpBillboard
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.AppModel

@Model("partnerRole", "角色")
class BasePartnerRole(table:String,schema:String):ContextModel(table,schema) {
    companion object :RefSingleton<BasePartnerRole>{
        override lateinit var ref: BasePartnerRole
    }

    val id=ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())
    val name=ModelField(null,
            "name",
            FieldType.STRING,
            "名称")
    val corp=ModelMany2OneField(null,
            "corp_id",
             FieldType.BIGINT,
            "公司",
            targetModelTable = "public.base_corp",
            targetModelFieldName = "id",
            defaultValue = CurrCorpBillboard(),
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val partners=ModelMany2ManyField(null,
            "partner_id",
             FieldType.BIGINT,
            "用户",
            "public.base_corp_partner_rel",
            "partner_id",
            "public.base_partner",
            "id")

    val accessControlRule = ModelField(null,"ac_rule",FieldType.STRING,"权限配置",defaultValue = "")

    val isSuper=ModelField(null,
            "is_super",
            FieldType.INT,
            "管理员",
            defaultValue = 0)

    val apps=ModelOne2ManyField(null,"m_partner_role_id",
            FieldType.BIGINT,
            title = "app",
            targetModelTable = "public.base_app",
            targetModelFieldName = "partner_role_id")

    constructor():this("base_partner_role","public")

    fun getInstallApps(id:Long):List<String>{
        val apps = mutableListOf<String>()
        var d = this.rawRead(model=this,criteria = eq(this.id,id),attachedFields = arrayOf(AttachedField(this.apps)))?.firstOrNull()
        d?.let {
            (it.getFieldValue(this.isSuper) as Int?)?.let {isSuper->
                if(isSuper>0){
                    return AppModel.ref.appPackageManifests.keys.toList()
                }
            }
            val pApps = it.getFieldValue(this.apps) as ModelDataArray?
            pApps?.let {mda->
                mda.toModelDataObjectArray().forEach {
                    mdo->
                    val name = mdo.getFieldValue(BasePartnerRole.ref.name) as String?
                    name?.let {
                       val pm= AppModel.ref.appPackageManifests[name]
                        pm?.let {appm->
                            apps.add(pm.name)
                        }
                    }
                }
            }
        }
        return apps
    }
}