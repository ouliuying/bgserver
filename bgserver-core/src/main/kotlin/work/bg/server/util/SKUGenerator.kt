/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.util
import java.lang.StringBuilder
import java.util.*
import java.util.regex.Pattern

object SKUGenerator {
    private val azs = arrayOf('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z')
    private val AZS = arrayOf('A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z')
    private val alphaNums = arrayOf('1','2','3','4','5','6','7','8','9','0')
    private val rnd = Random(Date().time)
    private val p:Pattern = Regex("(\\[[0-9a-z-A-Z]\\])").toPattern()
    fun newID(pattern:String):String{
        val m = p.matcher(pattern)
        var sb:StringBuilder = StringBuilder()
        var fromIndex = 0
        while (m.find()){
            val startIndex = m.start()
            val endIndex = m.end()
            val header = pattern.substring(fromIndex, startIndex)
            val m = pattern.substring(startIndex, endIndex)
            sb.append(header)
            sb.append(getReal(m[1]))
            fromIndex = endIndex
        }
        if(fromIndex < pattern.length){
            sb.append(pattern.substring(fromIndex,pattern.length))
        }
        return sb.toString()
    }
    fun getReal(d:Char):Char{
        return when (d) {
            'w' -> {
                azs[rnd.nextInt(26)]
            }
            'W' -> {
                AZS[rnd.nextInt(26)]
            }
            else -> {
                alphaNums[rnd.nextInt(10)]
            }
        }
    }
}