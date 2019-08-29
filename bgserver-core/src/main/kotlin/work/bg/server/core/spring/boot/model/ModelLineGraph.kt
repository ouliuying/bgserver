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

package work.bg.server.core.spring.boot.model

import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.core.annotation.AnnotationUtils
import work.bg.server.core.spring.boot.annotation.Action
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

class ModelLineGraph(val modelMetaDatas: List<ModelMetaData>):ModelGraph {
    var baseNode:ModelLineGraphNode?=null
    override fun build() {
        this.baseNode=this.buildInheritedLine(this.modelMetaDatas)
    }
    private fun buildInheritedLine(modelMetaDatas: List<ModelMetaData>):ModelLineGraphNode?{
        var node=null as ModelLineGraphNode?
        modelMetaDatas.forEach {
            if(node!=null){
                var childNode=ModelLineGraphNode()
                childNode.modelMetaData=it
                node=node?.addChildNode(childNode)
            }
            else
            {
                node= ModelLineGraphNode()
                node?.modelMetaData=it
            }
        }
        return node
    }
    override fun createModelAction(): ModelAction? {
        var mmLst=this.getConcreteModelMetaDatas()
        var ma=ModelAction()
        mmLst.forEach {
            var ams=this.createNodeAction(it!!)
            if(ams.count()>0){
                ams.forEach { t, u -> ma.addMethod(t,u)}
            }
        }
        return ma
    }
    private fun createNodeAction(modelMetaData: ModelMetaData):Map<String,ActionMethod>{
        var ams= mutableMapOf<String,ActionMethod>()
        var mmd=modelMetaData
        if(mmd!=null){
            val mTyp=(mmd?.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin
            mTyp.memberFunctions.forEach{
                if (it.isOpen){
                    var ann= AnnotationUtils.findAnnotation(it.javaMethod, Action::class.java)
                    if(ann!=null){
                        if(!ams.containsKey(ann.name)){
                            var am= ActionMethod(it)
                            am.name=am.name

                            ams[ann.name] =am
                        }
                        else{
                            var am=ams[ann.name]!!

                        }
                    }
                }
            }
        }
        return ams
    }
    override fun getConcreteModelMetaDatas(): List<ModelMetaData?> {
        var mmLst= mutableListOf<ModelMetaData?>()
        var node=this.baseNode
        while (node!=null){
            mmLst.add(node.modelMetaData)
            node=node.childNode
        }
        return mmLst.asReversed<ModelMetaData?>()
    }
}