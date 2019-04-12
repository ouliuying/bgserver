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

open class  ModelField constructor(model:ModelBase?,
                                   name:String,
                                   fieldType:FieldType,
                                   title:String?=null,
                                   length:Int?=null,
                                   val defaultValue:Any?=null,
                                   val index:ArrayList<FieldIndex>?=null,
                                   val primaryKey:FieldPrimaryKey?=null,
                                   val comment:String?=null):BinaryOperatorField(null,null,"",name,title,fieldType,model,length){
      init {

      }
//
//      override fun `as`(alias: String?): ModelField{
//            return ModelField(this.model,
//                    this.name,
//                    alias ?: this.alias,
//                    this.fieldType,
//                    this.title,
//                    this.length,
//                    this.defaultValue,
//                    this.index,
//                    this.primaryKey,
//                    this.comment)
//      }

      override fun accept(visitor: ModelExpressionVisitor,parent:ModelExpression?): Boolean {
          visitor.visit(this,parent)
          return true
      }

      override fun render(parent: ModelExpression?): Pair<String, Map<String, Any?>>? {
          return null
      }
}