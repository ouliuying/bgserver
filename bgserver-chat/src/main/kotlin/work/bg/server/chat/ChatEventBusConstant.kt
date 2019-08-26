/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.chat

object ChatEventBusConstant {
    const val CHAT_SESSION_ID = "chatSessionID"
    const val CHAT_TO_UUID = "toUUID"
    const val CHAT_FROM_UUID = "fromUUID"
    const val CHANNEL_UUID = "channelUUID"
    const val CLIENT_TO_SERVER_ADDRESS = "client.to.server"
    const val SERVER_TO_CLIENT_ADDRESS_PATTERN = "^server\\.to\\.client\\.[.]+\$"
    const val SERVER_TO_CLIENT_ADDRESS_HEADER = "server.to.client."
}