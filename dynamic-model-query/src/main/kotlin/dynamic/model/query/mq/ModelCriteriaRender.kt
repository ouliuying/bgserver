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

package dynamic.model.query.mq

import dynamic.model.query.mq.aggregation.AggExpression
import dynamic.model.query.mq.aggregation.CountExpression
import dynamic.model.query.mq.condition.constant.BooleanExpression
import dynamic.model.query.mq.condition.constant.StringExpression
import dynamic.model.query.mq.join.JoinModel
import dynamic.model.query.mq.logical.AndExpression
import dynamic.model.query.mq.logical.OrExpression

class ModelCriteriaRender: dynamic.model.query.mq.ModelExpressionVisitor {

     internal object  GRAMMARKEYS
     {
         const val  SELECT = "SELECT "
         const val SELECT_ALL = "SELECT * "
         const val FROM = " FROM "
         const val FIELD_COMMA = ","
         const val OFFSET = " OFFSET "
         const val LIMIT = " LIMIT "
         const val BRACKET_PREFIX = " ("
         const val BRACKET_SUFFIX = ") "
         const val ON = " ON "
         const val GROUP_BY = " GROUP BY "
         const val HAVING = " HAVING "
         const val ORDER_BY = " ORDER BY "
         const val ORDER_BY_ASC = " ASC "
         const val ORDER_BY_DESC = " DESC "
         const val EXISTS = " EXISTS "
         const val NOT_EXISTS = " NOT EXISTS "
         const val IN = " IN "
         const val NOT_IN = " NOT IN "
         const val LIKE = " LIKE "
         const val NOT_LIKE = " NOT LIKE "
         const val IS = " IS "
         const val IS_NOT = " IS NOT "
         const val AND = " AND "
         const val OR = " OR "
         const val ALL = "*"
         const val UPDATE = "UPDATE "
         const val SET = " SET "
         const val WHERE = " WHERE "
         const val ASSIGN = " = "
         const val INSERT = "INSERT "
         const val INTO = " INTO "
         const val VALUES = " VALUES"
         const val DELETE = "DELETE "
         const val COUNT = " COUNT"
         const val AVG = "AVG"
         const val MIN = "MIN"
         const val MAX = "MAX"
         const val SUM = "SUM"
         const val ASTERISK = " * "
         const val CREATE_RETURN_ID=" RETURNING id"
    }
    var tableColumnNameGenerator: dynamic.model.query.mq.ModelTableColumnNameGenerator = dynamic.model.query.mq.ModelTableColumnAliasNameGenerator()
    var namedSql:StringBuilder = StringBuilder()
    var namedParameters:MutableMap<String, dynamic.model.query.mq.FieldValue> = mutableMapOf()
    init {

    }
    override fun visit(expression: dynamic.model.query.mq.ModelExpression?, parent: dynamic.model.query.mq.ModelExpression?) =//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            when (expression) {
                is dynamic.model.query.mq.SelectStatement -> {
                    this.buildSelect(expression as dynamic.model.query.mq.SelectStatement,parent)
                }
                is dynamic.model.query.mq.UpdateStatement ->{
                    this.buildUpdate(expression,parent)
                }
                is dynamic.model.query.mq.CreateStatement ->{
                    this.buildCreate(expression,parent)
                }
                is dynamic.model.query.mq.DeleteStatement ->{
                    this.buildDelete(expression,parent)
                }
                is dynamic.model.query.mq.join.JoinModel -> {
                    var jm=expression as dynamic.model.query.mq.join.JoinModel
                    this.buildJoinModel(jm,parent)
                }
                is dynamic.model.query.mq.FieldBase -> {
                    this.buildField(expression,parent)
                }
                is dynamic.model.query.mq.GroupBy ->{
                    this.buildGroupBy(expression,parent)
                }
                is dynamic.model.query.mq.OrderBy ->{
                    this.buildOrderBy(expression,parent)
                }
                is dynamic.model.query.mq.OrderBy.OrderField ->{
                    this.buildOrderByField(expression,parent)
                }
                is dynamic.model.query.mq.condition.CheckValueExpression ->{
                    this.buildCheckValueExpression(expression,parent)
                }
                is dynamic.model.query.mq.condition.ExistsExpression,is dynamic.model.query.mq.condition.NotExistsExpression ->{
                    this.buildExistsExpression(expression,parent)
                }
                is dynamic.model.query.mq.condition.NotInExpression,is dynamic.model.query.mq.condition.InExpression ->{
                    this.buildInExpression(expression,parent)
                }
                is dynamic.model.query.mq.condition.NotLikeExpression,is dynamic.model.query.mq.condition.LikeExpression ->{
                    this.buildLikeExpression(expression,parent)
                }
                is dynamic.model.query.mq.condition.IsExpression,is dynamic.model.query.mq.condition.IsNotExpression ->{
                    this.buildIsExpression(expression,parent)
                }
                is AndExpression ->{
                    this.buildAndExpression(expression,parent)
                }
                is dynamic.model.query.mq.condition.constant.BooleanExpression ->{
                    this.buildBooleanExpression(expression,parent)
                }
                is dynamic.model.query.mq.condition.constant.StringExpression ->{
                    this.buildStringExpression(expression,parent)
                }
                is OrExpression ->{
                    this.buildOrExpression(expression,parent)
                }
                is dynamic.model.query.mq.aggregation.AggExpression ->{
                    this.buildAggExpression(expression,parent)
                }
                else -> {

                }
            }
    private fun buildBooleanExpression(expression: dynamic.model.query.mq.condition.constant.BooleanExpression?,
                                       parent: dynamic.model.query.mq.ModelExpression?){
     if(expression!=null && expression.value) {
        this.namedSql.append(" true ")
     }
     else if(expression!=null){
        this.namedSql.append(" false ")
     }
    }
    private fun  escapeSqlInject(value:String):String{
        return dynamic.model.query.mq.SqlUtil.Companion.escapeSqlString(value)
    }

    private fun buildStringExpression(expression: dynamic.model.query.mq.condition.constant.StringExpression?,
                                      parent: dynamic.model.query.mq.ModelExpression?){
        if(expression!=null && expression.value!=""){
            this.namedSql.append("\'")
            this.namedSql.append(this.escapeSqlInject(expression.value))
            this.namedSql.append("\'")
        }
    }

    // expression?.accept(this)
    private fun buildAggExpression(expression: dynamic.model.query.mq.aggregation.AggExpression?, parent: dynamic.model.query.mq.ModelExpression?){
        if(expression !is dynamic.model.query.mq.aggregation.CountExpression){
            if (expression?.fields?.size==1){
                var columnName=this.tableColumnNameGenerator.generateColumnName(expression?.fields?.first())
                this.namedSql.append(expression?.aggName);
                this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
                this.namedSql.append(columnName)
                this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
            }
        }
        else{
            this.namedSql.append(expression?.aggName);
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
            if (expression?.fields?.size<1){
                this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ALL)
            }else{
                expression?.fields?.forEach {
                    var columnName=this.tableColumnNameGenerator.generateColumnName(it)
                    this.namedSql.append(columnName)
                    this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
                }
            }
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
        }
    }
    private  fun buildAndExpression(andExpression:AndExpression,parent: dynamic.model.query.mq.ModelExpression?){
        var hasClosure=false
        if(parent is AndExpression || parent is OrExpression){
            hasClosure=true
        }
        if(hasClosure){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX);
        }

        andExpression.subExpressions?.forEach{
            this.accept(it,andExpression)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.AND)
        }
        this.namedSql=StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.AND))

        if(hasClosure){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX);
        }
    }
    private  fun buildOrExpression(orExpression:OrExpression,parent: dynamic.model.query.mq.ModelExpression?){
        var hasClosure=false
        if(parent is AndExpression || parent is OrExpression){
            hasClosure=true
        }
        if(hasClosure){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX);
        }

        orExpression.subExpressions?.forEach{
            this.accept(it,orExpression)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.OR)
        }
        this.namedSql=StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.OR))

        if(hasClosure){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
        }
    }
    private fun buildIsExpression(expression: dynamic.model.query.mq.ModelExpression?, parent: dynamic.model.query.mq.ModelExpression?){
        if (expression is dynamic.model.query.mq.condition.IsExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.IsExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.IS)
            var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[columnName]= dynamic.model.query.mq.FieldValue((expression as dynamic.model.query.mq.condition.IsExpression).field, "%" + (expression as dynamic.model.query.mq.condition.IsExpression).value + "%")
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.IsNotExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.IS_NOT)
            var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[columnName]= dynamic.model.query.mq.FieldValue((expression as dynamic.model.query.mq.condition.IsNotExpression).field, "%" + (expression as dynamic.model.query.mq.condition.IsNotExpression).value + "%")
        }
    }
    private  fun buildLikeExpression(expression: dynamic.model.query.mq.ModelExpression?, parent: dynamic.model.query.mq.ModelExpression?){
        if (expression is dynamic.model.query.mq.condition.LikeExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.LikeExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.LIKE)
            var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[columnName]= dynamic.model.query.mq.FieldValue((expression as dynamic.model.query.mq.condition.LikeExpression).field, "%" + (expression as dynamic.model.query.mq.condition.LikeExpression).value + "%")
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.NotLikeExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.NOT_LIKE)
            var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[columnName]= dynamic.model.query.mq.FieldValue((expression as dynamic.model.query.mq.condition.NotLikeExpression).field, "%" + (expression as dynamic.model.query.mq.condition.NotLikeExpression).value + "%")
        }
    }
    private  fun buildInExpression(expression: dynamic.model.query.mq.ModelExpression?, parent: dynamic.model.query.mq.ModelExpression?){
        if (expression is dynamic.model.query.mq.condition.InExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.InExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.IN)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
            if((expression as dynamic.model.query.mq.condition.InExpression).valueSet!=null)
            {
                var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
                this.namedSql.append(namedColumnName)
                this.namedParameters[columnName]= dynamic.model.query.mq.FieldValue((expression as dynamic.model.query.mq.condition.InExpression).field, (expression as dynamic.model.query.mq.condition.InExpression).valueSet)
            }
            else{
                this.accept((expression as dynamic.model.query.mq.condition.InExpression).criteria,expression)
            }
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.NotInExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.NOT_IN)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
            if((expression as dynamic.model.query.mq.condition.NotInExpression).valueSet!=null)
            {
                var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
                this.namedSql.append(namedColumnName)
                this.namedParameters[columnName]= dynamic.model.query.mq.FieldValue((expression as dynamic.model.query.mq.condition.NotInExpression).field, (expression as dynamic.model.query.mq.condition.NotInExpression).valueSet)
            }
            else{
                this.accept((expression as dynamic.model.query.mq.condition.NotInExpression).criteria,expression)
            }
        }
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
    }
    private  fun buildExistsExpression(expression: dynamic.model.query.mq.ModelExpression, parent: dynamic.model.query.mq.ModelExpression?){

        if (expression is dynamic.model.query.mq.condition.ExistsExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.ExistsExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.EXISTS)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
            this.accept((expression as dynamic.model.query.mq.condition.ExistsExpression).criteria,expression)
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as dynamic.model.query.mq.condition.NotExistsExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.NOT_EXISTS)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
            this.accept((expression as dynamic.model.query.mq.condition.NotExistsExpression).criteria,expression)
        }
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
    }
    private fun buildCheckValueExpression(checkValueExpression: dynamic.model.query.mq.condition.CheckValueExpression, parent: dynamic.model.query.mq.ModelExpression?){
        var columnName=this.tableColumnNameGenerator.generateColumnName(checkValueExpression.field)
        this.namedSql.append(columnName)
        this.namedSql.append(checkValueExpression.operator)
        when(checkValueExpression.value){
            is dynamic.model.query.mq.FieldBase ->{
                val vColumnName=this.tableColumnNameGenerator.generateColumnName(checkValueExpression.value as dynamic.model.query.mq.FieldBase)
                this.namedSql.append(vColumnName)
            }
            else->{
                var namedParameter=this.tableColumnNameGenerator.generateNamedParameter(columnName)
                this.namedSql.append(namedParameter)
                this.namedParameters.put(columnName, dynamic.model.query.mq.FieldValue(checkValueExpression.field, checkValueExpression.value))
            }
        }
    }
    private  fun buildOrderByField(orderByField: dynamic.model.query.mq.OrderBy.OrderField, parent: dynamic.model.query.mq.ModelExpression?){
        var columnName=this.tableColumnNameGenerator.generateColumnName(orderByField.field)
        this.namedSql.append(columnName)
        when(orderByField.orderType){
            dynamic.model.query.mq.OrderBy.Companion.OrderType.ASC ->{
                this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ORDER_BY_ASC)
            }
            dynamic.model.query.mq.OrderBy.Companion.OrderType.DESC ->{
                this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ORDER_BY_DESC)
            }
        }
    }
    private  fun buildOrderBy(orderBy: dynamic.model.query.mq.OrderBy, parent: dynamic.model.query.mq.ModelExpression?) {
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ORDER_BY)
        orderBy.fields?.forEach {
            this.accept(it,orderBy)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
        }
        if (orderBy.fields?.size>0){
            this.namedSql=StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA))
        }
    }
    private fun buildGroupBy(groupBy: dynamic.model.query.mq.GroupBy, parent: dynamic.model.query.mq.ModelExpression?) {
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.GROUP_BY)
        groupBy.fields?.forEach {
            accept(it,groupBy)
        }
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.HAVING)
        this.accept(groupBy.havingCriteria,groupBy)
    }
    private fun buildField(field: dynamic.model.query.mq.FieldBase, parent: dynamic.model.query.mq.ModelExpression?){
        var columnName=this.tableColumnNameGenerator.generateColumnName(field)
        this.namedSql.append(columnName)
    }
    private fun buildJoinModel(joinModel: dynamic.model.query.mq.join.JoinModel, parent: dynamic.model.query.mq.ModelExpression?) {
        this.namedSql.append(joinModel.operator);
        var tableName=this.tableColumnNameGenerator.generateTableName(joinModel.model)
        this.namedSql.append(tableName)
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ON)
        joinModel.subExpressions?.forEach {
            this.accept(it,joinModel)
        }
    }
    private fun accept(expression: dynamic.model.query.mq.ModelExpression?, parent: dynamic.model.query.mq.ModelExpression?)
    {
        if(expression!=null && !expression.accept(this,parent)){
            TODO("invoke render method!")
//            var rd = expression.render(parent)
//            if(rd?.second!=null){
//                this.namedParameters.putAll(rd?.second)
//            }
//            if(rd?.first!=null){
//
//                    this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX);
//                    this.namedSql.append(rd?.first);
//                    expression?.subExpressions?.forEach {
//                        this.accept(it,expression)
//                    }
//                    this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX);
//            }
        }
    }
    private  fun buildDelete(delete: dynamic.model.query.mq.DeleteStatement, parent: dynamic.model.query.mq.ModelExpression?){
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.DELETE)
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FROM)
        var tableName=this.tableColumnNameGenerator.generateTableName(delete.fromModel)
        this.namedSql.append(tableName)
        if (delete.whereExpression!=null){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.WHERE)
        }
        this.accept(delete.whereExpression,delete)
    }
    private fun buildCreate(create: dynamic.model.query.mq.CreateStatement, parent: dynamic.model.query.mq.ModelExpression?){
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.INSERT)
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.INTO)
        var tableName=this.tableColumnNameGenerator.generateTableName(create.model)
        this.namedSql.append(tableName)
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX.trim())
        var values= mutableListOf<String>()
        create.fieldValues.forEach {
            var columnName=this.tableColumnNameGenerator.generateColumnName(it.field,true)
            var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
            this.namedParameters[columnName]=it
            values.add(namedColumnName)
        }
        this.namedSql = StringBuilder(this.namedSql.removeSuffix(","))
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
        this.namedSql=java.lang.StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA))
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.VALUES)
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX.trim())
        this.namedSql.append(values.joinToString(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA.trim()))
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX.trim())
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.CREATE_RETURN_ID)
    }
    private  fun buildUpdate(update: dynamic.model.query.mq.UpdateStatement, parent: dynamic.model.query.mq.ModelExpression?){
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.UPDATE)
        var tableName=this.tableColumnNameGenerator.generateTableName(update.setModel)
        this.namedSql.append(tableName)
        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.SET)
        update.fieldValues.forEach {
            var columnName=this.tableColumnNameGenerator.generateColumnName(it.field,true)
            var namedColumnName=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(columnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ASSIGN)
            this.namedSql.append(namedColumnName)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
            this.namedParameters[columnName]=it
        }
        this.namedSql=StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA))
        if(update.whereExpression!=null){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.WHERE)
        }
        this.accept(update.whereExpression,update)

    }
    private fun buildSelect(select: dynamic.model.query.mq.SelectStatement, parent: dynamic.model.query.mq.ModelExpression?){

//        var hasClosure=select.hasClosure(parent)
//        if (hasClosure){
//            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
//        }
        if(select.selectFields!=null && select.selectFields.count()>0){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.SELECT)
        }
        else if(select.countExpression!=null||select.maxExpressions!=null
                ||select.minExpressions!=null||select.avgExpressions!=null
                ||select.sumExpressions!=null){
                this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.SELECT)
                if (select.countExpression!=null){
                    this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.COUNT)
                    this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
                    if(select.countExpression!!.field!=null){
                        this.namedSql.append(select.countExpression!!.field!!.name)
                    }
                    else{
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.ASTERISK)
                    }
                    this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
                    this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
                }

                select.maxExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.MAX)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
                    }
                }
                select.minExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.MIN)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
                    }
                }
                select.avgExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.AVG)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
                    }
                }
                select.sumExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.SUM)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
                    }
                }
            this.namedSql=StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA))
        }
        else
        {
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.SELECT_ALL)
        }



        select.selectFields?.forEach {
            this.accept(it,select)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA)
        }

        if (select.selectFields?.size>0){
            this.namedSql=StringBuilder(this.namedSql.removeSuffix(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FIELD_COMMA))
        }

        if(select.fromModel!=null){
            var tableName=this.tableColumnNameGenerator.generateTableName(select.fromModel)
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.FROM)
            this.namedSql.append(tableName)
        }

        select.joinModels?.forEach{
           // this.visit(it,select)
            this.accept(it,select)
        }
        if(select.expression!=null){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.WHERE)
        }
        this.accept(select.expression,select)

        this.accept(select.groupBy,select)

        this.accept(select.orderBy,select)

        if(select.offset!=null){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.OFFSET)
            this.namedSql.append(select.offset!!)
        }
        if(select.limit!=null){
            this.namedSql.append(dynamic.model.query.mq.ModelCriteriaRender.GRAMMARKEYS.LIMIT)
            this.namedSql.append(select.limit!!)
        }
//        if(hasClosure){
//            this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX);
//        }
    }
}