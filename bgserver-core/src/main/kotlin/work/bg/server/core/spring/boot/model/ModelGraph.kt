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

package work.bg.server.core.spring.boot.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.core.annotation.AnnotationUtils
import work.bg.server.core.spring.boot.annotation.Action
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
interface ModelGraph {
    open fun build()
    open fun createModelAction(): ModelAction?
    open fun getConcreteModelMetaDatas():List<ModelMetaData?>
}