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
import work.bg.server.core.acrule.inspector.*
import work.bg.server.core.mq.*
import work.bg.server.core.model.billboard.CurrCorpBillboard
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

    val accessControlRule = ModelField(null,
            "ac_rule",
            FieldType.STRING,
            "权限配置",
            defaultValue = "")

    val isSuper=ModelField(null,
            "is_super",
            FieldType.INT,
            "管理员",
            defaultValue = 0)

    val apps=ModelOne2ManyField(null,"m_partner_role_id",
            FieldType.BIGINT,
            title = "应用",
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

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "名称必须设置"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空")
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "名称必须设置"),
                ModelFieldNotNullOrEmpty(this.name,advice = "名称不能为空")
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "名称必须唯一",isolationType=ModelFieldUnique.IsolationType.IN_CORP)
        )
    }
    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.name,advice = "名称必须唯一",isolationType=ModelFieldUnique.IsolationType.IN_CORP)
        )
    }
}