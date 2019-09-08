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

package dynamic.model.web.spring.boot.model

import dynamic.model.query.mq.model.ModelMetaData
import org.slf4j.LoggerFactory

class  DefaultBuildModelStrategy: BuildModelStrategy {
    private val logger = LoggerFactory.getLogger(DefaultBuildModelStrategy::class.java)
    override  fun buildModel(modelMetaDatas: List<ModelMetaData>):  Pair<Map<String, ModelAction?>,List<ModelMetaData?>>
    {
        val modelActions = mutableMapOf<String, ModelAction?>()
        val concreteModelMetaDatas= mutableListOf<ModelMetaData?>()
        val models=this.sortModel(modelMetaDatas)
        models.forEach {
            try {
                val modelGraph= ModelLineGraph(it.value)
                modelGraph.build()
                val mAc=modelGraph.createModelAction()
                modelActions[it.key] = mAc
                concreteModelMetaDatas.add(modelGraph.getConcreteModelMetaDatas().first())
            }
            catch (err: Exception)
            {
                logger.error(err.toString())
            }
        }
        return Pair(modelActions,concreteModelMetaDatas)
    }
    private fun sortModel(modelMetaDatas: List<ModelMetaData>): Map<String,List<ModelMetaData>>{
        val models=HashMap<String,MutableList<ModelMetaData>>()
        modelMetaDatas.forEach{
            if (models.containsKey(it.modelName)){
                models[it.modelName]?.add(it)
            }
            else
            {
                val singleModelMetas = arrayListOf<ModelMetaData>()
                singleModelMetas.add(it)
                models[it.modelName]=singleModelMetas
            }
        }
        return models
    }
}