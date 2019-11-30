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

package work.bg.server.expression

import kotlin.math.exp

open class AtomRuleCriteriaScanner {
    companion object {
        const val TEST_AND = " and "
        const val TEST_OR = " or "
        const val OPERATOR_AND = "and"
        const val OPERATOR_OR = "or"
    }


   open fun scan(expression: String): RawExpressionNode? {
        return this.buildExpressionTree(expression)
    }

    private fun buildExpressionTree(expression: String): RawExpressionNode? {
        var index = 0
        var tempExp = expression.replace('（', '(')
        tempExp = tempExp.replace('）', ')')
        val len = tempExp.length
        if (len < 1) {
            return null
        }
        var isSubExpression = false
        var expItem = StringBuffer()
        var rawExpNodeRoot = RawExpressionNode()
        var bracketTest = 0
        var isString = false
        do {
            if(!isString){
                val test_c = tempExp[index]
                if(test_c=='\''){
                    isString = true
                    expItem.append(test_c)
                    index++
                    continue
                }
                if (!isSubExpression) {
                    if (test_c != '(') {
                        when {
                            tempExp.startsWith(TEST_AND, index, true) -> {
                                when {
                                    rawExpNodeRoot.operator == "" -> {
                                        rawExpNodeRoot.operator = OPERATOR_AND
                                        val item = RawExpressionNode()
                                        item.expression = expItem.toString().trim()
                                        rawExpNodeRoot.children.add(item)
                                    }
                                    rawExpNodeRoot.operator != OPERATOR_AND -> {

                                        val item = RawExpressionNode()
                                        item.expression = expItem.toString().trim()
                                        rawExpNodeRoot.children.add(item)

                                        val orRawExpNodeRoot = RawExpressionNode()
                                        orRawExpNodeRoot.operator = OPERATOR_OR
                                        orRawExpNodeRoot.children.add(rawExpNodeRoot)
                                        rawExpNodeRoot = orRawExpNodeRoot
                                    }
                                    else -> {
                                        val item = RawExpressionNode()
                                        item.expression = expItem.toString().trim()
                                        rawExpNodeRoot.children.add(item)
                                    }
                                }
                                expItem = StringBuffer()
                                index += TEST_AND.length
                            }
                            tempExp.startsWith(TEST_OR, index, true) -> {
                                when {
                                    rawExpNodeRoot.operator == "" -> {
                                        rawExpNodeRoot.operator = OPERATOR_OR
                                        val item = RawExpressionNode()
                                        item.expression = expItem.toString()
                                        rawExpNodeRoot.children.add(item)
                                    }
                                    rawExpNodeRoot.operator != OPERATOR_OR -> {

                                        val item = RawExpressionNode()
                                        item.expression = expItem.toString()
                                        rawExpNodeRoot.children.add(item)

                                        val andRawExpNodeRoot = RawExpressionNode()
                                        andRawExpNodeRoot.operator = OPERATOR_AND
                                        andRawExpNodeRoot.children.add(rawExpNodeRoot)
                                        rawExpNodeRoot = andRawExpNodeRoot
                                    }
                                    else -> {
                                        val item = RawExpressionNode()
                                        item.expression = expItem.toString()
                                        rawExpNodeRoot.children.add(item)
                                    }
                                }
                                expItem = StringBuffer()
                                index += TEST_OR.length
                            }
                            else -> {
                                expItem.append(test_c)
                                index++
                            }
                        }
                    } else {
                        val item = RawExpressionNode()
                        item.expression = expItem.toString()
                        rawExpNodeRoot.children.add(item)
                        isSubExpression = true
                        index++
                        expItem = StringBuffer()
                        bracketTest++
                    }
                } else{
                    if (test_c == ')') {
                        bracketTest--
                        if (bracketTest > 0) {
                            expItem.append(test_c)
                        } else {
                            isSubExpression = false
                            bracketTest = 0
                            val expNode = this.buildExpressionTree(expItem.toString())
                            expNode?.let {
                                rawExpNodeRoot.children.add(expNode)
                            }
                            expItem = StringBuffer()
                        }
                    } else {
                        if (test_c == '(') {
                            bracketTest++
                        }
                        expItem.append(test_c)
                    }
                    index++
                }
            }
            else{
                if(tempExp[index]!='\''){
                    expItem.append(tempExp[index])
                }
                else{
                    if(index<1){
                        return null
                    }
                    else if(tempExp[index-1]!='\\') {
                        isString = false
                    }
                    expItem.append(tempExp[index])
                }
                index++
            }
        } while (index < len)

        if(expItem.isNotEmpty() && expItem.isNotBlank()){
            val item = RawExpressionNode()
            item.expression = expItem.toString()
            rawExpNodeRoot.children.add(item)
        }

        return rawExpNodeRoot
    }

    open class RawExpressionNode {
        var expression: String = ""
        var operator: String = ""
        var children: ArrayList<RawExpressionNode> = arrayListOf()
    }
}