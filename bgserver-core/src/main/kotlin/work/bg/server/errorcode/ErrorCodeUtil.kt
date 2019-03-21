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

package work.bg.server.errorcode
fun ErrorCode.jsonFormat():Map<String,Any?> {
    var map= mutableMapOf<String,Any?>()
    map.put("errorCode",this.code)
    map.put("errorMsg",this.description)
    return map
}

fun ErrorCode.reLogin(redirectUrl:String?):Map<String,Any?>{
   // return String.format("""{"errorCode":${this.code},"errorMsg":"${this.description}","redirectURL":"${redirectUrl}"}""")
    var map= mutableMapOf<String,Any?>()
    map.put("errorCode",this.code)
    map.put("errorMsg",this.description)
    map.put("redirectURL",redirectUrl)
    return map
}

fun ErrorCode.jsonFormat(extraData:Map<String,Any?>):Map<String,Any?>{
    var map= mutableMapOf<String,Any?>()
    map.put("errorCode",this.code)
    map.put("errorMsg",this.description)
    extraData.forEach { t, u -> map.put(t,u) }
    return map
}

fun ErrorCode.jsonFormat(kv:Pair<String,Any?>):Map<String,Any?>{
    var map= mutableMapOf<String,Any?>()
    map.put("errorCode",this.code)
    map.put("errorMsg",this.description)
    map.put(kv.first,kv.second)
    return map
}


