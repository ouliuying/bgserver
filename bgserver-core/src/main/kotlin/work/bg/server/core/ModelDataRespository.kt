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

package work.bg.server.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


//access right //add update read delete
abstract  class  ModelDataRespository{
    @Autowired
    protected val namedParameterJdbcTemplate: NamedParameterJdbcTemplate?=null
    @Autowired
    protected val jdbcTemplate: JdbcTemplate?=null

//     protected  inline fun <reified T:Any> query(selectStatement: SelectStatementProvider,fields:Array<SqlColumn<*>>?=null):Array<out T>?{
//        val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
//        val records = this.namedParameterJdbcTemplate?.query(selectStatement.getSelectStatement(), namedParameters,
//                object : RowMapper<T> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): T {
//                        return ResultSetDataReaderUtil.scan(rs,fields)
//                    }
//                })
//            return records?.toTypedArray()
//    }
//
//    protected  inline fun <reified T:Any,reified  T2:Any> query(selectStatement: SelectStatementProvider,
//                                                                 aliasName:String,
//                                                                 aliasName2:String,
//                                                                 fields:Array<SqlColumn<*>>?=null):Array<Pair<out T,out T2>>?{
//        val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
//        val records = this.namedParameterJdbcTemplate?.query(selectStatement.getSelectStatement(), namedParameters,
//                object : RowMapper<Pair<T,T2>> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): Pair<T,T2> {
//                        return ResultSetDataReaderUtil.scan2(rs,aliasName,aliasName2,fields)
//                    }
//                })
//        return records?.toTypedArray()
//    }
//    protected  fun query(selectStatement: SelectStatementProvider,fields:Array<SqlColumn<*>>?=null, body:(rs:ResultSet,rowNum:Int)->Unit){
//        val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
//        this.namedParameterJdbcTemplate?.query(selectStatement.getSelectStatement(), namedParameters,
//                object : RowMapper<Any?> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): Any? {
//                        body(rs,rowNum)
//                        return null
//                    }
//                })
//    }
//    protected  inline fun <reified T:Any,reified  T2:Any,reified T3:Any> query(selectStatement: SelectStatementProvider,
//                                                                aliasName:String,
//                                                                aliasName2:String,
//                                                                               aliasName3:String,
//                                                                fields:Array<SqlColumn<*>>?=null):Array<Triple<out T,out T2,out T3>>?{
//        val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
//        val records = this.namedParameterJdbcTemplate?.query(selectStatement.getSelectStatement(), namedParameters,
//                object : RowMapper<Triple<T,T2,T3>> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): Triple<T,T2,T3> {
//                        return ResultSetDataReaderUtil.scan3(rs,aliasName,aliasName2,aliasName3,fields)
//                    }
//                })
//        return records?.toTypedArray()
//    }
//
//
//    protected  inline fun <reified T:Any> queryOne(selectStatement: SelectStatementProvider,fields:Array<SqlColumn<*>>?=null):T?{
//        val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
//        val records = this.namedParameterJdbcTemplate?.query(selectStatement.getSelectStatement(), namedParameters,
//                object : RowMapper<T> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): T {
//                        return ResultSetDataReaderUtil.scan(rs,fields)
//                    }
//                })
//        return records?.firstOrNull()
//    }
//
//    protected  inline fun <reified T:Any> queryRaw(sql:String,namedParameters:MapSqlParameterSource,fields:Array<SqlColumn<*>>?=null):Array<T>?{
//        val records = this.namedParameterJdbcTemplate?.query(sql, namedParameters,
//                object : RowMapper<T> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): T {
//                        return ResultSetDataReaderUtil.scan(rs,fields)
//                    }
//                })
//        return records?.toTypedArray()
//    }
//    fun count(selectStatement: SelectStatementProvider):Long?{
//        val namedParameters = MapSqlParameterSource(selectStatement.getParameters())
//        val records = this.namedParameterJdbcTemplate?.query(selectStatement.getSelectStatement(), namedParameters,
//                object : RowMapper<Long> {
//                    @Throws(SQLException::class)
//                    override  fun mapRow(rs: ResultSet, rowNum: Int): Long {
//                        return rs.getLong(0)
//                    }
//                })
//        return records?.firstOrNull()
//    }
//    fun searchUserAuthModelFields(partnerCxt: PartnerContext,
//                                       modelName: String,
//                                       modelViewName: String,
//                                       modelColumns:Array<SqlColumn<*>>
//                                       ):Array<SqlColumn<*>>?{
//        if(partnerCxt.isSys()){
//            return modelColumns
//        }
//        if(!canAccessModelView(partnerCxt,modelName,modelViewName))
//        {
//            return null
//        }
//
//        return null
//    }
//    fun canAccessModelView(partnerCxt: PartnerContext,
//                          modelName: String,
//                          modelViewName: String): Boolean{
//        if(partnerCxt.isSys())
//        {
//           return true
//        }
//        return false
//    }
}