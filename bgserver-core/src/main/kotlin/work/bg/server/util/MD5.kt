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

package work.bg.server.util

import java.security.MessageDigest

class MD5{
    companion object {
        fun hash(data:String):String{
            val mdEnc = MessageDigest.getInstance("MD5")
            var sb=StringBuilder()
            var md5Bytes=mdEnc.digest(data.toByteArray())
            md5Bytes.forEach {
                var bt=it.toUByte()
                var padVal=bt.toString(16).padStart(2,'0').toLowerCase()
                sb.append(padVal)
            }
            return sb.toString()
        }
    }
}