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
    fun toModelDataObjectArray():ArrayList<ModelDataObject>{
        var arr = arrayListOf<ModelDataObject>()
        data.forEach {
            arr.add(
                    ModelDataObject(it,this.model,this.fields)
            )
        }
        return arr
    }

    override fun isEmpty(): Boolean {
        return true
    }
    fun add(fvs:FieldValueArray){
        this.data.add(fvs)
    }
}