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

package work.bg.server.core.model

import dynamic.model.query.mq.*
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import dynamic.model.web.spring.boot.annotation.Model
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.storage.FileEntity

@Model("storageEntity","文件")
class StorageEntity:ContextModel("base_storage_entity","public") {
    companion object: RefSingleton<StorageEntity> {
        override lateinit var ref: StorageEntity
    }

    val id= ModelField(null,
            "id",
            dynamic.model.query.mq.FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())

    val serverPath= ModelField(null,
            "server_path",
            FieldType.STRING,
            "服务器路径")

    val clientName= ModelField(null,
            "client_name",
            FieldType.STRING,
            "上传名称")

    val requestName = ModelField(null,
            "request_name",
            FieldType.STRING,
            title = "请求标识")


    val typ = ModelField(null,
            "typ",
            FieldType.STRING,
            title = "资源类型")

    val typTitle = ModelField(null,
            "typ_title",
            FieldType.STRING,
            title = "资源类型名称")


    val isTransient = ModelField(null,
            "is_transient",
            FieldType.INT,
            title = "临时资源")

    val partners = ModelMany2ManyField(null,
            "partners",
            FieldType.BIGINT,
            "员工",
            relationModelTable = "public.base_partner_storage_entity_rel",
            relationModelFieldName = "partner_id",
            targetModelTable = "public.base_partner",
            targetModelFieldName = "id")

    fun addFileEntity(fileEntity:FileEntity,
                      partnerCache: PartnerCache):String?{
        val requestName = work.bg.server.util.GUID.randString().replace("-","")
        val mo = ModelDataObject(model=this)
        mo.setFieldValue(this.serverPath,fileEntity.serverPathName)
        mo.setFieldValue(this.clientName,fileEntity.clientName)
        mo.setFieldValue(this.requestName,requestName)
        mo.setFieldValue(this.typ,fileEntity.fileTyp.typ)
        mo.setFieldValue(this.typTitle,fileEntity.fileTyp.title)
        mo.setFieldValue(this.isTransient,fileEntity.fileTyp.isTransient)

        val psRel = ModelDataObject(model=BasePartnerStorageEntityRel.ref)
        psRel.setFieldValue(BasePartnerStorageEntityRel.ref.ownerTyp,0)
        psRel.setFieldValue(BasePartnerStorageEntityRel.ref.partner,partnerCache.partnerID)
        var sharedObject = ModelDataSharedObject(data=mutableMapOf<ModelBase?,ModelData>(
                BasePartnerStorageEntityRel.ref to psRel
        ))
        mo.setFieldValue(ConstRelRegistriesField.ref,sharedObject)
        var ret = this.rawCreate(mo,
                useAccessControl = true,
                partnerCache = partnerCache)
        if(ret.first!=null && ret.first!!>0){
            return requestName
        }
        return null
    }

    fun getServerFile(requestName:String):String?{
        var r = this.rawRead(model=this,criteria = eq(this.requestName,requestName))?.firstOrNull()
        return r?.let {
            it.getFieldValue(this.serverPath) as String?
        }
    }
}