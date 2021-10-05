package com.mdtlabs.ncd.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mdtlabs.ncd.common.StringConverter.convertGivenStringToMap
import com.mdtlabs.ncd.db.NCDMergerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TranslateLanguage(private val database: NCDMergerDatabase, val context: Context) {

    private var languageMap: Map<String, Any> = HashMap()

    private var currentLanguage: Map<String, String> = HashMap()

    fun setupTranslateLanguage(languageCode: String,callback: (onComplete:Boolean)->Unit) {
        fetchLanguage(languageCode,callback)
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchLanguage(languageCode: String, callback: (onComplete: Boolean) -> Unit) {
        getLifecycleScope(context).launch {
            database.languageDao().getAllLanguage().forEach { languageEntity ->
                convertGivenStringToMap(languageEntity.languageDetail)?.let { map ->
                    languageMap = map[AccessKeys.translation] as Map<String, Any>
                    setCurrentLanguage(languageCode)
                    callback.invoke(true)
                }
            }
        }
    }

    private fun getLifecycleScope(context: Context): CoroutineScope {
        return when (context) {
            is Fragment -> context.lifecycleScope
            is AppCompatActivity -> context.lifecycleScope
            else -> GlobalScope
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun setCurrentLanguage(languageId: String) {
        if (languageMap.containsKey(languageId) && languageMap[languageId] is Map<*, *>) {
            currentLanguage = languageMap[languageId] as Map<String, String>
        }
    }

    private fun translate(word: String): String {
        return if (currentLanguage.containsKey(word)) {
            currentLanguage[word] ?: word
        } else {
            word
        }
    }

    fun translateList(words: ArrayList<String>): ArrayList<String> {
        val translatedList = ArrayList<String>()
        words.forEach {
            translatedList.add(translate(it))
        }
        return translatedList
    }


}