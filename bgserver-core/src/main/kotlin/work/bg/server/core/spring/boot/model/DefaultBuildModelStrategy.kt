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

package work.bg.server.core.spring.boot.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.ui.Model
import work.bg.server.core.exception.ModelErrorException
import work.bg.server.core.spring.boot.annotation.Action
import kotlin.math.sin
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
class  DefaultBuildModelStrategy: BuildModelStrategy{
    private val logger = LoggerFactory.getLogger(DefaultBuildModelStrategy::class.java)
    override  fun buildModel(modelMetaDatas: List<ModelMetaData>):  Pair<Map<String,ModelAction?>,List<ModelMetaData?>>
    {
        val modelActions = mutableMapOf<String,ModelAction?>()
        val concreteModelMetaDatas= mutableListOf<ModelMetaData?>()
        val models=this.sortModel(modelMetaDatas)
        models.forEach {
            try {
                val modelGraph=ModelLineGraph(it.value)
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