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
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.core.annotation.AnnotationUtils
import dynamic.model.web.spring.boot.annotation.Action
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod
//to do for test
class ModelTreeGraph constructor(val modelMetaDatas: List<ModelMetaData>): ModelGraph {
    private val logger = LoggerFactory.getLogger(ModelGraph::class.java)
    var root: ModelTreeGraphNode?=null
    override fun build(){
        this.root=this.buildGraphTree()
    }
    override fun createModelAction(): ModelAction?{
        var ams=this.createNodeAction(this.root!!)
        if(ams.count()>0){
            var ma= ModelAction()
            ams.forEach { t, u -> ma.addMethod(t,u)}
            return ma
        }
        return null
    }
    override fun getConcreteModelMetaDatas():List<ModelMetaData?> {
        var modelMetaDatas= mutableListOf<ModelMetaData?>()
        if (this.root!=null){
            visitGraphTree(this.root!!){
                modelMetaDatas.add(it.getModelMetaData())
            }
        }
        return modelMetaDatas
    }
    fun visitGraphTree(node: ModelTreeGraphNode, body:(n: ModelTreeGraphNode) -> Unit){
        body(node)
        node.getSubNodes().forEach { this.visitGraphTree(it,body) }
    }
    fun createNodeAction(node: ModelTreeGraphNode):Map<String, ActionMethod>{
        var ams= mutableMapOf<String, ActionMethod>()
        var subNodes=node.getSubNodes()
        subNodes.forEach {
            this.combineModelAction(ams,this.createNodeAction(it))
        }
        var mmd=node.getModelMetaData()
        if(mmd!=null){
            val mtyp=(mmd?.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass.kotlin
            mtyp.memberFunctions.forEach{
                if (it.isOpen){
                    var ann= AnnotationUtils.findAnnotation(it.javaMethod, Action::class.java)
                    if(ann!=null){
                        if(!ams.containsKey(ann.name)){
                            ams.put(ann.name, ActionMethod(it))
                        }
                    }
                }
            }
        }
        return ams
    }

    fun combineModelAction(targetAms: MutableMap<String, ActionMethod>, sourceAms:Map<String, ActionMethod>){
        sourceAms.forEach { t, u ->
            if (targetAms.containsKey(t)){
                logger.warn("model action name ["+t+"] conflict!")
            }else{
                targetAms.put(t,u)
            }
        }
    }

    private  fun buildGraphTree(): ModelTreeGraphNode?{
        var nodes=this.buildAllGraphNodes()
        nodes.forEach root@{
            if (it.isRoot()){
                this.root=it
                return@root
            }
        }
        
        if(this.root!=null){
            nodes=nodes.filter {
                it!=this.root!!
            }
            fillSubNodes(this.root,nodes.toMutableList())
        }
        return this.root
    }
    private  fun buildAllGraphNodes():List<ModelTreeGraphNode>{
        var nodes= mutableListOf<ModelTreeGraphNode>()
        this.modelMetaDatas.forEach mm@{
            var hasCell=false
            var mmd=it
            nodes.forEach gn@ {
                if(gn@it.setModelMetaData(mm@mmd)){
                    hasCell=true
                }
            }
            if(!hasCell){
                var node= ModelTreeGraphNode(null)
                node.setModelMetaData(mm@mmd)
                nodes.add(node)
            }
        }
        return nodes
    }


    private fun fillSubNodes(parent: ModelTreeGraphNode?, nodes:MutableList<ModelTreeGraphNode>){
        var subNodes= mutableListOf<ModelTreeGraphNode>()
        nodes.forEach{
            if (it.isDependentedNode(parent!!)){
                parent?.addSubNode(it)
                subNodes.add(it)
            }
        }
        nodes.removeAll(subNodes)
        subNodes.forEach {
            fillSubNodes(it,nodes)
        }
    }
}
