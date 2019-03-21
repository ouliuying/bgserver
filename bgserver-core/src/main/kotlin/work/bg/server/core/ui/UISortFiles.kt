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