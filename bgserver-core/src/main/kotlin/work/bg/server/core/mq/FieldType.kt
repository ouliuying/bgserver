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

package work.bg.server.core.mq

import java.sql.JDBCType

enum class FieldType(val value:JDBCType) {
    NONE(JDBCType.NULL),
    INT(JDBCType.INTEGER),
    BIGINT(JDBCType.BIGINT),
    NUMBER(JDBCType.NUMERIC),
    DECIMAL(JDBCType.DECIMAL),
    STRING(JDBCType.VARCHAR),
    TEXT(JDBCType.LONGVARCHAR),
    DATETIME(JDBCType.TIMESTAMP),
    DATE(JDBCType.DATE),
    TIME(JDBCType.TIME),
};