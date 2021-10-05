package com.mdtlabs.ncd.db.local
import com.mdtlabs.ncd.db.tables.LanguageEntity

interface RoomHelper {

    suspend fun insertLanguage(languageEntity: LanguageEntity)
    suspend fun deleteAllLanguage()
}