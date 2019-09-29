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

package work.bg.server.core.controller

import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.errorcode.jsonFormat
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.model.ActionResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheKey
import work.bg.server.core.cache.PartnerCacheRegistry
import work.bg.server.core.constant.SessionTag
import work.bg.server.core.model.StorageEntity
import work.bg.server.core.spring.boot.annotation.ShouldLogin
import work.bg.server.core.storage.FileStorage
import java.nio.file.FileStore
import javax.servlet.http.HttpSession

@RestController
class StorageFileController {
    @Autowired
    lateinit var fileStorage:FileStorage
    @Autowired
    lateinit var partnerCacheRegistry: PartnerCacheRegistry
    @ShouldLogin
    @RequestMapping("/storage/upload/{fileType:^[a-z]{1,20}$}")
    fun uploadTempFile(@RequestParam("file") file:MultipartFile,
                       @PathVariable("fileType") fileType:String,session: HttpSession):Map<String,Any?> {
        val fe = fileStorage.save(file,fileType = fileType)
        if(fe==null){
            return ErrorCode.UPLOAD_FILE_FAIL.jsonFormat()
        }
        else{
            val partnerCacheKey = session.getAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY) as PartnerCacheKey?
                    ?: return ErrorCode.RELOGIN.jsonFormat()
            val partnerCache = partnerCacheRegistry.get(partnerCacheKey) ?: return ErrorCode.RELOGIN.jsonFormat()
            val requestName = StorageEntity.ref.addFileEntity(fe,partnerCache = partnerCache)
                    ?: return ErrorCode.UPLOAD_FILE_FAIL.jsonFormat()
            return ErrorCode.SUCCESS.jsonFormat(mapOf(
                    "requestName" to requestName,
                    "bag" to fe.attachedData
            ))
        }
    }
}