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

package work.bg.server.core.mq

import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.SqlParameterValue
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import work.bg.server.core.constant.ModelReservedKey
import work.bg.server.core.model.AccessControlModel
import work.bg.server.core.mq.join.JoinModel
import java.sql.JDBCType
import java.sql.ResultSet
import work.bg.server.core.mq.update as mqUpdate
import work.bg.server.core.mq.create as mqCreate
import work.bg.server.core.spring.boot.model.AppModel
import java.lang.StringBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

abstract class ModelBase(val tableName:String,val schemaName:String = "public"){
    private val logger = LogFactory.getLog(javaClass)
    @Autowired
    protected lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    protected lateinit var appModel:AppModel
    lateinit var fields:FieldCollection
    val fieldKeyHeader by lazy{"${this.schemaName}.${this.tableName}"}
    val fullTableName by lazy{"${this.schemaName}.${this.tableName}"}


    val meta by lazy { Meta() }

    //private lateinit var meta:ModelMeta
    private val tableColumnNameGenerator=ModelTableColumnAliasNameGenerator()




    init {

    }
    open fun getFieldKey(fieldName:String?):String?{
        return "${this.fieldKeyHeader}.$fieldName"
    }
    open fun isAssociative():Boolean{
        return false
    }
    open fun isDynamic():Boolean{
        return false
    }
    fun getFieldByPropertyName(propertyName:String):FieldBase?{
        return this.fields?.getFieldByPropertyName(propertyName)
    }
    open fun getModelFields():FieldCollection{

        var thisCls=this::class as KClass<*>?
        var baseCls=ModelBase::class as KClass<*>
        if(this.skipCorpIsolationFields()){
            baseCls = AccessControlModel::class
        }
        val mType=FieldBase::class.createType()

        var fields=FieldCollection()
        do{
            var fs=thisCls?.declaredMemberProperties?.filter {
                it.getter.returnType.isSubtypeOf(mType)
            }
            fs?.forEach{property->
                var fb=property.getter.call(this) as FieldBase?
                fb?.model=this
                var pp=FieldBase::class.declaredMemberProperties.firstOrNull {
                    it.name== ModelReservedKey.propertyName
                }
                if(pp!=null){
                    pp.isAccessible=true
                    //(pp as KMutableProperty1).set(fb,property.name)
                    pp.javaField?.set(fb,property.name)
                }
                fields.add(fb)
            }
            thisCls=thisCls?.superclasses?.firstOrNull{
                it.isSubclassOf(baseCls)
            }
            if(thisCls?.qualifiedName==baseCls.qualifiedName){
                break
            }
        }while (thisCls!=null)

        return fields
    }

    open fun skipCorpIsolationFields():Boolean{
        return false
    }
    open fun isSame(model:ModelBase?):Boolean{
        if(model!=null)
            return this.meta.tag == model.meta.tag
        return false
    }
    protected open fun query(vararg fields:FieldBase,
                             fromModel:ModelBase,
                             joinModels:Array<JoinModel>?=null,
                             criteria:ModelExpression?=null,
                             groupBy:GroupBy?=null,
                             orderBy:OrderBy?=null,
                             offset:Int?=null,
                             limit:Int?=null):ModelDataArray?{
        var rFields= mutableListOf<FieldBase>()
        if(fields.count()>0){
            rFields.addAll(fields)
        }
        else{
            rFields.addAll(fromModel.fields?.getAllPersistFields()?.values?.toTypedArray()?.toList()!!)
        }
        var st=select(*rFields.toTypedArray(),fromModel = fromModel)
        joinModels?.forEach {
            st.join(it)
        }
        st.groupBy(groupBy).where(criteria).orderBy(orderBy).offset(offset).limit(limit)
        var rendData=st.render(null)
       // val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
        //var parameters=this.fieldValueToParameters(rendData?.second)
        return this.querySql(
                rendData!!.first,
                st.selectFields as Array<FieldBase>,
                null,
                rendData?.second
        )
    }
    protected open fun query(vararg fields:FieldBase,
                             joinModels:Array<JoinModel>?=null,
                             criteria:ModelExpression?=null,
                             groupBy:GroupBy?=null,
                             orderBy:OrderBy?=null,
                             offset:Int?=null,
                             limit:Int?=null):ModelDataArray? {
      return this.query(*fields,fromModel = this,joinModels = joinModels,criteria = criteria,groupBy = groupBy,orderBy = orderBy,offset = offset,limit = limit)
    }

    protected open fun fieldValueToParameters(fieldValues:Map<String,FieldValue>?):Map<String, SqlParameterValue?>?{
        if (fieldValues!=null) {
            var mp = mutableMapOf<String, SqlParameterValue?>()
            fieldValues.forEach {
                var jdbcType = it.value.field.fieldType.value
                var p = SqlParameterValue(jdbcType.vendorTypeNumber, it.value.value)
                mp[it.key] = p
            }
            return mp
        }
        return null
    }

    protected open fun fieldValueToParameters(fieldValues:Array<FieldValue>?):Map<String, SqlParameterValue?>?{
        if (fieldValues!=null) {
            var mp = mutableMapOf<String, SqlParameterValue?>()
            fieldValues.forEach {
                var jdbcType = it.field.fieldType as Int
                var p = SqlParameterValue(jdbcType, it.value)
                var columnName=this.tableColumnNameGenerator.generateColumnName(it.field)
                mp[columnName] = p
            }
            return mp
        }
        return null
    }

    protected  open fun readDataFromRecord(record:ResultSet,fields:Array<out FieldBase>?):ModelDataObject{
        var data= ModelDataObject()
        var maxRange=record.metaData.columnCount
        (1..maxRange).forEach {
            var f=fields?.get(it-1)
            var value=scanRecordColumnValue(record,it)
            data.data.add(FieldValue(f!!,value))
        }
        return data
    }
    protected  open fun scanRecordColumnValue(record:ResultSet,index:Int):Any?{
        var cTyp=JDBCType.valueOf(record.metaData.getColumnType(index))
        return when (cTyp){
            JDBCType.TIMESTAMP->record.getTimestamp(index)
            JDBCType.LONGNVARCHAR,JDBCType.NVARCHAR,JDBCType.LONGVARCHAR,JDBCType.VARCHAR->record.getString(index)
            JDBCType.NUMERIC,JDBCType.DECIMAL->record.getBigDecimal(index)
            JDBCType.INTEGER->record.getInt(index)
            JDBCType.BIGINT->record.getLong(index)
            JDBCType.DATE->record.getDate(index)
            JDBCType.TIME->record.getTime(index)
            JDBCType.DOUBLE->record.getDouble(index)
            JDBCType.FLOAT->record.getFloat(index)
            else->null
        }
    }
   open fun delete(modelData: ModelData, criteria:ModelExpression?=null):Long?{
        var rData=work.bg.server.core.mq.delete(modelData.model!!).where(criteria).render(null)
        var parameters=this.fieldValueToParameters(rData?.second)
        var ret= if(parameters!=null)
        {
            this.namedParameterJdbcTemplate?.execute(rData?.first,parameters) {
                it.executeQuery()
                it.updateCount
            }
        }
        else
        {
            this.namedParameterJdbcTemplate?.execute(rData?.first) {
                it.executeQuery()
                it.updateCount
            }
        }
        return ret?.toLong()
    }


    open fun update(modelDataObject: ModelDataObject, criteria:ModelExpression?=null):Long?{
        var rData=mqUpdate(*modelDataObject.data.toTypedArray(),setModel = modelDataObject.model!!).where(criteria).render(null)
        logger.info(rData?.first)
        var parameters=this.fieldValueToParameters(rData?.second)
        var uRet= if(parameters!=null)
        {
            this.namedParameterJdbcTemplate?.update(rData?.first,parameters)
        }
        else
        {
            this.jdbcTemplate?.update(rData?.first)
        }
        return uRet?.toLong()
    }


    open fun create(modelDataObject:ModelDataObject):Long?{
        var rData=mqCreate(*modelDataObject.data.toTypedArray(),model=modelDataObject.model!!).render(null)
        var parameters=this.fieldValueToParameters(rData?.second)
        return if (parameters!=null){
            this.namedParameterJdbcTemplate?.execute(rData?.first,parameters){
                it.execute()
                it.resultSet.next()
                it.resultSet.getLong(1)
            }
        }
        else{
            this.namedParameterJdbcTemplate?.execute(rData?.first){
                it.execute()
                it.resultSet.next()
                it.resultSet.getLong(1)
            }
        }

    }

    open fun querySql(sql:String,selectFields:Array<FieldBase>?,model:ModelBase?=null,parameters:Map<String,FieldValue>?=null):ModelDataArray?{
        var sb = StringBuilder()
        parameters?.let {
            it.forEach { t, u ->
                sb.append("  $t =")
                u.value?.let {
                    sb.append("${it.toString()}")
                }
            }
        }
        this.logger.info("sql = ${sql}, values = ${sb.toString()}")
        var kParameters=this.fieldValueToParameters(parameters)
        if(kParameters!=null)
            return this.namedParameterJdbcTemplate?.query(sql,kParameters, ResultSetExtractor<ModelDataArray?> {
                var mda= ModelDataArray(fields= arrayListOf(),model=model)
                mda.fields?.addAll(selectFields!!.toList())
                while (it.next()){
                    mda.data.add(readDataFromRecord(it,selectFields).data)
                }
                mda
            })
        else
            return this.namedParameterJdbcTemplate?.query(sql, ResultSetExtractor<ModelDataArray?> {
                var mda= ModelDataArray(fields= arrayListOf(),model=model)
                mda.fields?.addAll(selectFields!!.toList())
                while (it.next()){
                    mda.data.add(readDataFromRecord(it,selectFields).data)
                }
                mda
            })
    }
    open fun queryCount(select:SelectStatement):Int{
        var render = select.render(null)
        var kParameters=this.fieldValueToParameters(render!!.second)
        var sql=render!!.first
        return if(kParameters!=null)
            return this.namedParameterJdbcTemplate?.query(sql,kParameters, ResultSetExtractor<Int> {
                it.next()
                it.getInt(1)
            })
        else
            return this.namedParameterJdbcTemplate?.query(sql, ResultSetExtractor<Int> {
                it.next()
                it.getInt(1)
            })
    }

   open fun queryMax(select:SelectStatement):Long?{
        var render = select.render(null)
        var kParameters=this.fieldValueToParameters(render!!.second)
        var sql=render!!.first
        return if(kParameters!=null)
            return this.namedParameterJdbcTemplate?.query(sql,kParameters, ResultSetExtractor<Long?> {
                it.next()
                it.getLong(1)
            })
        else
            return this.namedParameterJdbcTemplate?.query(sql, ResultSetExtractor<Long?> {
                it.next()
                it.getLong(1)
            })
    }

    open fun querySql(sql:String,selectFields:Array<FieldBase>?,model:ModelBase?=null,parameters:Array<FieldValue>?=null):ModelDataArray?{
        return if(parameters!=null && parameters.isNotEmpty()){
            var mParameters= mutableMapOf<String,FieldValue>()
            parameters.forEach {
                var columnName=this.tableColumnNameGenerator.generateColumnName(it.field)
                mParameters[columnName]=it
            }
            this.querySql(sql,selectFields,model,mParameters)
        }
        else{
            this.querySql(sql,selectFields,model,null as Map<String, FieldValue>)
        }
    }

    open fun executeSql(sql:String,parameters:Map<String,FieldValue>?=null):Boolean?{

        var tParameters=this.fieldValueToParameters(parameters)
        return if (tParameters!=null)
        {
            this.namedParameterJdbcTemplate?.execute(sql,tParameters) {
                it.execute()
            }
        }
        else
        {
            this.namedParameterJdbcTemplate?.execute(sql) {
                it.execute()
            }
        }
    }

    open fun executeSql(sql:String,parameters:Array<FieldValue>?=null):Boolean?{
        return if(parameters!=null){
            var mParameters= mutableMapOf<String,FieldValue>()
            parameters.forEach {
                var columnName=this.tableColumnNameGenerator.generateColumnName(it.field)
                mParameters[columnName]=it
            }
            this.executeSql(sql,mParameters)
        }
        else
        {
            this.executeSql(sql,null as Map<String, FieldValue>?)
        }
    }
    inner class Meta{
        lateinit var name:String
        lateinit var appName:String
        lateinit var title:String
        var fields=this@ModelBase.fields
        val tag by lazy{"${this.appName}.${this.name}"}
    }

}