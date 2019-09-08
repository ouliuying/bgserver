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
import org.springframework.beans.factory.support.GenericBeanDefinition
import dynamic.model.web.spring.boot.annotation.Model
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
class ModelTreeGraphNode constructor(val parent: ModelTreeGraphNode?){
    private var subNodes: MutableList<ModelTreeGraphNode> = mutableListOf()
    private var modelMetaData: ModelMetaData?=null
    private var modelMetaDataCache = mutableListOf<ModelMetaData>()
    private  var dependentClass: KClass<*>?=null
    fun addSubNode(node: ModelTreeGraphNode){
        this.subNodes.add(node)
    }
    fun getSubNodes():List<ModelTreeGraphNode>{
        return this.subNodes
    }
    private  fun isSameModel(mmd: ModelMetaData): Boolean{
        
        return this.isSubModel(mmd)||this.isSuperModel(mmd)
    }
    private fun isSuperModel(mmd: ModelMetaData): Boolean{
       if(this.modelMetaData!=null) {
           return (this.modelMetaData?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition).beanClass::class.
                   isSubclassOf((mmd.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass::class)
       }
        return false
    }

    private  fun isSubModel(mmd: ModelMetaData): Boolean{
        if(this.modelMetaData!=null){
            return (this.modelMetaData?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition).beanClass::class.
                    isSuperclassOf((mmd.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass::class)
        }
        return false
    }

    private  fun getDependentModel(): KClass<*>?{
        if(this.dependentClass!=null){
            return this.dependentClass
        }else{
            this.modelMetaDataCache.forEach mmc@ {
                var ann=(it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin.findAnnotation<Model>()
                if (ann!=null){
                    this.dependentClass=ann.dependentType
                    return@mmc
                }
            }
            return this.dependentClass
        }
    }
    fun setModelMetaData(mmd: ModelMetaData): Boolean{
        if(this.modelMetaData!=null){
            if(this.isSameModel(mmd)){
                if (this.isSubModel(mmd)){
                    this.modelMetaData=mmd
                }
                modelMetaDataCache.add(mmd)
                return true
            }
        }else{
            this.modelMetaData=mmd
            modelMetaDataCache.add(mmd)
            return true
        }
        return false
    }
    fun getModelMetaData(): ModelMetaData?{
        return this.modelMetaData
    }
    fun isRoot(): Boolean{
        var dep=this.getDependentModel()
        return dep==Any::class
    }
    fun containModel(model:KClass<*>):Boolean{
         this.modelMetaDataCache.forEach {

             if ((it.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin.qualifiedName==
                     model.qualifiedName)
             {
                 return true
             }
         }
        return false
    }
    fun isDependentedNode(node: ModelTreeGraphNode): Boolean{
        var dep=this.getDependentModel()
        if(dep==Any::class){
            return false
        }
        return node.containModel(dep!!)
    }
}