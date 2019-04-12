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

import work.bg.server.core.cache.PartnerCache

open class FunctionField<T>(model:ModelBase?,
                            val comp:((FieldValueArray,PartnerCache,Any?)->T?)?,
                            val inv:((FieldValueArray, PartnerCache, T?, Any?)->Unit)?,
                            name:String,
                            fieldType:FieldType,
                            title:String?,
                            open val depFields: Array<FieldBase?>):FieldBase(name,title,fieldType,model) {
    open  fun  compute(fieldValueArray:FieldValueArray,partnerCache: PartnerCache,data:Any?):T?{
            return this.comp?.invoke(fieldValueArray,partnerCache,data)
    }

    open fun  inverse(fieldValueArray:FieldValueArray, partnerCache: PartnerCache, value:T?, data:Any?){
       this.inv?.invoke(fieldValueArray,partnerCache,value,data)
    }
}