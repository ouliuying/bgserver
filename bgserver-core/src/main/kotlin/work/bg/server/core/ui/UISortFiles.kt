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

import org.dom4j.Element

class UISortFiles(private var uiFileInheriteSeq:ArrayList<UIFile> = arrayListOf()) {
    fun add(uiFile:UIFile){
        this.uiFileInheriteSeq.add(uiFile)
        var inheritEls=uiFile.doc.selectNodes("ui/inherit/xpath") as List<Element?>?
        inheritEls?.forEach {
            var appName=it?.attribute("app")?.value
            if(appName!=null && appName.isNullOrEmpty()){
                uiFile.dependsOnApps.add(appName)
            }
        }
        uiFile.dependsOnApps= arrayListOf(*uiFile.dependsOnApps.distinct().toTypedArray())
    }

    fun sort():List<UIFile>{
        return uiFileInheriteSeq.sortedWith(comparator = Comparator<UIFile> { o1, o2 ->
            when {
                o1.dependsOnApps.contains(o2.appName) -> 1
                o2.dependsOnApps.contains(o1.appName) -> -1
                else -> 0
            }
        })
    }
}