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

package work.bg.server.core.acrule

import work.bg.server.core.cache.CorpCache
import work.bg.server.core.cache.CorpPartnerRoleCache
import work.bg.server.core.ui.MenuTree
import work.bg.server.core.ui.ModelView
import work.bg.server.core.ui.ViewAction

class ACUIFilter {
    companion object {
        //todo add access control
        fun scanMenuTree(menuTrees: Map<String,MenuTree>,corp:CorpCache,role:CorpPartnerRoleCache):Map<String,MenuTree>{
            return menuTrees
        }

        //todo add access control
        fun scanViewAction(actions: Map<String,ViewAction>,corp:CorpCache,role:CorpPartnerRoleCache):Map<String,ViewAction>{
            return actions
        }

        private fun  getPropertyName(name:String):Pair<String,String?>{
            var nameItems=name.split('.')
            return if(nameItems.count()==1){
                Pair(nameItems[0],null)
            } else{
                Pair(nameItems[0],nameItems[1])
            }
        }

        //todo add access control

        fun scanModelView(views:Map<String,ModelView>,corp:CorpCache,role:CorpPartnerRoleCache):Map<String,ModelView>{

            return views
        }
    }
}