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
import kotlin.reflect.full.isSuperclassOf

class ModelLineGraphNode {
    var childNode:ModelLineGraphNode?=null
    var modelMetaData:ModelMetaData?=null
    fun addChildNode(childNode:ModelLineGraphNode,parentNode:ModelLineGraphNode?=null):ModelLineGraphNode{
        if(this.isChild(childNode)){
            if(this.childNode!=null){
                this.childNode?.addChildNode(childNode,this)
            }else{
                this.childNode=childNode
            }
        }
        else{
            if(parentNode==null){
                childNode.childNode=this
                return childNode
            }
            else{
                parentNode.childNode=childNode
                childNode.childNode=this
            }
        }
        return this
    }
    private fun isChild(childNode:ModelLineGraphNode):Boolean{
        return (this.modelMetaData?.beanDefinitionHolder as GenericBeanDefinition).beanClass::class.
                isSuperclassOf((childNode.modelMetaData?.beanDefinitionHolder as GenericBeanDefinition).beanClass::class)
        //return false
    }

}