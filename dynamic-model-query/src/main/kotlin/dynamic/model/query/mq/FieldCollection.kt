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

package dynamic.model.query.mq
import java.util.concurrent.locks.StampedLock


class FieldCollection(vararg fields: dynamic.model.query.mq.FieldBase): Iterable<dynamic.model.query.mq.FieldBase>{
    private var fieldSet= mutableMapOf<String, dynamic.model.query.mq.FieldBase>()
    private var cachedPropertyFieldsLock:StampedLock = StampedLock()
    private lateinit var cachedPropertyFields:MutableMap<String, dynamic.model.query.mq.FieldBase>
    private var cachedPersistFieldsLock: StampedLock = StampedLock()
    private lateinit var cachedPersistFields:MutableMap<String, dynamic.model.query.mq.FieldBase>
    private lateinit var cachedPersistFieldsExcludeVirtualOne2OneField:MutableMap<String, dynamic.model.query.mq.FieldBase>
    private val model by lazy{this.fieldSet.values.first().model!! }
    init {

        fields.forEach {
            fieldSet[it.getFullName()!!]=it
        }
    }
    fun getAllFields():Map<String, dynamic.model.query.mq.FieldBase>{
        return this.fieldSet
    }
    fun getDefaultOrderBy(): dynamic.model.query.mq.OrderBy?{
        var idf= getField(dynamic.model.query.mq.FieldConstant.Companion.id)
        return if(idf!=null){
            dynamic.model.query.mq.OrderBy(dynamic.model.query.mq.OrderBy.OrderField(idf!!, dynamic.model.query.mq.OrderBy.Companion.OrderType.DESC))
        }
        else{
            null
        }
    }
    fun getFieldByTargetField(field: dynamic.model.query.mq.FieldBase?): dynamic.model.query.mq.FieldBase?{
        return this.fieldSet.values.firstOrNull {
            when(it){
                is dynamic.model.query.mq.RefTargetField ->{
                    "${it.targetModelTable}.${it.targetModelFieldName}"==field?.getFullName()}
                else->false
                }
            }
    }
    fun getFieldByPropertyName(propertyName:String): dynamic.model.query.mq.FieldBase?{

        var tamp=cachedPropertyFieldsLock.readLock()
        try {
            if(this::cachedPropertyFields.isInitialized){

                return this.cachedPropertyFields?.get(propertyName)
            }
        }

        finally {
            cachedPropertyFieldsLock.unlockRead(tamp)
        }

        tamp=cachedPropertyFieldsLock.writeLock()
        try {
            this.cachedPropertyFields= mutableMapOf()
            this.fieldSet.forEach{
               this.cachedPropertyFields?.set(it.value.propertyName,it.value)
            }
            return this.cachedPropertyFields?.get(propertyName)
        }
        finally {
            cachedPropertyFieldsLock.unlockWrite(tamp)
        }
    }

    fun getIdField(): dynamic.model.query.mq.FieldBase?{
        return getField(dynamic.model.query.mq.FieldConstant.Companion.id)
    }
    fun getPartnerField(): dynamic.model.query.mq.FieldBase?{
        return getField(dynamic.model.query.mq.FieldConstant.Companion.partner)
    }
    fun getCorpField(): dynamic.model.query.mq.FieldBase?{
        return getField(dynamic.model.query.mq.FieldConstant.Companion.corp)
    }
    fun getPartnerRoleField(): dynamic.model.query.mq.FieldBase?{
        return getField(dynamic.model.query.mq.FieldConstant.Companion.partner_role)
    }

    fun getField(name:String?): dynamic.model.query.mq.FieldBase?{
        if(!name?.contains(".")!!){
            return this.fieldSet["${this.model.fullTableName}.$name"]
        }
        return this.fieldSet[name]
    }
    fun clearCache(){
        var tamp=cachedPersistFieldsLock.writeLock()
        try {
            this.cachedPersistFields= mutableMapOf()
            this.cachedPersistFieldsExcludeVirtualOne2OneField= mutableMapOf()
        }
        finally {
            cachedPersistFieldsLock.unlockWrite(tamp)
        }
    }

    override fun iterator(): Iterator<dynamic.model.query.mq.FieldBase> {
        return object :Iterator<dynamic.model.query.mq.FieldBase>{
            val fields=this@FieldCollection.fieldSet.values
            var it=fields.iterator()
            override fun hasNext(): Boolean {
                return it.hasNext()
            }

            override fun next(): dynamic.model.query.mq.FieldBase {
              return it.next()
            }
        }
    }

    fun add(field: dynamic.model.query.mq.FieldBase?): dynamic.model.query.mq.FieldBase?{
        if(!this.fieldSet.containsKey(field?.getFullName()))
        {
            this.fieldSet.put(field?.getFullName()!!,field!!);
        }
        return null
    }

    fun remove(fullName:String): dynamic.model.query.mq.FieldBase?{
        return this.fieldSet.remove(fullName)
    }
    inline fun <reified T>  getTypeFields():Map<String, dynamic.model.query.mq.FieldBase?>?{
        var fs= mutableMapOf<String, dynamic.model.query.mq.FieldBase?>()
        this.getAllFields().forEach { t, u ->
            when(u){
                is T->{
                    fs[t]=u
                }
            }
        }
        return if(fs.count()>0){
            fs
        }
        else{
            null
        }
    }

    fun getAllPersistFields(excludeVirtualOne2OneField:Boolean=false):Map<String, dynamic.model.query.mq.FieldBase>{
        var tamp=cachedPersistFieldsLock.readLock()
        try {
            if(this::cachedPersistFields.isInitialized){
                return if (!excludeVirtualOne2OneField) {
                     this.cachedPersistFields
                } else {
                    this.cachedPersistFieldsExcludeVirtualOne2OneField
                }
            }
        }
        finally {
            cachedPersistFieldsLock.unlockRead(tamp)
        }

        tamp=cachedPersistFieldsLock.writeLock()
        try {
            this.cachedPersistFields= mutableMapOf()
            this.cachedPersistFieldsExcludeVirtualOne2OneField= mutableMapOf()
            this.fieldSet.forEach{
                if((it.value !is dynamic.model.query.mq.FunctionField<*,*>) && (it.value !is dynamic.model.query.mq.RefRelationField) && it.value !is dynamic.model.query.mq.One2ManyField){
                        this.cachedPersistFields?.set(it.key,it.value)
                    if(it.value !is dynamic.model.query.mq.ModelOne2OneField || !(it.value as dynamic.model.query.mq.ModelOne2OneField).isVirtualField){
                        cachedPersistFieldsExcludeVirtualOne2OneField?.set(it.key,it.value)
                    }
                }
            }
            if(!excludeVirtualOne2OneField){
                return this.cachedPersistFields
            }
            else{
                return cachedPersistFieldsExcludeVirtualOne2OneField
            }

        }
        finally {
            cachedPersistFieldsLock.unlockWrite(tamp)
        }
    }

    fun sortTypeFields(vararg fields: dynamic.model.query.mq.FieldBase):Triple<Map<String, dynamic.model.query.mq.FieldBase>,Map<String, dynamic.model.query.mq.FieldBase>,Map<String, dynamic.model.query.mq.FieldBase>>?{
        var names=fields.map {
            it.getFullName()
        }
        return sortTypeFields(*names.toTypedArray())
    }

    fun sortTypeFields(vararg fullFieldNames:String?):Triple<Map<String, dynamic.model.query.mq.FieldBase>,Map<String, dynamic.model.query.mq.FieldBase>,Map<String, dynamic.model.query.mq.FieldBase>>?{
        var persistFields= mutableMapOf<String, dynamic.model.query.mq.FieldBase>()
        var funFields= mutableMapOf<String, dynamic.model.query.mq.FieldBase>()
        var relatioinFields=mutableMapOf<String, dynamic.model.query.mq.FieldBase>()
        this.fieldSet.forEach{
            if(fullFieldNames!=null && fullFieldNames!!.toList().contains(it.key)){
                when(it){
                    is dynamic.model.query.mq.FunctionField<*,*> ->{
                        funFields[it.key] = it.value
                    }
                    is dynamic.model.query.mq.RefRelationField ->{
                        relatioinFields[it.key]=it.value
                    }
                    else->{
                        persistFields[it.key] = it.value
                    }
                }
            }
        }
        return Triple(persistFields,relatioinFields,funFields)
    }
    fun toArray():Array<dynamic.model.query.mq.FieldBase>?{
        return this.fieldSet.values.toTypedArray()
    }
}