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

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.junit.Test

class MD5Test : StringSpec({
    var mdHash=MD5.hash("123456")
    mdHash shouldBe "e10adc3949ba59abbe56e057f20f883e"
})


class MD5Test2{

    @Test
    fun test(){
        var mdHash=MD5.hash("123456")
        mdHash shouldBe "e10adc3949ba59abbe56e057f20f883e"
    }
}
