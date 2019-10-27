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

package work.bg.server.product.acrule.bean

import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.eq
import org.springframework.stereotype.Component
import work.bg.server.core.acrule.ModelCreateRecordFieldsValueFilterRule
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.model.BasePartner
import work.bg.server.product.model.Product
import work.bg.server.product.model.ProductSKUPattern
import work.bg.server.util.SKUGenerator
import work.bg.server.util.TypeConvert

@Component
class ModelCreateProductInnerRecordFieldsValueFilterBean: ModelCreateRecordFieldsValueFilterRule<Unit> {
    private lateinit var _config:String
    override var config: String
        get() = _config
        set(value) {
            _config=value
        }

    override fun invoke(modelData: ModelDataObject, partnerCache: PartnerCache, data: Unit?): Pair<Boolean, String> {
        if(modelData.hasFieldValue(Product.ref.skuPattern)){
            var skuPattern = modelData.getFieldValue(Product.ref.skuPattern)
            var id =null as Long?
            when (skuPattern) {
                is ModelDataObject? -> id = skuPattern?.idFieldValue?.value as Long?
                is Number? -> try {
                    id = TypeConvert.getLong(skuPattern)
                } catch (ex:Throwable){
                    ex.printStackTrace()
                }
                else -> try {
                    id = skuPattern?.toString()?.toLong()

                } catch (ex:Exception){
                    ex.printStackTrace()
                }
            }
            id?.let {
                var p = ProductSKUPattern.ref.rawRead(criteria = eq(ProductSKUPattern.ref.id,id))?.firstOrNull()
                p?.let {
                    val pattern = p.getFieldValue(ProductSKUPattern.ref.pattern) as String?
                    pattern?.let {
                        var sku = SKUGenerator.newID(pattern)
                        modelData.setFieldValue(Product.ref.sku,sku)
                    }
                }
            }
        }
        return Pair(true,"")
    }
}