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

class ModelDataArray(override var data:ArrayList<FieldValueArray> = arrayListOf(),
                     model:ModelBase?=null,fields:ArrayList<FieldBase>?=null):ModelData(data,model,fields) {
    override  fun isArray(): Boolean {
        return true
    }
    fun firstOrNull():ModelDataObject?{
        if(this.data.count()>0){
            return ModelDataObject(this.data.first(),this.model,this.fields)
        }
        return null
    }
}