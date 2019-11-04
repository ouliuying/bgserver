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

package work.bg.server.core.cache


import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import work.bg.server.core.context.ContextVariantInitializer
import work.bg.server.core.model.*
import java.lang.Exception
import java.time.Duration

@Component
class PartnerCacheRegistry{
    private var partnersCache: LoadingCache<PartnerCacheKey, PartnerCache>?=null
    @Value("\${partner.cache.max-size}")
    private var maxCacheSize:Long=1000
    @Value("\${partner.cache.timeout}")
    private var cacheTimeoutMinutes:Long=30
    @Autowired
    protected  val basePartner: BasePartner?=null
    @Autowired
    protected  val baseCorp: BaseCorp?=null
    @Autowired
    protected val basePartnerRole: BasePartnerRole?=null
    init {
        this.partnersCache = CacheBuilder.newBuilder()
                .maximumSize(this.maxCacheSize)
                .expireAfterAccess(Duration.ofMinutes(this.cacheTimeoutMinutes))
                .build(
                        object : CacheLoader<PartnerCacheKey, PartnerCache>() {
                            override fun load(key: PartnerCacheKey): PartnerCache {
                                var pc= createPartnerCache(key)
                                pc=this@PartnerCacheRegistry.loadPartnerCacheContext(pc)
                                return pc
                            }
                        })
    }
    protected  fun createPartnerCache(partnerKey:PartnerCacheKey): PartnerCache {

        try {
            var partnerData=basePartner?.rawRead(*basePartner?.fields?.getAllPersistFields()?.values?.toTypedArray()!!,
                    criteria = eq(this.basePartner?.id!!, partnerKey.partnerID),
                    attachedFields = arrayOf(dynamic.model.query.mq.AttachedField(this.basePartner?.corps!!), dynamic.model.query.mq.AttachedField(this.basePartner?.partnerRoles!!)))

            var corpPartnerRelFieldValueArry=((partnerData?.data?.firstOrNull()?.getValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject).data.get(BaseCorpPartnerRel.ref) as dynamic.model.query.mq.ModelDataArray?)?.data?.firstOrNull {
                (it.getValue(BaseCorpPartnerRel.ref.corp) as dynamic.model.query.mq.ModelDataObject).data.getValue(BaseCorp.ref.id) as Long?==partnerKey.corpID
            }

            var corpModelDataObject=corpPartnerRelFieldValueArry?.getValue(BaseCorpPartnerRel.ref.corp) as dynamic.model.query.mq.ModelDataObject
            var partnerRoleModelDataObject = corpPartnerRelFieldValueArry.getValue(BaseCorpPartnerRel.ref.partnerRole) as dynamic.model.query.mq.ModelDataObject

            var roleID=partnerRoleModelDataObject.data.getValue(BasePartnerRole.ref.id) as Long?

            return PartnerCache(mapOf(
                    "corpObject" to corpModelDataObject,
                    "partnerRoleObject" to partnerRoleModelDataObject
            ),partnerKey.partnerID,partnerKey.corpID,roleID!!,partnerKey.devType)
        }
        catch (ex:Exception){
          throw ex
        }
    }
    private fun loadPartnerCacheContext(partnerCache:PartnerCache):PartnerCache{
        var initializers = dynamic.model.query.mq.model.AppModel.ref.getTypeModels<ContextVariantInitializer>()
        initializers.forEach {
            it.contextVariantSet(partnerCache = partnerCache)
        }
        return partnerCache
    }
    fun get(key:PartnerCacheKey):PartnerCache?{
        return this.partnersCache?.get(key)
    }
}