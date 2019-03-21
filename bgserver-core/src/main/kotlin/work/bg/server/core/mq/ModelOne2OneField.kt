

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

open class ModelOne2OneField constructor(model:ModelBase?,
                                         name:String,
                                         fieldType:FieldType,
                                         title:String?,
                                         length:Int?=null,
                                         override val isVirtualField:Boolean=false,
                                         override val targetModelTable: String?=null,
                                         override val targetModelFieldName: String?=null,
                                         override val foreignKey: FieldForeignKey?=null
):ModelField(model,name,fieldType,title,length),One2OneField{


}