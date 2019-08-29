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

package work.bg.server.errorcode


enum class ErrorCode(val code:Int, val description:String){
    SUCCESS(0,"success"),
    RELOGIN(1,"login in"),
    USERNAMEORPASSWORDISEMPTY(2,"UserName or password cant be empty!"),
    USERNAMEORPASSWORDILLEGAL(3,"UserName or password is illegal"),
    CREATEMODELFAIL(4,"没有提交任何数据！"),
    UPDATEMODELFAIL(5,"model update failed"),
    UNKNOW(9999,"system busy error")
}