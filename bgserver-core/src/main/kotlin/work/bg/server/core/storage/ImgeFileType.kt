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

package work.bg.server.core.storage

import org.springframework.stereotype.Component

@Component
class ImgeFileType:FileType {
    override val filter: Regex
        get() = Regex(".+\\.((png)|(jpeg)|(jpg)|(gif))$",RegexOption.IGNORE_CASE)
    override val isTransient: Int
        get() = 0
    override val title: String
        get() = "图片"
    override val typ: String
        get() = "image"

    override fun process(file: String): Any? {
        return null
    }
}