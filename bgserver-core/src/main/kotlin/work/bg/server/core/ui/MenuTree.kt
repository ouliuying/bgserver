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

package work.bg.server.core.ui

open class MenuNode(val app:String,val title:String?,val model:String?,val viewType:String?){
    var children:ArrayList<MenuNode>?=null
    open fun createCopy():MenuNode{
        var mn=MenuNode(this.app,this.title,this.model,this.viewType)
        mn.children=this.childrenCopy()
        return mn
    }
    open fun childrenCopy():ArrayList<MenuNode>?{
        if(children==null){
            return null
        }
        var cpyChildren= arrayListOf<MenuNode>()
        children?.forEach {
            cpyChildren.add(it.createCopy())
        }
        return cpyChildren
    }
}

class MenuTree(app:String,val name:String, title:String?):MenuNode(app,title,null,null) {
    override fun createCopy(): MenuTree {
        var mt=MenuTree(this.app,this.name,this.title)
        mt.children=childrenCopy()
        return mt
    }
}