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