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
import dynamic.model.query.mq.model.AppModel
import work.bg.server.core.acrule.inspector.*
import work.bg.server.core.model.billboard.CurrCorpBillboard
import dynamic.model.web.spring.boot.annotation.Model


@Model("partnerRole", "角色")
class BasePartnerRole(table:String,schema:String):ContextModel(table,schema) {
    companion object : RefSingleton<BasePartnerRole> {
        override lateinit var ref: BasePartnerRole
    }

    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())

    val name= dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            "名称")



    val corp= dynamic.model.query.mq.ModelMany2OneField(null,
            "corp_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "公司",
            targetModelTable = "public.base_corp",
            targetModelFieldName = "id",
            defaultValue = CurrCorpBillboard(),
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val partners= dynamic.model.query.mq.ModelMany2ManyField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "用户",
            "public.base_corp_partner_rel",
            "partner_id",
            "public.base_partner",
            "id")

    val accessControlRule = dynamic.model.query.mq.ModelField(null,
            "ac_rule",
            dynamic.model.query.mq.FieldType.STRING,
            "权限配置",
            defaultValue = "")

    val isSuper= dynamic.model.query.mq.ModelField(null,
            "is_super",
            dynamic.model.query.mq.FieldType.INT,
            "管理员",
            defaultValue = 0)

    val apps= dynamic.model.query.mq.ModelOne2ManyField(null, "m_partner_role_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            title = "应用",
            targetModelTable = "public.base_app",
            targetModelFieldName = "partner_role_id")

    constructor():this("base_partner_role","public")

    fun getInstallApps(id:Long):List<String>{
        val apps = mutableListOf<String>()
        var d = this.rawRead(model=this,criteria = eq(this.id,id),attachedFields = arrayOf(dynamic.model.query.mq.AttachedField(this.apps)))?.firstOrNull()
        d?.let {
            (it.getFieldValue(this.isSuper) as Int?)?.let {isSuper->
                if(isSuper>0){
                    return AppModel.ref.appPackageManifests.keys.toList()
                }
            }
            val pApps = it.getFieldValue(this.apps) as dynamic.model.query.mq.ModelDataArray?
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