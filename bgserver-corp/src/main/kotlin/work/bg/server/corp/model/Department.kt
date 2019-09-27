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

package work.bg.server.corp.model

import com.google.gson.JsonObject
import dynamic.model.query.mq.*
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import work.bg.server.core.acrule.inspector.ModelFieldNotNullOrEmpty
import work.bg.server.core.acrule.inspector.ModelFieldRequired
import work.bg.server.core.acrule.inspector.ModelFieldUnique
import work.bg.server.core.model.ContextModel
import work.bg.server.core.model.billboard.CurrCorpBillboard
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.context.JsonClauseResolver
import work.bg.server.core.ui.ModelView

@Model("department", "部门")
class Department:ContextModel("corp_department",
        "public") {
    companion object : RefSingleton<Department> {
        override lateinit var ref: Department
    }
    val id= dynamic.model.query.mq.ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = dynamic.model.query.mq.FieldPrimaryKey())
    val name= dynamic.model.query.mq.ModelField(null,
            "name",
            dynamic.model.query.mq.FieldType.STRING,
            "名称")
    val comment = dynamic.model.query.mq.ModelField(null,
            "comment",
            dynamic.model.query.mq.FieldType.TEXT,
            "注释")
    val corp = dynamic.model.query.mq.ModelMany2OneField(null,
            "corp_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            null,
            targetModelTable = "public.base_corp",
            targetModelFieldName = "id",
            defaultValue = CurrCorpBillboard(),
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val partners= dynamic.model.query.mq.ModelMany2ManyField(null,
            "partner_id",
            dynamic.model.query.mq.FieldType.BIGINT, "员工",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id",
            relationModelTable = "public.department_partner_rel",
            relationModelFieldName = "partner_id")

    val parent= dynamic.model.query.mq.ModelMany2OneField(null, "parent_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "上级部门",
            targetModelTable = "public.corp_department",
            targetModelFieldName = "id",
            defaultValue = null,
            foreignKey = dynamic.model.query.mq.FieldForeignKey(action = dynamic.model.query.mq.ForeignKeyAction.CASCADE))

    val children = dynamic.model.query.mq.ModelOne2ManyField(null,
            "m_parent_id",
            dynamic.model.query.mq.FieldType.BIGINT,
            title = "下级部门",
            targetModelTable = "public.corp_department",
            targetModelFieldName = "parent_id")


    override fun getModelCreateFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.parent,this.name,advice = "同级部门不能重名",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInStoreInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldUnique(this.parent,this.name,advice = "同级部门不能重名",isolationType = ModelFieldUnique.IsolationType.IN_CORP)
        )
    }

    override fun getModelEditFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "必须输入部门名称"),
                ModelFieldNotNullOrEmpty(this.name,advice = "部门名称不能为空")
        )
    }

    override fun getModelCreateFieldsInspectors(): Array<ModelFieldInspector>? {
        return arrayOf(
                ModelFieldRequired(this.name,advice = "必须输入部门名称"),
                ModelFieldNotNullOrEmpty(this.name,advice = "部门名称不能为空")
        )
    }

    override fun loadListModelViewData(mv: ModelView, viewData: MutableMap<String, Any>, pc: PartnerCache, ownerFieldValue: FieldValue?, toField: FieldBase?, ownerModelID: Long?, reqData: JsonObject?): ModelDataArray? {
        if(ownerFieldValue!=null){
            if(ownerFieldValue.value== dynamic.model.query.mq.Undefined && ownerModelID==null){
                return null
            }
        }
        val fields = this.getModelViewFields(mv).toTypedArray()
        val pageIndex = reqData?.get("pageIndex")?.asInt?:1
        val pageSize = reqData?.get("pageSize")?.asInt?:10
        val jCriteria = reqData?.get("criteria")?.asJsonObject
        //TODO parse javascript criteria

        var criteria = null as dynamic.model.query.mq.ModelExpression?
        jCriteria?.let {
            criteria = JsonClauseResolver(it,this,pc.modelExpressionContext).criteria()
        }
        val (ret,ownerCriteria) = this.getCriteriaByOwnerModelParam(ownerFieldValue,toField,ownerModelID)
        if(ret){
            criteria = if(criteria!=null && ownerCriteria!=null) and(ownerCriteria,criteria!!) else if(criteria!=null) criteria  else ownerCriteria
        }
        var data = this.acRead(*fields,partnerCache = pc,pageIndex = 0,pageSize =null,criteria = criteria)
        var totalCount = this.acCount(criteria = criteria,partnerCache = pc)
        viewData["totalCount"]=totalCount
        return this.toClientModelData(data,arrayListOf(*fields.filter {_f->
            _f is dynamic.model.query.mq.ModelMany2ManyField
        }.toTypedArray())) as dynamic.model.query.mq.ModelDataArray?
    }
}