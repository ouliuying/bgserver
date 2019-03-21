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

import org.springframework.beans.factory.support.GenericBeanDefinition
import work.bg.server.core.spring.boot.annotation.Model
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
class ModelTreeGraphNode constructor(val parent: ModelTreeGraphNode?){
    private var subNodes: MutableList<ModelTreeGraphNode> = mutableListOf()
    private var modelMetaData:ModelMetaData?=null
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

    private  fun isSubModel(mmd:ModelMetaData): Boolean{
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