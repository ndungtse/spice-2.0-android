package com.medtroniclabs.spice.common

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCriteria
import java.lang.reflect.Type


object SecuredPreference {

    enum class EnvironmentKey {
        TOKEN,
        USERNAME,
        PHONE_NUMBER,
        PASSWORD,
        ISLOGGEDIN,
        ISOFFLINELOGIN,
        ISMETALOADED,
        USER_RESPONSE,
        IS_INITIAL_DATA_LOADED,
        IS_ABOVE_FIVE_YEARS_LOADED,
        USER_FHIR_ID,
        ORGANIZATION_FHIR_ID,
        IS_MOTHER_NEONATE_LOADEDANC,
        IS_UNDER_TWO_MONTHS_LOADED,
        IS_LABOUR_DELIVERY_LOADED,
        OFFLINE_SYNC_REQUEST_ID,
        LAST_SYNCED_AT,
        FOLLOW_UP_CRITERIA
    }


    private const val DEFAULT_SUFFIX = "_preferences"

    private var mPrefs: SharedPreferences? = null

    fun build(
        mKey: String,
        context: Context
    ) {
        initPrefs(context, mKey + DEFAULT_SUFFIX)
    }

    private fun initPrefs(context: Context, prefsName: String) {
        val mainKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        mPrefs = EncryptedSharedPreferences.create(
            context,
            prefsName,
            mainKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Returns the underlying SharedPreference instance
     *
     * @return an instance of the SharedPreference
     * @throws RuntimeException if SharedPreference instance has not been instantiated yet.
     */
    private val preferences: SharedPreferences
        get() {
            if (mPrefs != null) {
                return mPrefs!!
            }
            throw RuntimeException(
                "Prefs class not correctly instantiated. Please call Builder.setContext().build() in the Application class onCreate."
            )
        }

    /**
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     * @see android.content.SharedPreferences.getAll
     */
    val all: Map<String, *>
        get() = preferences.all

    /**
     * Retrieves a stored int value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not
     * an int.
     * @see android.content.SharedPreferences.getInt
     */
    fun getInt(key: String, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    /**
     * Retrieves a stored int value, or 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not
     * an int.
     * @see android.content.SharedPreferences.getInt
     */
    fun getInt(key: String): Int {
        return preferences.getInt(key, 0)
    }


    /**
     * Retrieves a stored boolean value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a boolean.
     * @see android.content.SharedPreferences.getBoolean
     */
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    /**
     * Retrieves a stored boolean value, or false if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or false.
     * @throws ClassCastException if there is a preference with this name that is not a boolean.
     * @see android.content.SharedPreferences.getBoolean
     */
    fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }


    /**
     * Retrieves a stored long value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getLong(key: String, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    /**
     * Retrieves a stored long value, or 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getLong(key: String): Long {
        return preferences.getLong(key, 0L)
    }

    /**
     * Returns the double that has been saved as a long raw bits value in the long preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue the double Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getDouble(key: String, defValue: Double): Double {
        return java.lang.Double.longBitsToDouble(
            preferences.getLong(
                key,
                java.lang.Double.doubleToLongBits(defValue)
            )
        )
    }

    /**
     * Returns the double that has been saved as a long raw bits value in the long preferences.
     * Returns 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getDouble(key: String): Double {
        return java.lang.Double.longBitsToDouble(
            preferences.getLong(
                key,
                java.lang.Double.doubleToLongBits(0.0)
            )
        )
    }

    /**
     * Retrieves a stored float value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a float.
     * @see android.content.SharedPreferences.getFloat
     */
    fun getFloat(key: String, defValue: Float): Float {
        return preferences.getFloat(key, defValue)
    }

    /**
     * Retrieves a stored float value, or 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not a float.
     * @see android.content.SharedPreferences.getFloat
     */
    fun getFloat(key: String): Float {
        return preferences.getFloat(key, 0.0f)
    }


    /**
     * Retrieves a stored String value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a String.
     * @see android.content.SharedPreferences.getString
     */
    fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    /**
     * Retrieves a stored String value, or an empty string if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or "".
     * @throws ClassCastException if there is a preference with this name that is not a String.
     * @see android.content.SharedPreferences.getString
     */
    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }

    /**
     * Stores a long value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putLong
     */
    fun putLong(key: String, value: Long) {
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    /**
     * Stores an integer value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putInt
     */
    fun putInt(key: String, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Stores a double value as a long raw bits value.
     *
     * @param key   The name of the preference to modify.
     * @param value The double value to be save in the preferences.
     * @see android.content.SharedPreferences.Editor.putLong
     */
    fun putDouble(key: String, value: Double) {
        val editor = preferences.edit()
        editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        editor.apply()
    }

    /**
     * Stores a float value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putFloat
     */
    fun putFloat(key: String, value: Float) {
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    /**
     * Stores a boolean value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putBoolean
     */
    fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * Stores a String value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putString
     */
    fun putString(key: String, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Removes a preference value.
     *
     * @param key The name of the preference to remove.
     * @see android.content.SharedPreferences.Editor.remove
     */
    fun remove(key: String) {
        val prefs = preferences
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }

    /**
     * Checks if a value is stored for the given key.
     *
     * @param key The name of the preference to check.
     * @return `true` if the storage contains this key value, `false` otherwise.
     * @see android.content.SharedPreferences.contains
     */
    operator fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    fun saveStringArray(key: String, array: Array<String>) {
        val editor = preferences.edit()
        val set = HashSet<String>()
        set.addAll(array)
        editor.putStringSet(key, set)
        editor.apply()
    }

    fun getStringArray(key: String): Array<String>? {
        val set = preferences.getStringSet(key, null)
        return set?.toTypedArray()
    }

    fun clear(context: Context) {
        val username = getString(EnvironmentKey.USERNAME.name)
        val phoneNumber = getString(EnvironmentKey.PHONE_NUMBER.name)
        val password = getString(EnvironmentKey.PASSWORD.name)
        val initialDataStatus = getBoolean(EnvironmentKey.IS_INITIAL_DATA_LOADED.name)
        val lastSyncedAt = getString(EnvironmentKey.LAST_SYNCED_AT.name)
        val requestIds =
            getStringArray(EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
        try {
            preferences.edit().clear().apply()
        } catch (e: Exception) {
            val sharedPreferences =
                context.getSharedPreferences("SecuredPreference", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        } finally {
            putString(EnvironmentKey.USERNAME.name, username)
            putString(EnvironmentKey.PHONE_NUMBER.name, phoneNumber)
            putString(EnvironmentKey.PASSWORD.name, password)
            putString(EnvironmentKey.LAST_SYNCED_AT.name, lastSyncedAt)
            putBoolean(EnvironmentKey.IS_INITIAL_DATA_LOADED.name, initialDataStatus)
            requestIds?.let { saveStringArray(EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name, it) }
        }
    }

    fun putUserDetails(loginResponse: LoginResponse) {
        val loginResponseString = Gson().toJson(loginResponse)
        putString(EnvironmentKey.USER_RESPONSE.name, loginResponseString)
    }

    fun getUserDetails(): LoginResponse {
        val userResponseString = getString(EnvironmentKey.USER_RESPONSE.name)
        val type: Type = object : TypeToken<LoginResponse>() {}.type
        return Gson().fromJson(userResponseString, type)
    }

    fun putFollowUpCriteria(followUpCriteria: FollowUpCriteria) {
        val followUpCriteriaString = Gson().toJson(followUpCriteria)
        putString(EnvironmentKey.FOLLOW_UP_CRITERIA.name, followUpCriteriaString)
    }

    fun getFollowUpCriteria(): FollowUpCriteria {
        val followUpCriteriaString = getString(EnvironmentKey.FOLLOW_UP_CRITERIA.name)
        val type: Type = object : TypeToken<FollowUpCriteria>() {}.type
        return Gson().fromJson(followUpCriteriaString, type)
    }

    fun getUserId(): Long {
        return getUserDetails().id
    }

    fun getPhoneNumberCode(): String? {
        return getUserDetails().country.phoneNumberCode
    }

    fun getRole(): String? {
        return getUserDetails().roles.first().name
    }

    fun logout(): Boolean {
        remove(EnvironmentKey.ISLOGGEDIN.name)
        remove(EnvironmentKey.TOKEN.name)
        remove(EnvironmentKey.ISOFFLINELOGIN.name)
//        remove(EnvironmentKey.USER_RESPONSE.name)
        remove(EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name)
        remove(EnvironmentKey.IS_MOTHER_NEONATE_LOADEDANC.name)
        remove(EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name)
        remove(EnvironmentKey.IS_LABOUR_DELIVERY_LOADED.name)
        return true
    }

    fun getUserFhirId(): String {
        return getString(EnvironmentKey.USER_FHIR_ID.name) ?: ""
    }

    fun getOrganizationFhirId(): String {
        return getString(EnvironmentKey.ORGANIZATION_FHIR_ID.name) ?: ""
    }
}