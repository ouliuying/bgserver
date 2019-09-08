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

import dynamic.model.query.exception.ModelErrorException
import dynamic.model.query.mq.model.ModelMetaData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.GenericBeanDefinition
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
//for test
class ModelTreeBuildModelStrategy: BuildModelStrategy {
    private val logger = LoggerFactory.getLogger(ModelTreeBuildModelStrategy::class.java)
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
                concreteModelMetaDatas.addAll(modelGraph.getConcreteModelMetaDatas())
            }
            catch (err: Exception)
            {
                logger.error(err.toString())
            }
        }
        return Pair(modelActions,concreteModelMetaDatas)
    }
    private  fun buildModelAction(modelMetaDatas: List<ModelMetaData>): ModelAction {
        val topModelMetaDatas=modelMetaDatas.filter{this.isTopModel(it,modelMetaDatas)}
        val orderModelMetaDatas=this.orderTopModels(topModelMetaDatas)
        return this.createModelAction(orderModelMetaDatas)
    }

    private fun createModelAction(modelMetaDatas: List<ModelMetaData>): ModelAction {
        val ma = ModelAction()
        modelMetaDatas.forEach  {
            val mmd=it
            //it.modelCls.memberFunctions.forEach { this.addActionMethod(mmd,it,ma) }
            this.addActionMethod(it,ma)
        }
        return ma
    }

    private  fun addActionMethod(modelMetaData: ModelMetaData, ma: ModelAction){
        //        val ann=kf.findAnnotation<work.bg.server.core.spring.boot.annotation.Action>()
        //        if(ann!=null){
        //            ma.addMethod(ann.name, ActionMethod(kf))
        //        }
        val ams=this.collectModelAction(modelMetaData)
        ams.forEach{
            ma.addMethod(it.key,it.value)
        }
    }
    private  fun collectModelAction(modelMetaData: ModelMetaData): Map<String, ActionMethod>
    {
        val ams= HashMap<String, ActionMethod>()
        val clsLst= mutableListOf<KClass<*>>()
        // clsLst.add(modelMetaData.modelCls)
        orderSingleModelClass((modelMetaData.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin,clsLst)
        clsLst.reverse()

        clsLst.forEach {
            this.collectModelActionImp(it,ams)
        }
        return ams
    }
    private  fun collectModelActionImp(cls: KClass<*>, ams: HashMap<String, ActionMethod>)
    {
        cls.memberFunctions.forEach {
            val (isSignal,name) = this.isSignalModelAction(it)
            if (isSignal) {
                ams[name] = ActionMethod(it)
            }
            else
            {
                for ((k,v) in ams){
                    if (this.isSameActionMethod(it,v)){
                        ams[k]= ActionMethod(it)
                        break
                    }
                }
            }
        }
    }

    private  fun isSameActionMethod(kf: KFunction<*>, am: ActionMethod):Boolean{
        if (kf.name==am.method.name && kf.returnType==am.method.returnType){
            if(kf.parameters.count() == am.method.parameters.count()){
                val size=kf.parameters.count()
                for (i in 1..size){
                    if(kf.parameters[i].type!=am.method.parameters[i].type){
                        return false
                    }
                }
                return true
            }
        }
        return false
    }

    private  fun isSignalModelAction(kf: KFunction<*>): Pair<Boolean,String>
    {
        val ann=kf.findAnnotation<Action>()
        if(ann!=null){
            return Pair(true,ann.name)
        }
        return Pair(false,"")
    }
    private  fun orderSingleModelClass(cls: KClass<*>, inheritedCls: MutableList<KClass<*>>){
        inheritedCls.add(cls)
        cls.superclasses.forEach {
            val ann=it.findAnnotation<Model>()
            if (ann!=null){
                orderSingleModelClass(it,inheritedCls)
                return
            }
        }
    }
    /**
     * Be dependent on  model is in front of list
     */
    private  fun orderTopModels(topModelMetaDatas: List<ModelMetaData>): List<ModelMetaData>{
        val orderModelMetaDatas= mutableListOf<ModelMetaData>()
        var tTopModelMetaDatas=topModelMetaDatas.toMutableList<ModelMetaData>()
        do {
            val model=tTopModelMetaDatas.firstOrNull { !this.isDependentModel(it,tTopModelMetaDatas) }
            if(model!=null)
            {
                orderModelMetaDatas.add(model)
                tTopModelMetaDatas=tTopModelMetaDatas.filter { this.isNotSameModelModeMetaData(it,model)}.toMutableList()
            }
            else{
                throw ModelErrorException("model hierarchical incorrect")
            }
        }while (tTopModelMetaDatas.count()>0)
        // orderModelMetaDatas.reverse()
        return orderModelMetaDatas
    }
    private  fun isNotSameModelModeMetaData(mmd: ModelMetaData, targetMmd: ModelMetaData): Boolean{
        return mmd!=targetMmd
    }
    private  fun getModelTypeDependent(modelKCls: KClass<*>): KClass<*> {
        val ann=modelKCls.findAnnotation<Model>()
        if (ann!=null){
            return ann.dependentType
        }
        modelKCls.allSuperclasses.forEach{
            val ann2=it.findAnnotation<Model>()
            if(ann2!=null)
            {
                return ann2.dependentType
            }
        }
        return Any::class
    }
    private  fun isDependentModelType(kc: KClass<*>, dependentTarget: KClass<*>): Boolean{
        if(kc==dependentTarget){
            return false
        }
        val dKCls=this.getModelTypeDependent(kc)

        if(dKCls==dependentTarget)
        {
            return true
        }

        val allSuperClasses=dependentTarget.allSuperclasses
        allSuperClasses.forEach{
            if(dKCls==it){
                return true
            }
        }
        return false
    }
    private  fun isDependentModel(mmd: ModelMetaData, topModelMetaDatas: List<ModelMetaData>): Boolean
    {
        // var dependentFlag=false
        topModelMetaDatas.forEach {
            if(this.isDependentModelType((mmd.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin,(it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin)){
                return true
            }
        }
        return false
    }
    private  fun isTopModel(modelMetaData: ModelMetaData, modelMetaDatas: List<ModelMetaData>): Boolean{

        modelMetaDatas.forEach {
            if((it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin.isSubclassOf((modelMetaData.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin) &&
                    (modelMetaData.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin!=(it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin)
            {
                return false
            }
        }
        return true
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