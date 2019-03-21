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

package work.bg.server.core.model

import com.google.gson.JsonObject
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import work.bg.server.core.RefSingleton
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.*
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.ActionResult

@Model("app","应用")
class BaseApp(tableName:String, schemaName:String):ContextModel(tableName,schemaName) {

    companion object: RefSingleton<BaseApp> {
        override lateinit var ref: BaseApp
    }
    constructor():this("base_app","public")

    val id=ModelField(null,
            "id",
            FieldType.BIGINT,
            "标示",
            primaryKey = FieldPrimaryKey())
    val name=ModelField(null,
            "name",
            FieldType.STRING,
            "名称")
    val title=ModelField(null,
            "title",
            FieldType.STRING,
            "说明",
            defaultValue = "针对app的说明")
    val defaultFlag=ModelField(null,
            "default_flag",
            FieldType.INT,
            "默认",
            defaultValue = 0)
    val partnerRole=ModelMany2OneField(null,
            "partner_role_id",
            FieldType.BIGINT,
            targetModelTable = "public.base_partner_role",
            targetModelFieldName = "id",
            title = "角色",
            foreignKey = FieldForeignKey(action = ForeignKeyAction.CASCADE)
            )

    @Action("loadAppContainer")
    fun loadAppContainer(@RequestBody appData :JsonObject,
                         partnerCache:PartnerCache):ActionResult?{
        var res = ActionResult()
        var app= appData["app"].asString
        var menu = appData["menu"].asJsonObject
        var menuApp = menu["app"]?.asString
        var menuName = menu["menu"]?.asString
        var viewKeys = partnerCache.getAccessControlAppViewKey(app)
        var menuTree = partnerCache.getAccessControlMenu(menuApp,menuName)
        if(viewKeys!=null){
            res.bag["views"]=viewKeys
        }
        if(menuName!=null && menuTree!=null){
            res.bag[menuName]=menuTree
        }
        return res
    }
}