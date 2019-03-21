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

package work.bg.server.core.cache


import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import work.bg.server.core.model.*
import work.bg.server.core.mq.*
import work.bg.server.core.mq.specialized.ConstRelRegistriesField
import java.time.Duration

@Service
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
    fun get(key:PartnerCacheKey):PartnerCache?{
        return this.partnersCache?.get(key)
    }
}