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


