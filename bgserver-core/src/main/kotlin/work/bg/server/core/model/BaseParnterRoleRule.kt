/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.core.model

import dynamic.model.query.mq.*
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldUnique

@Model("partnerRoleRule",title = "角色规则")
class BasePartnerRoleRule:ContextModel("base_partner_role_rule","public") {

    companion object: RefSingleton<BasePartnerRoleRule> {
        override lateinit var ref: BasePartnerRoleRule
    }

    val id= ModelField(null,
            "id",
            FieldType.BIGINT,
            "标识",
            primaryKey = FieldPrimaryKey())

    val app = ModelField(null,
            "app",
            FieldType.STRING,
            "名称")

    val model = ModelField(null,
            "model",
            FieldType.STRING,
            "名称")

    val actionType = ModelField(null,
            "action_typ",
            FieldType.INT,
            title = "操作类型")

    val partnerRole = ModelMany2OneField(null,
            "partner_role_id",
            FieldType.BIGINT,
            "名称",
            targetModelTable = "public.base_partner_role",
            targetModelFieldName = "id",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE))

    val modelRule = ModelField(null,
            "model_rule",FieldType.STRING,
            title = "模型规则")

    val viewRule = ModelField(null,
            "view_rule",
            fieldType = FieldType.STRING,
            title = "视图规则")

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.actionType,
                        this.partnerRole,
                        this.app,
                        this.model,
                        isolationType = ModelFieldUnique.IsolationType.IN_CORP,
                        advice = "在同一角色及操作类型下，应用模型必须唯一")
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.actionType,
                        this.partnerRole,
                        this.app,
                        this.model,
                        isolationType = ModelFieldUnique.IsolationType.IN_CORP,
                        advice = "在同一角色及操作类型下，应用模型必须唯一")
        )
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return super.getModelCreateFieldsInspectors()
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return super.getModelEditFieldsInspectors()
    }


   enum class ModelActionType(typ:Int){
       Create(0),
       Edit(1),
       Read(2),
       Delete(3)
   }
}