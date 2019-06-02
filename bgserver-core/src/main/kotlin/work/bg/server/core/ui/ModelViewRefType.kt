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

class ModelViewRefType {
    companion object {
        const val None = "none"
        const val Main = "main"
        const val Sub = "sub"
        const val Embedded = "embedded"
        const val SingleSelection = "singleSelection"
        val allRefTypes:ArrayList<String>
        get() = arrayListOf(Main, Sub, Embedded, SingleSelection)
    }
}