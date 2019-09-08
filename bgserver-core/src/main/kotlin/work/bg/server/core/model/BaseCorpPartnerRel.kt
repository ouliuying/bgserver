

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

import dynamic.model.query.mq.FieldCollection
import dynamic.model.query.mq.ModelField
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.model.ModelBase
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldMustCoexist
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.billboard.CurrCorpBillboard
import work.bg.server.core.model.billboard.CurrPartnerBillboard
import dynamic.model.web.spring.boot.annotation.Model
import kotlin.reflect.KClass

@Model("corpPartnerRel")
class BaseCorpPartnerRel(table:String,schema:String):ContextModel(table,schema) {
    companion object : RefSingleton<BaseCorpPartnerRel> {
        override lateinit var ref: BaseCorpPartnerRel
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标识",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val corp= dynamic.model.query.mq.ModelMany2OneField(null,
            "corp_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "公司",
            "public.base_corp",
            "id",
            defaultValue = CurrCorpBillboard(),
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))
    val partner= dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "用户",
            "public.base_partner",
            "id",
            defaultValue = CurrPartnerBillboard(),
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))
    val partnerRole= dynamic.model.query.mq.ModelMany2OneField(null,
            "partner_role_id",
            dynamic.model.query.mq.FieldType.BIGINT, "角色",
            "public.base_partner_role",
            "id",
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))
    val isDefaultCorp= dynamic.model.query.mq.ModelField(null,
            "is_default_corp",
            dynamic.model.query.mq.FieldType.INT,
            " 默认公司",
            null,
            0)
    constructor():this("base_corp_partner_rel","public")



    override fun <T : ModelBase> getModelFields(overrideBaseCls: KClass<T>?): FieldCollection {
        return super.getModelFields(BaseCorpPartnerRel::class)
    }

    override fun corpIsolationFields(): Array<ModelField>? {
        return null
    }

    override fun isAssociative(): Boolean {
        return true
    }


    override fun maybeCheckACRule(): Boolean {
        return false
    }

    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldUnique(corp,partner,advice = "用戶角色必須唯一",
                isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL))
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldUnique(corp,partner,advice = "用戶角色必須唯一",
                isolationType = ModelFieldUnique.IsolationType.IN_GLOBAL))
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldRequired(this.corp,this.partner,this.partnerRole,advice = "缺少必要的字段"),
                ModelFieldMustCoexist(partnerRole,isDefaultCorp,advice = "必須选择用户角色"))
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(ModelFieldRequired(this.corp,this.partner,this.partnerRole,advice = "缺少必要的字段"),
                ModelFieldMustCoexist(partnerRole,isDefaultCorp,advice = "必須选择用户角色"))
    }
}