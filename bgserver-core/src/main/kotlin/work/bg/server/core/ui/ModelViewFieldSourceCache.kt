/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.core.ui

import util.MethodInvocation
import work.bg.server.core.spring.boot.model.AppModel



//TODO cache instance method,call method with parameters
object ModelViewFieldSourceCache {
    fun run(source:ModelViewFieldSource):Any?{
        var model = AppModel.ref.getModel(source.app,source.model)
        model?.let {
            return MethodInvocation(model,source.method)()
        }
        return null
    }
}