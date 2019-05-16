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

package work.bg.server.core.context

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.model.AppModel

class JsonClauseResolver(val obj:JsonObject,
                         val model:ModelBase,
                         val context:ModelExpressionContext?=null){
    private  var relationModels:ArrayList<ModelBase> = arrayListOf()
    init {
        this.model.fields.forEach {
            when(it){
                is Many2ManyField->{
                    val tModel=AppModel.ref.getModel(it.targetModelTable!!)
                    if(tModel!=null){
                        this.relationModels.add(tModel)
                    }
                    val rModel=AppModel.ref.getModel(it.relationModelTable!!)
                    if(rModel!=null){
                        this.relationModels.add(rModel)
                    }
                }
                is Many2OneField->{
                    val tModel=AppModel.ref.getModel(it.targetModelTable!!)
                    if(tModel!=null){
                        this.relationModels.add(tModel)
                    }
                }
                is One2ManyField->{
                    val tModel=AppModel.ref.getModel(it.targetModelTable!!)
                    if(tModel!=null){
                        this.relationModels.add(tModel)
                    }
                }
            }
        }
    }

    fun criteria():ModelExpression?{
        return this.createCriteriaFromObject(this.obj)
    }
    private fun createCriteriaFromObject(obj:JsonObject):ModelExpression?{
        var op=obj["op"].asString
        when{
            op.compareTo("or",true)==0->{
                var orExps=this.createCriteriaFromArray(obj["exp"].asJsonArray)
                return if(orExps!=null){
                    or(*orExps)
                } else{
                    null
                }
            }
            op.compareTo("and",true)==0->{
                var andExps=this.createCriteriaFromArray(obj["exp"].asJsonArray)
                return if(andExps!=null){
                    and(*andExps)
                } else{
                    null
                }
            }
            else->{
                val exp=obj["exp"].asJsonArray
                if(exp.count()==4){
                    val fieldName=exp[0].asString
                    var field=this.getFieldByPropertyName(fieldName)
                    if(field!=null){
                        val operator=exp[1].asString
                        val value=exp[2].asString
                        val valueType=exp[3].asString
                        when (valueType) {
                            CriteriaValueType.EXPRESSION -> {
                                return this.createExpression(field,operator,value)
                            }
                            CriteriaValueType.FIELD -> {
                                return this.createFieldExpression(field,operator,value)
                            }
                            CriteriaValueType.VARIABLE -> {
                                return this.createVariableExpression(field,operator,value)
                            }
                            else->{
                                return this.createConstantExpression(field,operator,value)
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    //TODO add sub select support
    private fun createExpression(field:FieldBase,operator:String,value:String):ModelExpression?{

        return null
    }
    private fun createFieldExpression(field:FieldBase,operator:String,value:String):ModelExpression?{
        try {
            var cField=this.getFieldByPropertyName(value)
            if(field!=null){
                return when{
                    operator.compareTo("=",true)==0 ->  eq(field,cField)
                    operator.compareTo(">",true)==0->  gt(field,cField)
                    operator.compareTo("<",true)==0->  lt(field,cField)
                    operator.compareTo(">=",true)==0 -> gtEq(field,cField)
                    operator.compareTo("<=",true)==0 -> ltEq(field,cField)
                    operator.compareTo("<>",true)==0 -> notEq(field,cField)
                    operator.compareTo("!=",true)==0-> notEq(field,cField)
                    operator.compareTo("in",true)==0-> `in`(field,cField)
                    Regex("\\s*not\\s+in\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notIn(field,cField)
                    else-> null
                }
            }
        }
        catch (ex:Exception){

        }
        return null
    }
    private fun createVariableExpression(field:FieldBase,
                                         operator:String,
                                         value:String):ModelExpression?{
        try {
            var kv = this.context?.valueFromContextKey(value)?:return null
            if(kv!!.first){
                return when{
                    operator.compareTo("=",true)==0 ->  eq(field,kv.second)
                    operator.compareTo(">",true)==0->  gt(field,kv.second)
                    operator.compareTo("<",true)==0->  lt(field,kv.second)
                    operator.compareTo(">=",true)==0 -> gtEq(field,kv.second)
                    operator.compareTo("<=",true)==0 -> ltEq(field,kv.second)
                    operator.compareTo("<>",true)==0 -> notEq(field,kv.second)
                    operator.compareTo("!=",true)==0-> notEq(field,kv.second)
                    operator.compareTo("in",true)==0-> `in`(field,kv.second as Array<Any>?)
                    Regex("\\s*not\\s+in\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notIn(field,kv.second as Array<Any>?)
                    operator.compareTo("is",true)==0-> `is`(field,kv.second)
                    Regex("\\s*is\\s+not\\s*",RegexOption.IGNORE_CASE).matches(operator)-> isNot(field,kv.second)
                    operator.compareTo("like",true)==0 -> like(field,kv.second as String)
                    operator.compareTo("iLike",true)==0 -> iLike(field,kv.second as String)
                    Regex("\\s*not\\s+like\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notLike(field,kv.second as String)
                    Regex("\\s*not\\s+ilike\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notILike(field,kv.second as String)
                    else-> null
                }
            }
        }
        catch (ex:Exception){

        }
        return null
    }
    private fun createConstantExpression(field:FieldBase,operator:String,value:String):ModelExpression?{
        try {
            var cValue = if(operator.compareTo("in",true)==0 ||
                    Regex("\\s*not\\s+in\\s*",RegexOption.IGNORE_CASE).matches(operator)){
                var items=JsonParser().parse(value) as JsonArray
                var iValues= arrayListOf<Any?>()
                items.forEach {
                    iValues.add(ModelFieldConvert.toTypeValue(field,it.asString))
                }
                iValues.toTypedArray()
            } else{
                ModelFieldConvert.toTypeValue(field,value)
            }
            return when{
                operator.compareTo("=",true)==0 ->  eq(field,cValue)
                operator.compareTo(">",true)==0->  gt(field,cValue)
                operator.compareTo("<",true)==0->  lt(field,cValue)
                operator.compareTo(">=",true)==0 -> gtEq(field,cValue)
                operator.compareTo("<=",true)==0 -> ltEq(field,cValue)
                operator.compareTo("<>",true)==0 -> notEq(field,cValue)
                operator.compareTo("!=",true)==0-> notEq(field,cValue)
                operator.compareTo("in",true)==0-> `in`(field,cValue as Array<Any>?)
                Regex("\\s*not\\s+in\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notIn(field,cValue as Array<Any>?)
                operator.compareTo("is",true)==0-> `is`(field,cValue)
                Regex("\\s*is\\s+not\\s*",RegexOption.IGNORE_CASE).matches(operator)-> isNot(field,cValue)
                operator.compareTo("like",true)==0 -> like(field,cValue as String)
                operator.compareTo("iLike",true)==0 -> iLike(field,cValue as String)
                Regex("\\s*not\\s+like\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notLike(field,cValue as String)
                Regex("\\s*not\\s+ilike\\s*",RegexOption.IGNORE_CASE).matches(operator)-> notILike(field,cValue as String)
                else-> null
            }
        }
        catch (ex:Exception){

        }
        return null
    }
    private  fun getFieldByPropertyName(fieldName:String):FieldBase?{
        val app=this.model.meta.appName
        val model=this.model.meta.name
        val fieldNameItems=fieldName.split('.')
        when {
            fieldNameItems.count()==1 -> {
                return this.model.fields.getFieldByPropertyName(fieldNameItems[0])
            }
            fieldNameItems.count()==2 -> {
                val model= AppModel.ref.getModel(app,fieldNameItems[0])
                if(this.relationModels.contains(model!!)){
                    return this.model.fields.getFieldByPropertyName(fieldNameItems[1])
                }
            }
            fieldNameItems.count()==3 -> {
                val model= AppModel.ref.getModel(fieldNameItems[0],fieldNameItems[1])
                if(this.relationModels.contains(model!!)){
                    return this.model.fields.getFieldByPropertyName(fieldNameItems[2])
                }
            }
        }
        return null
    }

    private  fun createCriteriaFromArray(jArr:JsonArray):Array<ModelExpression>?{
        var exps= arrayListOf<ModelExpression>()
        jArr.forEach {
            var c=this.createCriteriaFromObject(it.asJsonObject)
            if(c!=null){
                exps.add(c)
            }
        }
        return if(exps.count()>0) exps.toTypedArray() as Array<ModelExpression> else null
    }
}