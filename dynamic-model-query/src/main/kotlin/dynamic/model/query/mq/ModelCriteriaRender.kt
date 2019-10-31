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
import dynamic.model.query.mq.condition.*
import dynamic.model.query.mq.condition.constant.BooleanExpression
import dynamic.model.query.mq.condition.constant.StringExpression
import dynamic.model.query.mq.join.JoinModel
import dynamic.model.query.mq.logical.AndExpression
import dynamic.model.query.mq.logical.OrExpression

class ModelCriteriaRender: ModelExpressionVisitor {

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
    var tableColumnNameGenerator: ModelTableColumnNameGenerator = ModelTableColumnAliasNameGenerator()
    var namedSql:StringBuilder = StringBuilder()
    var namedParameters:MutableMap<String, FieldValue> = mutableMapOf()
    init {

    }
    override fun visit(expression: ModelExpression?, parent: ModelExpression?) =//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            when (expression) {
                is SelectStatement -> {
                    this.buildSelect(expression as SelectStatement,parent)
                }
                is UpdateStatement ->{
                    this.buildUpdate(expression,parent)
                }
                is CreateStatement ->{
                    this.buildCreate(expression,parent)
                }
                is DeleteStatement ->{
                    this.buildDelete(expression,parent)
                }
                is JoinModel -> {
                    var jm=expression as JoinModel
                    this.buildJoinModel(jm,parent)
                }
                is FieldBase -> {
                    this.buildField(expression,parent)
                }
                is GroupBy ->{
                    this.buildGroupBy(expression,parent)
                }
                is OrderBy ->{
                    this.buildOrderBy(expression,parent)
                }
                is OrderBy.OrderField ->{
                    this.buildOrderByField(expression,parent)
                }
                is CheckValueExpression ->{
                    this.buildCheckValueExpression(expression,parent)
                }
                is ExistsExpression,is NotExistsExpression ->{
                    this.buildExistsExpression(expression,parent)
                }
                is NotInExpression,is InExpression ->{
                    this.buildInExpression(expression,parent)
                }
                is NotLikeExpression,is LikeExpression ->{
                    this.buildLikeExpression(expression,parent)
                }
                is IsExpression,is IsNotExpression ->{
                    this.buildIsExpression(expression,parent)
                }
                is AndExpression ->{
                    this.buildAndExpression(expression,parent)
                }
                is BooleanExpression ->{
                    this.buildBooleanExpression(expression,parent)
                }
                is StringExpression ->{
                    this.buildStringExpression(expression,parent)
                }
                is OrExpression ->{
                    this.buildOrExpression(expression,parent)
                }
                is AggExpression ->{
                    this.buildAggExpression(expression,parent)
                }
                else -> {

                }
            }
    private fun buildBooleanExpression(expression: BooleanExpression?,
                                       parent: ModelExpression?){
     if(expression!=null && expression.value) {
        this.namedSql.append(" true ")
     }
     else if(expression!=null){
        this.namedSql.append(" false ")
     }
    }
    private fun  escapeSqlInject(value:String):String{
        return SqlUtil.escapeSqlString(value)
    }

    private fun buildStringExpression(expression: StringExpression?,
                                      parent: ModelExpression?){
        if(expression!=null && expression.value!=""){
            this.namedSql.append("\'")
            this.namedSql.append(this.escapeSqlInject(expression.value))
            this.namedSql.append("\'")
        }
    }

    // expression?.accept(this)
    private fun buildAggExpression(expression: AggExpression?, parent: ModelExpression?){
        if(expression !is CountExpression){
            if (expression?.fields?.size==1){
                var columnName=this.tableColumnNameGenerator.generateColumnName(expression?.fields?.first())
                this.namedSql.append(expression?.aggName);
                this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
                this.namedSql.append(columnName)
                this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
            }
        }
        else{
            this.namedSql.append(expression?.aggName);
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
            if (expression.fields.isEmpty()){
                this.namedSql.append(GRAMMARKEYS.ALL)
            }else{
                expression.fields.forEach {
                    var columnName=this.tableColumnNameGenerator.generateColumnName(it)
                    this.namedSql.append(columnName)
                    this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
                }
            }
            this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
        }
    }
    private  fun buildAndExpression(andExpression:AndExpression,parent: ModelExpression?){
        var hasClosure=false
        if(parent is AndExpression || parent is OrExpression){
            hasClosure=true
        }
        if(hasClosure){
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX);
        }

        andExpression.subExpressions?.forEach{
            this.accept(it,andExpression)
            this.namedSql.append(GRAMMARKEYS.AND)
        }
        this.namedSql=StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.AND))

        if(hasClosure){
            this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX);
        }
    }
    private  fun buildOrExpression(orExpression:OrExpression,parent: ModelExpression?){
        var hasClosure=false
        if(parent is AndExpression || parent is OrExpression){
            hasClosure=true
        }
        if(hasClosure){
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX);
        }

        orExpression.subExpressions?.forEach{
            this.accept(it,orExpression)
            this.namedSql.append(GRAMMARKEYS.OR)
        }
        this.namedSql=StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.OR))

        if(hasClosure){
            this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
        }
    }
    private fun buildIsExpression(expression: ModelExpression?, parent: ModelExpression?){
        if (expression is IsExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as IsExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.IS)
            var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[ColumnKeyName]= FieldValue(expression.field, expression.value)
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as IsNotExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.IS_NOT)
            var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[ColumnKeyName]= FieldValue(expression.field, expression.value)
        }
    }
    private  fun buildLikeExpression(expression: ModelExpression?, parent: ModelExpression?){
        if (expression is LikeExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as LikeExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.LIKE)
            var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[ColumnKeyName]= FieldValue((expression as LikeExpression).field, "%" + (expression as LikeExpression).value + "%")
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as NotLikeExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.NOT_LIKE)
            var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(namedColumnName)
            this.namedParameters[ColumnKeyName]= FieldValue((expression as NotLikeExpression).field, "%" + (expression as NotLikeExpression).value + "%")
        }
    }
    private  fun buildInExpression(expression: ModelExpression?, parent: ModelExpression?){
        if (expression is InExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as InExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.IN)
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
            if(expression.valueSet!=null)
            {
                var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
                this.namedSql.append(namedColumnName)
                this.namedParameters[ColumnKeyName]= FieldValue(expression.field, expression.valueSet)
            }
            else{
                this.accept((expression as InExpression).criteria,expression)
            }
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as NotInExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.NOT_IN)
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
            if((expression as NotInExpression).valueSet!=null)
            {
                var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
                this.namedSql.append(namedColumnName)
                this.namedParameters[ColumnKeyName]= FieldValue((expression as NotInExpression).field, (expression as NotInExpression).valueSet)
            }
            else{
                this.accept((expression as NotInExpression).criteria,expression)
            }
        }
        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
    }
    private  fun buildExistsExpression(expression: ModelExpression, parent: ModelExpression?){

        if (expression is ExistsExpression){
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as ExistsExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.EXISTS)
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
            this.accept((expression as ExistsExpression).criteria,expression)
        }
        else{
            var columnName=this.tableColumnNameGenerator.generateColumnName((expression as NotExistsExpression).field)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.NOT_EXISTS)
            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
            this.accept((expression as NotExistsExpression).criteria,expression)
        }
        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
    }
    private fun buildCheckValueExpression(checkValueExpression: CheckValueExpression, parent: ModelExpression?){
        var columnName=this.tableColumnNameGenerator.generateColumnName(checkValueExpression.field)
        this.namedSql.append(columnName)
        this.namedSql.append(checkValueExpression.operator)
        when(checkValueExpression.value){
            is FieldBase ->{
                val vColumnName=this.tableColumnNameGenerator.generateColumnName(checkValueExpression.value as FieldBase)
                this.namedSql.append(vColumnName)
            }
            else->{
                var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
                this.namedSql.append(namedColumnName)
                this.namedParameters[ColumnKeyName] = FieldValue(checkValueExpression.field, checkValueExpression.value)
            }
        }
    }
    private  fun buildOrderByField(orderByField: OrderBy.OrderField, parent: ModelExpression?){
        var columnName=this.tableColumnNameGenerator.generateColumnName(orderByField.field)
        this.namedSql.append(columnName)
        when(orderByField.orderType){
            OrderBy.Companion.OrderType.ASC ->{
                this.namedSql.append(GRAMMARKEYS.ORDER_BY_ASC)
            }
            OrderBy.Companion.OrderType.DESC ->{
                this.namedSql.append(GRAMMARKEYS.ORDER_BY_DESC)
            }
        }
    }
    private  fun buildOrderBy(orderBy: OrderBy, parent: ModelExpression?) {
        this.namedSql.append(GRAMMARKEYS.ORDER_BY)
        orderBy.fields?.forEach {
            this.accept(it,orderBy)
            this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
        }
        if (orderBy.fields?.size>0){
            this.namedSql=StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.FIELD_COMMA))
        }
    }
    private fun buildGroupBy(groupBy: GroupBy, parent: ModelExpression?) {
        this.namedSql.append(GRAMMARKEYS.GROUP_BY)
        groupBy.fields?.forEach {
            accept(it,groupBy)
        }
        groupBy.havingCriteria?.let {
            this.namedSql.append(GRAMMARKEYS.HAVING)
            this.accept(groupBy.havingCriteria,groupBy)
        }

    }
    private fun buildField(field: FieldBase, parent: ModelExpression?){
        var columnName=this.tableColumnNameGenerator.generateColumnName(field)
        this.namedSql.append(columnName)
    }
    private fun buildJoinModel(joinModel: JoinModel, parent: ModelExpression?) {
        this.namedSql.append(joinModel.operator);
        var tableName=this.tableColumnNameGenerator.generateTableName(joinModel.model)
        this.namedSql.append(tableName)
        this.namedSql.append(GRAMMARKEYS.ON)
        joinModel.subExpressions?.forEach {
            this.accept(it,joinModel)
        }
    }
    private fun accept(expression: ModelExpression?, parent: ModelExpression?)
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
    private  fun buildDelete(delete: DeleteStatement, parent: ModelExpression?){
        this.namedSql.append(GRAMMARKEYS.DELETE)
        this.namedSql.append(GRAMMARKEYS.FROM)
        var tableName=this.tableColumnNameGenerator.generateTableName(delete.fromModel)
        this.namedSql.append(tableName)
        if (delete.whereExpression!=null){
            this.namedSql.append(GRAMMARKEYS.WHERE)
        }
        this.accept(delete.whereExpression,delete)
    }
    private fun buildCreate(create: CreateStatement, parent: ModelExpression?){
        this.namedSql.append(GRAMMARKEYS.INSERT)
        this.namedSql.append(GRAMMARKEYS.INTO)
        var tableName=this.tableColumnNameGenerator.generateTableName(create.model)
        this.namedSql.append(tableName)
        this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX.trim())
        var values= mutableListOf<String>()
        create.fieldValues.forEach {
            var columnName=this.tableColumnNameGenerator.generateColumnName(it.field,true)
            var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
            this.namedParameters[ColumnKeyName]=it
            values.add(namedColumnName)
        }
        this.namedSql = StringBuilder(this.namedSql.removeSuffix(","))
        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
        this.namedSql=java.lang.StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.FIELD_COMMA))
        this.namedSql.append(GRAMMARKEYS.VALUES)
        this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX.trim())
        this.namedSql.append(values.joinToString(GRAMMARKEYS.FIELD_COMMA.trim()))
        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX.trim())
        this.namedSql.append(GRAMMARKEYS.CREATE_RETURN_ID)
    }
    private  fun buildUpdate(update: UpdateStatement, parent: ModelExpression?){
        this.namedSql.append(GRAMMARKEYS.UPDATE)
        var tableName=this.tableColumnNameGenerator.generateTableName(update.setModel)
        this.namedSql.append(tableName)
        this.namedSql.append(GRAMMARKEYS.SET)
        update.fieldValues.forEach {
            var columnName=this.tableColumnNameGenerator.generateColumnName(it.field,true)
            var (namedColumnName,ColumnKeyName)=this.tableColumnNameGenerator.generateNamedParameter(columnName)
            this.namedSql.append(columnName)
            this.namedSql.append(GRAMMARKEYS.ASSIGN)
            this.namedSql.append(namedColumnName)
            this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
            this.namedParameters[ColumnKeyName]=it
        }
        this.namedSql=StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.FIELD_COMMA))
        if(update.whereExpression!=null){
            this.namedSql.append(GRAMMARKEYS.WHERE)
        }
        this.accept(update.whereExpression,update)

    }
    private fun buildSelect(select: SelectStatement, parent: ModelExpression?){

//        var hasClosure=select.hasClosure(parent)
//        if (hasClosure){
//            this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
//        }
          if(select.countExpression!=null||select.maxExpressions!=null
                ||select.minExpressions!=null||select.avgExpressions!=null
                ||select.sumExpressions!=null){
                this.namedSql.append(GRAMMARKEYS.SELECT)
                if (select.countExpression!=null){
                    this.namedSql.append(GRAMMARKEYS.COUNT)
                    this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
                    if(select.countExpression!!.field!=null){
                        this.namedSql.append(select.countExpression!!.field!!.name)
                    }
                    else{
                        this.namedSql.append(GRAMMARKEYS.ASTERISK)
                    }
                    this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
                    this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
                }

                select.maxExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(GRAMMARKEYS.MAX)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
                    }
                }
                select.minExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(GRAMMARKEYS.MIN)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
                    }
                }
                select.avgExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(GRAMMARKEYS.AVG)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
                    }
                }
                select.sumExpressions?.forEach {
                    if(it.field!=null){
                        this.namedSql.append(GRAMMARKEYS.SUM)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_PREFIX)
                        this.namedSql.append(it.field!!.name)
                        this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX)
                        this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
                    }
                }
            if(select.selectFields.isEmpty()){
                this.namedSql=StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.FIELD_COMMA))
            }
        }
        else if(select.selectFields.count()>0){
            this.namedSql.append(GRAMMARKEYS.SELECT)
        }
        else
        {
            this.namedSql.append(GRAMMARKEYS.SELECT_ALL)
        }



        select.selectFields.forEach {
            this.accept(it,select)
            this.namedSql.append(GRAMMARKEYS.FIELD_COMMA)
        }

        if (select.selectFields.isNotEmpty()){
            this.namedSql=StringBuilder(this.namedSql.removeSuffix(GRAMMARKEYS.FIELD_COMMA))
        }

        if(select.fromModel!=null){
            var tableName=this.tableColumnNameGenerator.generateTableName(select.fromModel)
            this.namedSql.append(GRAMMARKEYS.FROM)
            this.namedSql.append(tableName)
        }

        select.joinModels?.forEach{
           // this.visit(it,select)
            this.accept(it,select)
        }
        if(select.expression!=null){
            this.namedSql.append(GRAMMARKEYS.WHERE)
        }
        this.accept(select.expression,select)

        this.accept(select.groupBy,select)

        this.accept(select.orderBy,select)

        if(select.offset!=null){
            this.namedSql.append(GRAMMARKEYS.OFFSET)
            this.namedSql.append(select.offset!!)
        }
        if(select.limit!=null){
            this.namedSql.append(GRAMMARKEYS.LIMIT)
            this.namedSql.append(select.limit!!)
        }
//        if(hasClosure){
//            this.namedSql.append(GRAMMARKEYS.BRACKET_SUFFIX);
//        }
    }
}