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

package work.bg.server.core.context

import dynamic.model.query.exception.ModelErrorException
import dynamic.model.query.mq.model.ModelBase
import java.lang.StringBuilder
import kotlin.reflect.KClass

class CriteriaStatementUtil {
    internal object CriteriaKey {
        const val SPACE = " "
        const val BRACKET_PREFIX = "("
        const val BRACKET_SUFFIX = ")"
    }
    companion object {
        fun parse(statement:String, model: ModelBase?, context: ModelExpressionContext): dynamic.model.query.mq.ModelExpression?{
             return CriteriaTree(statement).build()
        }

    }
    open class CriteriaTreeNode(val statement:String,var subNodes:ArrayList<CriteriaTreeNode>?=null)
    class AndCriteriaTreeNode(statement:String):CriteriaTreeNode(statement){
        val operator="and"
    }
    class OrCriteriaTreeNode(statement:String):CriteriaTreeNode(statement){
        val operator="or"
    }
    class FieldExpressionTreeNode(statement:String):CriteriaTreeNode(statement){
        var fieldName:String=""
        var fieldValue:String=""
        var operator:String=""
    }
    class FieldInExpressionTreeNode(statement: String):CriteriaTreeNode(statement){
        var fieldName:String=""
        var inValues:ArrayList<String>?=null
        var isNotIn:Boolean=false
    }
    class FieldLikeExpressionTreeNode(statement:String):CriteriaTreeNode(statement){
        var fieldName:String=""
        var likeValue:String=""
        var isNotLike:Boolean=false
    }
    class ExistsExpressionTreeNode(statement:String):CriteriaTreeNode(statement){
        var isNotExists:Boolean=false
    }
    class SubExpressionTreeNode(statement: String):CriteriaTreeNode(statement)
    class SelectExpressionTreeNode(statement:String):CriteriaTreeNode(statement){
        var table:String?=null
        var fields:ArrayList<String>?=null
    }
    class JoinExpressionTreeNode(statement: String):CriteriaTreeNode(statement){
        var table:String?=null
        var join:String?=null
    }

    class CriteriaTree(private val statement:String){
        private var root:CriteriaTreeNode?=null
        private var fieldExpressionHeader=Regex("^\\s+?[a-z][a-z\\.]+?[a-z]\\s+?[=,>,<,\\!]+?",RegexOption.IGNORE_CASE)
        private var fieldInExpressionHeader=Regex("^\\s+?[a-z][a-z\\.]+?[a-z]\\s+(not)\\s+in\\s+\\(",RegexOption.IGNORE_CASE)
        private var fieldLikeExpressionHeader=Regex("^\\s+?[a-z][a-z\\.]+?[a-z]\\s+(not)\\s+like",RegexOption.IGNORE_CASE)
        private var fieldExistsExpressionHeader=Regex("^\\s+?(not)\\s+exists\\s+\\(",RegexOption.IGNORE_CASE)
        private var fileSubExpressionHeader=Regex("^\\s+?\\(",RegexOption.IGNORE_CASE)
        private var andExressionHeader=Regex("^\\s+?and\\s+",RegexOption.IGNORE_CASE)
        private var orExpressionHeader=Regex("^\\s+?or\\s+",RegexOption.IGNORE_CASE)
        private var andOrExpressionTail=Regex("(and)|(or)",RegexOption.IGNORE_CASE)
        private var isStringTypeHeader=Regex("^\\s+?\'")
        private var isSelectTypeHeader=Regex("^\\s+?select",RegexOption.IGNORE_CASE)
        init {
            var trimStatement=this.repairStatement(this.statement)
            root= CriteriaTreeNode(trimStatement)
            this.buildCriteria(root!!)
        }
        private fun getExpression(nodeCls: KClass<*>, statement:String, startIndex:Int, header:String):String{
            return when(nodeCls){
                FieldExpressionTreeNode::class,FieldLikeExpressionTreeNode::class->{
                   var subStr = StringBuffer()
                    subStr.append(header)
                    var iStartIndex=startIndex+header.length
                    var res=isStringTypeHeader.find(statement,iStartIndex)
                    if(res!=null){
                        subStr.append(res.value)
                        iStartIndex+=res.range.step
                        while (iStartIndex<statement.length){
                            if(statement[iStartIndex]!='\''){
                                subStr.append(statement[iStartIndex])
                            }
                            else if(iStartIndex<statement.length-1){
                                if(statement[iStartIndex+1]=='\''){
                                    subStr.append("\'\'")
                                    iStartIndex+=1
                                }
                                else{
                                    subStr.append("\'")
                                    break
                                }
                            }
                            else{
                                subStr.append("\'")
                                break
                            }
                            iStartIndex+=1
                        }
                    }
                    else{
                        var res2=andOrExpressionTail.find(statement,iStartIndex)
                        if(res2!=null){
                            var iEndIndex=iStartIndex+res2.range.step
                            subStr.append(statement.substring(iStartIndex,iEndIndex))
                        }
                        else{
                            subStr.append(statement.substring(iStartIndex))
                        }
                    }
                    subStr.toString()
                }
                FieldInExpressionTreeNode::class,ExistsExpressionTreeNode::class,SubExpressionTreeNode::class->{
                    var subStr=StringBuilder()
                    subStr.append(header)
                    var patternCount=1
                    var iStartIndex=startIndex+header.length
                    while (patternCount>0&&iStartIndex<statement.length){
                        if(!isStringValue(statement,iStartIndex)){
                            if(statement[iStartIndex]=='('){
                                patternCount++
                            }
                            else if(statement[iStartIndex]==')'){
                                patternCount--

                            }
                            subStr.append(statement[iStartIndex])
                            iStartIndex+=1
                        }
                        else{
                            iStartIndex=skipStringValue(statement,iStartIndex)
                        }
                    }
                    subStr.toString()
                }
                else->""
            }
        }
        private fun isStringValue(statement:String,startIndex:Int):Boolean{
            return isStringTypeHeader.find(statement,startIndex)!=null
        }
        private  fun skipStringValue(statement: String,startIndex: Int):Int{
            var iStartIndex=startIndex+1
            while (iStartIndex<statement.length){
                if(statement[iStartIndex]!='\''){
                    iStartIndex+=1
                }
                else if(iStartIndex<statement.length-1){
                    if(statement[iStartIndex+1]=='\''){
                        iStartIndex+=1
                    }
                    else{
                        iStartIndex+=1
                        break
                    }
                }
                else{
                    iStartIndex+=1
                    break
                }
            }
            return iStartIndex
        }
        private fun getAndOrExpression(statement: String,startIndex:Int):CriteriaTreeNode?{
            var res=andExressionHeader.find(statement,startIndex)
            if(res!=null){
                return AndCriteriaTreeNode(res.value)
            }
            res=orExpressionHeader.find(statement,startIndex)
            if(res!=null){
                return OrCriteriaTreeNode(res.value)
            }
            return null
        }
        private fun buildCriteria(parent:CriteriaTreeNode){
            parent.subNodes= parent.subNodes?:ArrayList<CriteriaTreeNode>()
            var startIndex=0
            val statement=this.repairStatement(parent.statement)
            while (statement.length>startIndex){
                var stateTreeNode:CriteriaTreeNode?=null
                var res=fieldExpressionHeader.find(statement,startIndex)
                if(res!=null){
                    stateTreeNode=FieldExpressionTreeNode(getExpression(FieldExpressionTreeNode::class,statement,startIndex,res.value))
                    stateTreeNode.fieldName=res.value.trim(' ','!','=','>','<')
                    stateTreeNode.fieldValue=stateTreeNode.statement.substring(res.value.length)
                    stateTreeNode.operator=Regex("[=,!,>,<]+").find(res.value)?.value!!
                }
                else{
                    res=fieldInExpressionHeader.find(statement,startIndex)
                    if(res!=null){
                        stateTreeNode=FieldInExpressionTreeNode(getExpression(FieldInExpressionTreeNode::class,statement,startIndex,res.value))
                    }
                    else
                    {
                        res=fieldLikeExpressionHeader.find(statement,startIndex)
                        if(res!=null){
                            stateTreeNode=FieldLikeExpressionTreeNode(getExpression(FieldLikeExpressionTreeNode::class,statement,startIndex,res.value))
                        }
                        else{
                            res=fieldExistsExpressionHeader.find(statement,startIndex)
                            if(res!=null){
                                stateTreeNode=ExistsExpressionTreeNode(getExpression(ExistsExpressionTreeNode::class,statement,startIndex,res.value))
                            }
                            else
                            {
                                res=fileSubExpressionHeader.find(statement,startIndex)
                                if(res!=null){
                                    stateTreeNode=SubExpressionTreeNode(getExpression(ExistsExpressionTreeNode::class,statement,startIndex,res.value))
                                }
                            }
                        }
                    }
                }

                if(stateTreeNode!=null){
                    parent.subNodes?.add(stateTreeNode)
                    startIndex+=stateTreeNode.statement.length
                    var exp=this.getAndOrExpression(statement,startIndex)
                    if(exp!=null){
                        startIndex+=exp.statement.length
                        parent.subNodes?.add(exp)
                    }
                    when(stateTreeNode){
                        is FieldInExpressionTreeNode->{
                            this.buildInExpression(stateTreeNode)
                        }
                        is ExistsExpressionTreeNode->{
                            this.buildExistsExpression(stateTreeNode)
                        }
                        is SubExpressionTreeNode->{
                            this.buildSubExpression(stateTreeNode)
                        }
                    }
                }
                else{
                    throw ModelErrorException("$statement 格式错误！")
                }
            }
            //return parent
        }
        private fun buildInExpression(parent: CriteriaTreeNode){
            var inTreeNode=parent as FieldInExpressionTreeNode
            var inIndex=inTreeNode.statement.indexOf("in",0,true)
            var notIndex=inTreeNode.statement.indexOf("not",0,true)
            if(notIndex<0 || notIndex>inIndex){
                inTreeNode.fieldName=inTreeNode.statement.substring(0,inIndex)
            }
            else{
                inTreeNode.fieldName=inTreeNode.statement.substring(0, notIndex)
                inTreeNode.isNotIn=true
            }
            var inSubStr=inTreeNode.statement.substringAfter("(")
            if(isSelectTypeHeader.find(inSubStr)!=null){
                parent.subNodes=parent.subNodes?: ArrayList()
                var select=inSubStr.substringBeforeLast(")")
                var selectExp=SelectExpressionTreeNode(select)
                parent.subNodes?.add(selectExp)
                this.buildSelect(selectExp)
            }else if(!inSubStr.contains("'")){
                inSubStr=inSubStr.trim('(',')')
                inTreeNode.inValues= arrayListOf(*inSubStr.split(',').toTypedArray())
            }
            else{
                inTreeNode.inValues= ArrayList()
                inSubStr=inSubStr.trim('(',')')
                var inValue=StringBuffer()
                var iStartIndex=1
                inValue.append('\'')
                while (iStartIndex<inSubStr.length){
                    var c=inSubStr[iStartIndex]
                    if(c!='\''){
                        inValue.append(c)
                    }
                    else if(iStartIndex<inSubStr.length-1){
                        iStartIndex+=1
                        if(inSubStr[iStartIndex]=='\''){
                            inValue.append(inSubStr[iStartIndex])
                        }
                        else if(inSubStr[iStartIndex]==','){
                            inTreeNode.inValues?.add(inValue.toString())
                            inValue= StringBuffer()
                            iStartIndex+=1
                            if(iStartIndex==inSubStr.length-1){
                                break
                            }
                            else{
                                inValue.append('\'')
                            }
                        }
                        else{
                            throw ModelErrorException("${inSubStr}格式错误")
                        }
                    }
                    else{
                        inValue.append(c)
                        inTreeNode.inValues?.add(inValue.toString())
                        break
                    }
                    iStartIndex+=1
                }
            }
        }
        private fun buildExistsExpression(parent:CriteriaTreeNode){
            var existsTreeNode=parent as ExistsExpressionTreeNode
            var existsIndex=existsTreeNode.statement.indexOf("exists",0,true)
            var notIndex=existsTreeNode.statement.indexOf("not",0,true)
            existsTreeNode.isNotExists = !(notIndex<0 || notIndex>existsIndex)
            var inSubStr=existsTreeNode.statement.substringAfter("(")
            parent.subNodes=parent.subNodes?: ArrayList()
            var select=inSubStr.substringBeforeLast(")")
            var selectExp=SelectExpressionTreeNode(select)
            parent.subNodes?.add(selectExp)
            this.buildSelect(selectExp)
        }
        private fun buildSubExpression(parent:CriteriaTreeNode){
            this.buildCriteria(parent)
        }
        private fun buildSelect(parent:CriteriaTreeNode){
            var selectStr=this.repairStatement(parent.statement)
            var selectRegex=Regex("^select\\s+(<selectFields>[0-9a-z\\.\\s,]+)\\s+from\\s+(<table>[0-9a-z\\.])\\s+(<joinTable>[\\s\\S]+)?\\s+where\\s+(<criteria>[\\s\\S]+)?$")
            var res=selectRegex.find(selectStr)
            if(res!=null){
                var selectTreeNode=parent as SelectExpressionTreeNode
                parent.subNodes=parent.subNodes?: ArrayList()
                var selectFields=res.groups["selectFields"]?.value
                var table=res.groups["table"]?.value
                var joinTable=res.groups["joinTable"]?.value
                var criteria=res.groups["criteria"]?.value
                selectTreeNode.fields= arrayListOf(*selectFields!!.split(',').toTypedArray())
                selectTreeNode.table=table
                if(criteria!=null){
                    var criteriaTreeNode=CriteriaTreeNode(criteria)
                    selectTreeNode.subNodes?.add(criteriaTreeNode)
                    this.buildCriteria(criteriaTreeNode)
                }
                if(joinTable!=null){
                    var regexJoin=Regex("\\s+?(left|right|inner)?\\s+join\\s+",RegexOption.IGNORE_CASE)
                    var matchs=regexJoin.findAll(joinTable).toList()
                    var joinModels=ArrayList<String>()
                    if(matchs.count()==1){
                        joinModels.add(joinTable)
                    }
                    else{
                        var iStartIndex=1
                        var iFromIndex=0
                        while (iStartIndex<matchs.count()){
                            var iToIndex=matchs[iStartIndex].range.start
                            joinModels.add(joinTable.substring(iFromIndex,iToIndex))
                            iFromIndex=iToIndex
                        }
                        var lastJoinModel=joinTable.substring(iFromIndex)
                        if(!lastJoinModel.isEmpty()){
                            joinModels.add(lastJoinModel)
                        }
                    }
                    for (jm in joinModels){
                        var joinExp=JoinExpressionTreeNode(jm)
                        selectTreeNode.subNodes?.add(joinExp)
                        this.buildJoinExpression(joinExp)
                    }
                }
            }
        }
        private  fun buildJoinExpression(parent:JoinExpressionTreeNode){
            var joinIndex=parent.statement.indexOf("join",0,true)
            parent.join=parent.statement.substring(0,joinIndex+4)
            var onIndex=parent.statement.indexOf(" on ",joinIndex+4)
            parent.table=parent.statement.substring(joinIndex+4,onIndex)
            var onCriteria=parent.statement.substring(onIndex+4)
            var criteria=CriteriaTreeNode(onCriteria)
            parent.subNodes=parent.subNodes?: ArrayList()
            parent.subNodes?.add(criteria)
            this.buildCriteria(criteria)
        }
        private fun getToken(statement:String):Pair<String,String>{
            return Pair(statement.substringBefore(CriteriaKey.SPACE),
                    statement.substringAfter(CriteriaKey.SPACE))
        }

        fun build(): dynamic.model.query.mq.ModelExpression?{


           return null
        }

        private fun repairStatement(statement:String):String{
            return trimBeginEndPairBracket(statement)
        }

        private fun trimBeginEndPairBracket(statement:String):String{
            var trimStatement=statement.trim()
            return if(trimStatement.startsWith(CriteriaKey.BRACKET_PREFIX)&&trimStatement.endsWith(CriteriaKey.BRACKET_SUFFIX)){
                trimStatement=trimStatement.substring(1,trimStatement.length-2)
                trimBeginEndPairBracket(trimStatement)
            }
            else{
                trimStatement
            }
        }
    }
}