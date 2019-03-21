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

package util

import java.security.CryptoPrimitive
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