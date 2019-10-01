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

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.lang.Thread.sleep
import java.nio.file.*
import kotlin.random.Random

@Service
class FileStorage: InitializingBean, BeanPostProcessor {
    @Value("\${bg.work.storage.tmp-path}")
    lateinit var tmpPath:String
    @Value("\${bg.work.storage.path}")
    lateinit var path:String
    private var fileTypes = mutableMapOf<String,FileType>()

    override fun afterPropertiesSet() {
        val path = Paths.get("static/$tmpPath")
        if(Files.notExists(path)){
            Files.createDirectories(path)
        }
        val path2 = Paths.get("static/${this.path}")
        if(Files.notExists(path2)){
            Files.createDirectories(path2)
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
       if(bean is FileType){
           this.fileTypes[bean.typ]=bean
       }
        return bean
    }

    private fun getNewFileName(originalFilename:String):String{
        val ext = originalFilename.substringAfterLast(".")
        return if(ext==originalFilename) work.bg.server.util.GUID.randString() else work.bg.server.util.GUID.randString()+"."+ext
    }



    fun save(file:MultipartFile,fileType:String):FileEntity?{
        val filename = StringUtils.cleanPath(file.originalFilename!!)
        if(file.isEmpty){
            return null
        }
        if(filename.contains("..")){
            return null
        }
        return this.saveImp(file,fileType = fileType,
                clientFileName = filename)
    }

    private fun saveImp(file:MultipartFile,
                fileType:String,
                clientFileName:String):FileEntity?{
        val fileTyp = this.fileTypes[fileType]
        return fileTyp?.let {
            if(!it.filter.matches(clientFileName)){
                return null
            }
            val basePath = if(it.isTransient>0) this.tmpPath else this.path
            val destPath = Paths.get("static",basePath,getNewFileName(clientFileName))
            Files.copy(file.inputStream,destPath, StandardCopyOption.REPLACE_EXISTING)
            var tryCount = 100
            while (tryCount>0 && Files.notExists(destPath)){
                sleep(1000)
                tryCount--
            }
            if(tryCount<1){
                return null
            }
            val data = fileTyp.process(destPath.toString())
            return FileEntity(clientFileName,destPath.toString(),data,fileTyp)
        }
    }
}