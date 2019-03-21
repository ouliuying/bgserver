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

package work.bg.server.errorcode


enum class ErrorCode(val code:Int, val description:String){
    SUCCESS(0,"success"),
    RELOGIN(1,"login in"),
    USERNAMEORPASSWORDISEMPTY(2,"UserName or password cant be empty!"),
    USERNAMEORPASSWORDILLEGAL(3,"UserName or password is illegal"),
    CREATEMODELFAIL(4,"model create failed"),
    UPDATEMODELFAIL(4,"model update failed"),
    UNKNOW(9999,"system busy error")
}