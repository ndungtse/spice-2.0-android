package com.mdtlabs.ncd.db.local

import com.mdtlabs.ncd.db.dao.LanguageDAO
import com.mdtlabs.ncd.db.tables.LanguageEntity
import javax.inject.Inject

class RoomHelperImpl @Inject constructor(private val languageDAO: LanguageDAO): RoomHelper {

    override suspend fun insertLanguage(languageEntity: LanguageEntity) = languageDAO.insertLanguage(languageEntity)

    override suspend fun deleteAllLanguage() = languageDAO.deleteAllLanguage()

}