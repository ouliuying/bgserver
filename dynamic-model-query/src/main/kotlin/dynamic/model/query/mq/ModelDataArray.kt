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

package dynamic.model.query.mq

import dynamic.model.query.mq.model.ModelBase

class ModelDataArray(override var data:ArrayList<dynamic.model.query.mq.FieldValueArray> = arrayListOf(),
                     model: ModelBase?=null, fields:ArrayList<dynamic.model.query.mq.FieldBase>?=null): dynamic.model.query.mq.ModelData(data,model,fields) {
    override  fun isArray(): Boolean {
        return true
    }
    fun firstOrNull(): dynamic.model.query.mq.ModelDataObject?{
        if(this.data.count()>0){
            return dynamic.model.query.mq.ModelDataObject(this.data.first(), this.model, this.fields)
        }
        return null
    }
    fun toModelDataObjectArray():ArrayList<dynamic.model.query.mq.ModelDataObject>{
        var arr = arrayListOf<dynamic.model.query.mq.ModelDataObject>()
        data.forEach {
            arr.add(
                    dynamic.model.query.mq.ModelDataObject(it, this.model, this.fields)
            )
        }
        return arr
    }

    override fun isEmpty(): Boolean {
        return true
    }
    fun add(fvs: dynamic.model.query.mq.FieldValueArray){
        this.data.add(fvs)
    }
}