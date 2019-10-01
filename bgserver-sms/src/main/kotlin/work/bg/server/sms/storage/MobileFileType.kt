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

package work.bg.server.sms.storage


import com.google.gson.JsonObject
import org.apache.any23.encoding.TikaEncodingDetector
import org.apache.commons.lang3.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.springframework.stereotype.Component
import work.bg.server.core.storage.FileType
import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import work.bg.server.sms.MobileHelper
import java.io.File


@Component
class MobileFileType:FileType {
    override val filter: Regex
        get() = Regex(".+\\.((txt)|(csv)|(xls)|(xlsx)$)",RegexOption.IGNORE_CASE)
    override val isTransient: Int
        get() = 1
    override val title: String
        get() = "号码文件"
    override val typ: String
        get() = "mobilefile"
    private fun getCharset(file:String): Charset {
        var stream = FileInputStream(file)
        val ret= TikaEncodingDetector().guessEncoding(stream)
        return if(ret==null) Charset.defaultCharset() else Charset.forName(ret)
    }
    override fun process(file: String): Any? {
        val ext = file.substringAfterLast(".").toLowerCase()
        var mobileCount = 0
        if(ext=="csv"||ext=="txt"){
           val lines =  Files.readAllLines(Paths.get(file),this.getCharset(file))
            var p =Pattern.compile("1[3456789]\\d{9}")
            lines.forEach {
             val m = p.matcher(it)
                while (m.find()){
                  mobileCount++
                }
            }
            var jo = JsonObject()
            jo.addProperty("mobileCount",mobileCount)
            jo.addProperty("errorCode",0)
            jo.addProperty("errorMsg","成功")
            return jo
        }
        else if(ext=="xls"){
            try {
                val excelFile = FileInputStream(File(file))
                val workbook = HSSFWorkbook(excelFile)
                val datatypeSheet = workbook.getSheetAt(0)
                val iterator = datatypeSheet.iterator()
                while (iterator.hasNext()) {
                    val currentRow = iterator.next()
                    val cellIterator = currentRow.iterator()
                    while (cellIterator.hasNext()){
                        var currentCell = cellIterator.next()
                        if (currentCell.cellType == CellType.STRING) {
                            var p =Pattern.compile("1[3456789]\\d{9}")
                            val txt = currentCell.stringCellValue
                            val m = p.matcher(txt)
                            while (m.find()){
                                mobileCount++
                            }
                        } else if (currentCell.cellType == CellType.NUMERIC) {
                            var mobile = currentCell.numericCellValue.toString()
                            if(MobileHelper.isMobile(mobile = mobile)){
                                mobileCount++
                            }
                        }
                    }
                }
                var jo = JsonObject()
                jo.addProperty("mobileCount",mobileCount)
                jo.addProperty("errorCode",0)
                jo.addProperty("errorMsg","成功")
                return jo
            }
            catch (ex:Exception){
                ex.printStackTrace()
            }
        }
        else if(ext=="xlsx"){
            try {
                val excelFile = FileInputStream(File(file))
                val workbook = XSSFWorkbook(excelFile)
                val datatypeSheet = workbook.getSheetAt(0)
                val iterator = datatypeSheet.iterator()
                while (iterator.hasNext()) {
                    val currentRow = iterator.next()
                    val cellIterator = currentRow.iterator()
                    while (cellIterator.hasNext()){
                        var currentCell = cellIterator.next()
                        if (currentCell.cellType == CellType.STRING) {
                            var p =Pattern.compile("1[3456789]\\d{9}")
                            val txt = currentCell.stringCellValue
                            val m = p.matcher(txt)
                            while (m.find()){
                                mobileCount++
                            }
                        } else if (currentCell.cellType == CellType.NUMERIC) {
                            var mobile = currentCell.numericCellValue.toString()
                            if(MobileHelper.isMobile(mobile = mobile)){
                                mobileCount++
                            }
                        }
                    }
                }
                var jo = JsonObject()
                jo.addProperty("mobileCount",mobileCount)
                jo.addProperty("errorCode",0)
                jo.addProperty("errorMsg","成功")
                return jo
            }
            catch (ex:Exception){
                ex.printStackTrace()
            }
        }
        var obj=JsonObject()
        obj.addProperty("errorMsg","文件格式错误")
        obj.addProperty("errorCode",9999)
        return obj
    }
}