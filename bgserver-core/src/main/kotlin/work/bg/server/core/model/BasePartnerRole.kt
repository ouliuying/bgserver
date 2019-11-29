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

import dynamic.model.query.mq.*
import dynamic.model.query.mq.model.AppModel
import work.bg.server.core.acrule.inspector.*
import work.bg.server.core.model.billboard.CurrCorpBillboard
import dynamic.model.web.spring.boot.annotation.Model


@Model("partnerRole", "角色")
class BasePartnerRole(table:String,schema:String):ContextModel(table,schema) {
    companion object : RefSingleton<BasePartnerRole> {
        override lateinit var ref: BasePartnerRole
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val name= ModelField(null,
            "name",
            FieldType.STRING,
            "名称")



    val corp= ModelMany2OneField(null,
            "corp_id",
            FieldType.BIGINT,
            "公司",
            targetModelTable = "public.base_corp",
            targetModelFieldName = "id",
            defaultValue = CurrCorpBillboard(),
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val partners= ModelMany2ManyField(null,
            "partner_id",
            FieldType.BIGINT,
            "用户",
            "public.base_corp_partner_rel",
            "partner_id",
            "public.base_partner",
            "id")

//    val modelRule = ModelOne2ManyField(null,
//            "model_rule",
//            FieldType.BIGINT,
//            "模型权限配置",
//            targetModelTable = "public.base_partner_role_model_rule",
//            targetModelFieldName = "partner_role_id")

    val viewRule = ModelOne2ManyField(null,
            "view_rule",
            FieldType.BIGINT,
            "视图权限配置",
            targetModelTable = "public.base_partner_role_model_view_rule",
            targetModelFieldName = "partner_role_id")

    val menuRule = ModelOne2ManyField(null,
            "menu_rule",
            FieldType.BIGINT,
            "菜单权限配置",
            targetModelTable = "public.base_partner_role_app_menu_rule",
            targetModelFieldName = "partner_role_id")



    val isSuper= ModelField(null,
            "is_super",
            FieldType.INT,
            "管理员",
            defaultValue = 0)

    val apps= ModelOne2ManyField(null, "m_partner_role_id",
            FieldType.BIGINT,
            title = "应用",
            targetModelTable = "public.base_app",
            targetModelFieldName = "partner_role_id")

    constructor():this("base_partner_role","public")

    fun getInstallApps(id:Long):List<BaseApp.BaseAppData>{
        val apps = mutableListOf<BaseApp.BaseAppData>()
        var d = this.rawRead(model=this,criteria = eq(this.id,id),attachedFields = arrayOf(AttachedField(this.apps)))?.firstOrNull()
        d?.let {
            (it.getFieldValue(this.isSuper) as Int?)?.let {isSuper->
                if(isSuper>0){
                     AppModel.ref.appPackageManifests.forEach { (t, u) ->
                        apps.add(BaseApp.BaseAppData(
                                u.name,
                                u.modelUrl
                        ))
                    }
                    return apps
                }
            }
            val pApps = it.getFieldValue(this.apps) as ModelDataArray?
            pApps?.let {mda->
                mda.toModelDataObjectArray().forEach {
                    mdo->
                    val name = mdo.getFieldValue(BaseApp.ref.name) as String?
                    val modelUrl = mdo.getFieldValue(BaseApp.ref.modelUrl) as String?
                    name?.let {
                       val pm= AppModel.ref.appPackageManifests[name]
                        pm?.let {appm->
                            apps.add(BaseApp.BaseAppData(
                                    appm.name,
                                    modelUrl?:""
                            ))
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