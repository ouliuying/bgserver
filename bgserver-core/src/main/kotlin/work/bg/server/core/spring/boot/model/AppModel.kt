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

package work.bg.server.core.spring.boot.model

import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import work.bg.server.core.RefSingleton
import work.bg.server.core.config.AppPackageManifest
import work.bg.server.core.constant.ModelReservedKey
import work.bg.server.core.mq.*
import work.bg.server.core.model.billboard.FieldDefaultValueBillboard
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.superclasses

class  AppModel(modelMetaDatas: List<ModelMetaData>, val appPackageManifests:Map<String,AppPackageManifest>, var appContext: ApplicationContext?=null){
    private  val logger = LogFactory.getLog(javaClass)
    private  var appModelRegistries: MutableMap<String,ModelActionRegistry> = HashMap<String,ModelActionRegistry>()
    private  var modelMetaDatas=null as List<ModelMetaData>?
    private  var tableModelMap= null as MutableMap<String,ModelBase>?
    private  var appModelMapCache = null as MutableMap<String,ModelBase>?
    @Autowired
    val requestMappingHandlerAdapter: RequestMappingHandlerAdapter?=null
    @Autowired
    val jdbcTemplate:JdbcTemplate?=null
    companion object :RefSingleton<AppModel>{
        override lateinit var ref: AppModel
    }
    val models:List<ModelBase>?
       get() {
           return this.appModelMapCache?.values?.toList()
       }
    init {
            logger.info("AppModel load start at ${Date()}")
            modelMetaDatas.forEach {
            if (this.appModelRegistries.containsKey(it.appName)){
                this.appModelRegistries[it.appName]?.addModel(it)
            }
            else
            {
                val registry = ModelActionRegistry(it.appName, DefaultBuildModelStrategy())
                registry.addModel(it)
                this.appModelRegistries[it.appName]=registry
            }
        }

        AppModel.ref=this
    }

    //
    fun fieldsFromTable(table:String):FieldCollection?{
        return this.tableModelMap?.get(table)?.fields
    }
    fun getModel(table:String?):ModelBase?{
        if(table.isNullOrEmpty()){
            return null
        }
        return this.tableModelMap?.get(table)
    }
    fun getModel(appName:String?,modelName:String?):ModelBase?{
        if(appName.isNullOrEmpty() || modelName.isNullOrEmpty()){
            return null
        }
        var key="$appName.$modelName"
        return this.appModelMapCache?.get(key)
    }
    fun getTargetField(field:RefTargetField):FieldBase?{
        var targetModel=this.getModel(field.targetModelTable!!)
        return targetModel?.fields?.getField(field.targetModelFieldName)
    }
    fun refresh(): List<ModelMetaData?>{
        var concreteModelMetaDatas= mutableListOf<ModelMetaData?>()
        this.appModelRegistries.forEach {
            concreteModelMetaDatas.addAll(it.value.refresh())
        }
       // var modelMetaDatas=concreteModelMetaDatas as List<ModelMetaData>?
        this.modelMetaDatas=overrideModelMeta(concreteModelMetaDatas)
        return this.modelMetaDatas as List<ModelMetaData?>
    }
    private fun overrideModelMeta(modelMetaDatas:MutableList<ModelMetaData?>):List<ModelMetaData>?{
        var overrideModelMetaDatas= mutableListOf<ModelMetaData?>()
        while (modelMetaDatas.count()>0){
            var fmmd=modelMetaDatas.first()
            var cls=(fmmd?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass
            var fmmds=modelMetaDatas.filter { it->
                var tCls=(it?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass
                tCls?.kotlin?.isSubclassOf(cls?.kotlin!!)!! || cls?.kotlin?.isSubclassOf(tCls?.kotlin!!)!!
            }
            modelMetaDatas.removeAll(fmmds)
            var (minFmmd,maxFmmd)=this.getMaxMinLevelModelMeta(fmmds)
            var mmd=ModelMetaData(minFmmd!!.appName,minFmmd.modelName,minFmmd!!.title,maxFmmd!!.beanDefinitionHolder,minFmmd.index)
            overrideModelMetaDatas.add(mmd)
        }
        return overrideModelMetaDatas as List<ModelMetaData>?
    }
    private fun getMaxMinLevelModelMeta(fmmds:List<ModelMetaData?>):Pair<ModelMetaData?,ModelMetaData?>{
        var fmmd=fmmds.first()
        var maxFmmd=fmmds.first()
        var cnt=fmmds.count()
        if(cnt>1){
            for(i in 1 until cnt){
                var oldCls=(fmmd?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass!!
                var maxOldCls=(maxFmmd?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass!!
                var iCls=(fmmds[i]?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass!!
                if(oldCls.kotlin.isSubclassOf(iCls.kotlin)){
                    fmmd=fmmds[i]
                }
                if(maxOldCls.kotlin.isSuperclassOf(iCls.kotlin)){
                    maxFmmd=fmmds[i]
                }
            }
        }
        return Pair(fmmd,maxFmmd)
    }
    private  fun initialize(){
        this.registerSingleton()
        this.buildDatabase()
    }
    private fun registerSingleton(){

        val data = ArrayList<Array<String>>()
        this.modelMetaDatas?.forEach{ it ->
            var bean=this.appContext?.getBean((it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass) as ModelBase?
            var currCls=bean!!::class as? KClass<*>
            var depLines = arrayListOf<String>()
            while(currCls!=null){
                depLines.add(currCls.qualifiedName?:"")
                var mfs=currCls.companionObjectInstance as? RefSingleton<Any>
                mfs?.ref=bean
                currCls=currCls.superclasses.firstOrNull {sit->
                    sit.isSubclassOf(ModelBase::class)
                }
            }
            if(bean!=null){
                data.add(depLines.toTypedArray())
                logger.info("load model ${bean.javaClass.canonicalName} at ${Date()}")
            }
        }
        val txt = data.joinToString("\r\n============\r\n"){
            it.joinToString()
        }
        var  outputFile:FileWriter?=null
        try {

            val filePath = "hierarchy/model.txt"
            val path = Paths.get("hierarchy")
            if(Files.notExists(path)){
                Files.createDirectory(path)
            }
            val file = File(filePath)
            // create FileWriter object with file as parameter
            outputFile = FileWriter(file)
        }
        catch (ex:java.lang.Exception){

        }
        finally {
            outputFile?.write(txt)
            outputFile?.flush()
            outputFile?.close()
        }

    }
    private fun buildDatabase(){
        this.tableModelMap= mutableMapOf()
        this.modelMetaDatas?.forEach {
            var bean=this.appContext?.getBean((it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass) as ModelBase?
            var fields=bean?.getModelFields()
            bean?.fields=fields!!
            bean?.meta?.appName=it.appName
            bean?.meta?.name=it.modelName
            bean?.meta?.title=it.title
            this.tableModelMap?.put(bean?.fullTableName!!,bean)
        }
        this.appModelMapCache= mutableMapOf()
        this.tableModelMap?.forEach { t, u ->
            this.appModelMapCache?.put("${u.meta?.appName}.${u.meta?.name}",u)
        }
        //todo create database tables
        this.tableModelMap?.forEach {
            if(it.value.isDynamic()){
                return@forEach
            }
            this.createOrUpdateTable(it.value)
        }
        this.tableModelMap?.forEach {
            if(it.value.isDynamic()){
                return@forEach
            }
            this.createOrUpdateTableIndexOrForeignKey(it.value)
        }
    }

    private fun createOrUpdateTableIndexOrForeignKey(model:ModelBase?){
        var foreignKeys=this.getModelForeignKeyFields(model)
        foreignKeys?.forEach {
            var fk=(it as RefTargetField).foreignKey
            var fkm=it.model
            var tf=(it as RefTargetField).targetModelFieldName
            var tm=(it as RefTargetField).targetModelTable
            var tModel=this.getModel(tm)
            var tField=tModel?.fields?.getField("${tModel.fullTableName}.$tf")
            if(tModel!=null && tField!=null){
                try {
                    when {
                        fk?.action==ForeignKeyAction.CASCADE -> runSql("ALTER TABLE ${fkm?.fullTableName}\n" +
                                "    ADD CONSTRAINT ${this.getColumnForeignKeyName(it,fkm,tField,tModel)} FOREIGN KEY (${it.name})\n" +
                                "    REFERENCES ${tModel.fullTableName} (${tField.name}) MATCH SIMPLE\n" +
                                "    ON UPDATE NO ACTION\n" +
                                "    ON DELETE CASCADE;")
                        fk?.action==ForeignKeyAction.SET_NULL -> runSql("ALTER TABLE ${fkm?.fullTableName}\n" +
                                "    ADD CONSTRAINT ${this.getColumnForeignKeyName(it,fkm,tField,tModel)} FOREIGN KEY (${it.name})\n" +
                                "    REFERENCES ${tModel.fullTableName} (${tField.name}) MATCH SIMPLE\n" +
                                "    ON UPDATE NO ACTION\n" +
                                "    ON DELETE SET NULL;")
                        else -> runSql("ALTER TABLE ${fkm?.fullTableName}\n" +
                                "    ADD CONSTRAINT ${this.getColumnForeignKeyName(it,fkm,tField,tModel)} FOREIGN KEY (${it.name})\n" +
                                "    REFERENCES ${tModel.fullTableName} (${tField.name}) MATCH SIMPLE\n" +
                                "    ON UPDATE NO ACTION\n" +
                                "    ON DELETE NO ACTION;")
                    }

                }
                catch (ex:Exception){

                }
            }
        }
        var (indexFields,indexUnique) =this.getModelIndexFields(model)
        indexFields?.forEach { k, u ->
            var (indexName,columns,unique)=this.getColumnIndexName(k, u,indexUnique)
            if(unique){
                runSql("CREATE UNIQUE INDEX $indexName\n" +
                        "    ON ${model?.fullTableName}\n" +
                        "    ($columns)")
            }
            else{
                runSql("CREATE INDEX $indexName\n" +
                        "    ON ${model?.fullTableName}\n" +
                        "    ($columns)")
            }
        }
    }
    private  fun createSchema(schema:String?){
        var tmpSchema=schema
        if(tmpSchema.isNullOrEmpty()){
            tmpSchema="public"
        }
        val sql="CREATE SCHEMA IF NOT EXISTS $tmpSchema"
        try {
            jdbcTemplate?.execute(sql)
        }
        finally {

        }
    }
    private fun runSql(sql:String){
        try {

            this.jdbcTemplate?.execute(sql)
        }
        catch (ex:Exception){

        }
    }

    private fun createOrUpdateTable(model:ModelBase?){
        this.createSchema(model?.schemaName)
        //var sql=StringBuilder()
        runSql("CREATE TABLE IF NOT EXISTS ${bakeSqlKeyword(model?.fullTableName!!)}();")
        model?.fields?.forEach {
            if((it !is FunctionField<*>) && (it !is ModelOne2ManyField) && (it !is ModelMany2ManyField) && (it !is ModelOne2OneField)) {
                if(!isTableColumnExist(model,it)){
                    runSql("ALTER TABLE ${bakeSqlKeyword(model.fullTableName)} ADD  COLUMN   IF NOT EXISTS  ${bakeSqlKeyword(it.name)} ${this.getColumnTypeExpression(it)};")
                }
                if ((it as ModelField).defaultValue != null) {
                    runSql("ALTER TABLE  ${bakeSqlKeyword(model.fullTableName)} ALTER  COLUMN  ${bakeSqlKeyword(it.name)} SET DEFAULT ${this.getFieldDefaultValueExpression(it)};")
                }
                if((it as ModelField).primaryKey!=null){
                    runSql("ALTER TABLE  ${bakeSqlKeyword(model.fullTableName)} ADD  CONSTRAINT ${this.getColumnPrimaryName(it)} PRIMARY KEY (${bakeSqlKeyword(it.name)});")
                }
            }
            else if(it is ModelOne2OneField){
                if(!it.isVirtualField){
                    if(!isTableColumnExist(model,it)){
                        runSql("ALTER TABLE ${bakeSqlKeyword(model.fullTableName)} ADD  COLUMN   IF NOT EXISTS  ${bakeSqlKeyword(it.name)} ${this.getColumnTypeExpression(it)};")
                    }
                    if ((it as ModelField).defaultValue != null) {
                        runSql("ALTER TABLE  ${bakeSqlKeyword(model.fullTableName)} ALTER  COLUMN  ${bakeSqlKeyword(it.name)} SET DEFAULT ${this.getFieldDefaultValueExpression(it)};")
                    }
                }
            }
        }
    }

    //postgresql add multiple \id/ big serial column
    private fun isTableColumnExist(model:ModelBase,field:FieldBase):Boolean{
        var table=model.tableName
        var schema=model.schemaName
        var column=field.name
        var count=0 as Int
        var sql="select count(*) from information_schema.columns where table_schema='$schema' and table_name='$table' and column_name='$column'"
        this.jdbcTemplate?.query(sql){rs->
               count= rs.getInt(1)
        }
        return count>0
    }
    private fun bakeSqlKeyword(name:String):String{
        return when{
            name.compareTo("table",true)==0->{
                "\"$name\""
            }
            else->name
        }
    }
    private fun getColumnTypeExpression(field:FieldBase?):String{
        when(field?.fieldType){
            FieldType.INT->{
                if((field as ModelField).primaryKey!=null && field.primaryKey!!.serial){
                    return "serial"
                }
                return "integer"
            }
            FieldType.TEXT->{
                return "text"
            }
            FieldType.STRING->{
                return if(field?.length!=null){
                    "character varying(${field.length})"
                } else{
                    "character varying"
                }
            }
            FieldType.BIGINT->{
                if ((field as ModelField).primaryKey != null && field.primaryKey!!.serial) {
                    return "bigserial"
                }
                return "bigint"
            }
            FieldType.DATE->{
                return "date"
            }
            FieldType.DATETIME->{
                return "timestamp without time zone"
            }
            FieldType.NUMBER->{
                return "numeric"
            }
            FieldType.TIME->{
                return "time without time zone"
            }
            FieldType.NONE->{
                return "text"
            }
            else->{
                return "text"
            }
        }
    }

    private fun getColumnPrimaryName(field:FieldBase?):String{
        return "${field?.model?.tableName}_${field?.name}_p_key"
    }

    private  fun getColumnForeignKeyName(field:FieldBase?,model:ModelBase?,
                                        targetField:FieldBase?,targetModel:ModelBase?):String{
        return "${model?.tableName}_${field?.name}_${targetModel?.tableName}_${targetField?.name}_f_key"
    }
    private  fun getColumnIndexName(iKey:String,fields:List<FieldBase?>,uniqueMap:Map<String,Boolean>):Triple<String,String,Boolean>{
       // var f=fields.first()
        var columns=fields.stream().map { it?.name }.toArray()
        var unique = uniqueMap[iKey]?:false
        return Triple(iKey+(if(unique) {
            "_u_index"
        } else "_index"),columns.joinToString(),unique)
    }
    /**
     * support only id primary key
     */
    private fun getModelPrimaryField(model:ModelBase?):FieldBase?{
        return model?.fields?.firstOrNull{
            it.name==ModelReservedKey.idFieldName && (it as ModelField).primaryKey!=null
        }
    }
    private fun getModelForeignKeyFields(model:ModelBase?):List<FieldBase?>?{
        var fks= mutableListOf<FieldBase?>()
        model?.fields?.forEach {
            when(it){
                is Many2ManyField,is Many2OneField,is One2ManyField->{
                    var fk=(it as RefTargetField).foreignKey
                    if(fk!=null){
                        fks.add(it)
                    }
                }
            }
        }
        return if (fks?.count()>0) {
            fks.toList()
        } else null
    }
    private  fun getModelIndexFields(model:ModelBase?):Pair<Map<String,MutableList<FieldBase?>>,MutableMap<String,Boolean>>{
        var scopeIndexMap= mutableMapOf<String,MutableMap<String,MutableList<FieldBase?>>>()
        var indexUniqueMap= mutableMapOf<String,Boolean>()
        model?.fields?.forEach {
            if(it is ModelField){
                if(it.index!=null){
                    it.index.forEach {sit->
                        var scope = sit.scope?:""
                        var name=sit.name?:""
                        if(name.isNullOrEmpty() && scope.isNullOrEmpty()){
                            scope=it.name
                            name=it.name
                        }
                        var key = "i_${scope}_$name"
                        if(indexUniqueMap.containsKey(key)){
                                indexUniqueMap[key]= indexUniqueMap[key]!! && sit.unique
                        }
                        else{
                            indexUniqueMap[key]=sit.unique
                        }
                        if(!scopeIndexMap.containsKey(scope)){
                                scopeIndexMap[scope]= mutableMapOf<String,MutableList<FieldBase?>>()
                        }
                        var indexMap=scopeIndexMap[scope]!!
                        if(indexMap.containsKey(name)){
                            indexMap[name]?.add(it)
                        }
                        else{
                            indexMap[name]= mutableListOf(it) as MutableList<FieldBase?>
                        }
                    }
                }
            }
        }
        var noScopeIndexMap = scopeIndexMap.filter {
            it.key==""
        }
        noScopeIndexMap.forEach {
            for(name2Fields in it.value){
                var name = name2Fields.key
                var fields = name2Fields.value
                for (kv in scopeIndexMap){
                    if(!kv.key.isEmpty()){
                        var targetFields = kv.value[name]
                        if(targetFields!=null){
                            for (f in fields){
                                if(targetFields.count {tit->
                                            f!!.isSame(tit!!)
                                        }<1){
                                    targetFields.add(f)
                                }
                            }
                        }
                    }
                }
            }
        }

        var indexMap = mutableMapOf<String,MutableList<FieldBase?>>()
        scopeIndexMap.forEach { t, u ->
            u.forEach { st, su ->
                var key = "i_${t}_$st"
                indexMap[key]=su
            }
        }
        return Pair(indexMap,indexUniqueMap)
    }
    private fun getFieldDefaultValueExpression(field:ModelField?):String?{
        if(field?.defaultValue!=null && field.defaultValue !is FieldDefaultValueBillboard){
            when(field?.fieldType){
                FieldType.NUMBER,FieldType.BIGINT,FieldType.INT->{
                    return field?.defaultValue.toString()
                }
                FieldType.STRING,FieldType.TEXT->{
                    return "'"+SqlUtil.escapeSqlString(field?.defaultValue.toString())+"'"
                }
                FieldType.TIME->{
                    return "now()::time without time zone"
                }
                FieldType.DATETIME->{
                    return "now()::timestamp without time zone"
                }
                FieldType.DATE->{
                    return "now()::date without time zone"
                }
                else->{return "null"}
            }
        }
        return "null"
    }
    operator fun invoke(request: HttpServletRequest,
                        response: HttpServletResponse,
                        session: HttpSession,
                        appName: String,
                        modelName: String,
                        action: String): Any?{
        try
        {
//            var paramInfo=StringBuilder()
//            paramInfo.append("call $appName.$modelName.$action, parameters = ")
//            request.parameterMap.forEach { t, u ->
//                var pValue=u.joinToString()
//                paramInfo.append("$t =$pValue  ")
//            }
//            try {
//                var allBytes=request.inputStream.readBytes()
//                paramInfo.append("all bytes = ")
//                paramInfo.append(String(allBytes, Charset.defaultCharset()))
//            }
//            catch(ex:Exception)
//            {
//                logger.error(ex.toString())
//            }
//
//            logger.info(paramInfo)
            return this.appModelRegistries[appName]?.invoke(request,response,session,this,appName,modelName,action)
        }
        catch (err:Exception){
            var errMsg=err.toString()
            err.printStackTrace()
            logger.error("$appName - $modelName - $action error: $errMsg")
        }
        finally {
            //
        }
        return null
    }
    fun getBean(typeName:String):Any?{
        return this.appContext?.getBean(Class.forName(typeName))
    }
}



