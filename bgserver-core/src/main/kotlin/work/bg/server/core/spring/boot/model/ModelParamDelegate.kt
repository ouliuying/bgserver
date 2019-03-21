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

import kotlin.reflect.KProperty

class  ModelParamDelegate<out T> constructor(val data: Map<String,Any?>){
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        try {
            return this.data[property.name] as T
        }
        catch (err: Exception){

        }
        return null
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Any?) {
        throw Exception()
    }
}