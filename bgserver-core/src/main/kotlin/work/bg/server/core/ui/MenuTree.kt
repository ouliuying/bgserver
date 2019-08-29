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

package work.bg.server.core.ui

open class MenuNode(val app:String,val title:String?,val model:String?,val viewType:String?,val icon:String?){
    var children:ArrayList<MenuNode>?=null
    open fun createCopy():MenuNode{
        var mn=MenuNode(this.app,this.title,this.model,this.viewType,this.icon)
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

class MenuTree(app:String,val name:String, title:String?,icon:String?):MenuNode(app,title,null,null,icon) {
    override fun createCopy(): MenuTree {
        var mt=MenuTree(this.app,this.name,this.title,icon)
        mt.children=childrenCopy()
        return mt
    }
}