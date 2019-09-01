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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import work.bg.server.core.model.*
import work.bg.server.core.mq.*
import work.bg.server.core.mq.specialized.ConstRelRegistriesField
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
                                return createPartnerCache(key)
                            }
                        })
    }
    protected  fun createPartnerCache(partnerKey:PartnerCacheKey): PartnerCache {

        try {
            var partnerData=basePartner?.rawRead(*basePartner?.fields?.getAllPersistFields()?.values?.toTypedArray()!!,
                    criteria = eq(this.basePartner?.id!!,partnerKey.partnerID)!!,
                    attachedFields = arrayOf(AttachedField(this.basePartner?.corps!!),AttachedField(this.basePartner?.partnerRoles!!)))

            var corpPartnerRelFieldValueArry=((partnerData?.data?.firstOrNull()?.getValue(ConstRelRegistriesField.ref) as ModelDataSharedObject).
                    data?.get(BaseCorpPartnerRel.ref) as ModelDataArray?)?.data?.firstOrNull {
                (it.getValue(BaseCorpPartnerRel.ref!!.corp) as ModelDataObject).data.getValue(BaseCorp.ref!!.id) as Long?==partnerKey.corpID
            }

            var corpModelDataObject=corpPartnerRelFieldValueArry?.getValue(BaseCorpPartnerRel.ref?.corp!!) as ModelDataObject
            var partnerRoleModelDataObject = corpPartnerRelFieldValueArry?.getValue(BaseCorpPartnerRel.ref?.partnerRole!!) as ModelDataObject

            var roleID=partnerRoleModelDataObject.data.getValue(BasePartnerRole.ref?.id!!) as Long?

            return PartnerCache(mapOf(
                    "corpObject" to corpModelDataObject,
                    "partnerRoleObject" to partnerRoleModelDataObject
            ),partnerKey.partnerID,partnerKey.corpID,roleID!!)
        }
        catch (ex:Exception){
          throw ex
        }
    }
    fun get(key:PartnerCacheKey):PartnerCache?{
        return this.partnersCache?.get(key)
    }
}