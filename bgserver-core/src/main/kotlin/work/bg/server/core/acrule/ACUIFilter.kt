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

package work.bg.server.core.acrule

import work.bg.server.core.cache.CorpCache
import work.bg.server.core.cache.CorpPartnerRoleCache
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.model.AppModel
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