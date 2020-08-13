package com.aleksanderkapera.covidstats.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Manages shared preferences
 *
 * Inspired by Malwinder Singh
 */
object SharedPrefsManager {

    //Shared Preference field used to save and retrieve JSON string
    lateinit var preferences: SharedPreferences
    val gson = Gson()

    //Name of Shared Preference file
    private const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"

    /**
     * Call this first before retrieving or saving object.
     *
     * @param application Instance of application class
     */
    fun with(application: Application) {
        preferences = application.getSharedPreferences(
            PREFERENCES_FILE_NAME, Context.MODE_PRIVATE
        )
    }

    /**
     * Saves object into the Preferences.
     *
     * @param `object` Object of model class (of type [T]) to save
     * @param key Key with which Shared preferences to
     **/
    fun <T> put(`object`: T, key: String) {
        //Convert object to JSON String.
        val jsonString = GsonBuilder().create().toJson(`object`)
        //Save that String in SharedPreferences
        preferences.edit().putString(key, jsonString).apply()
    }

    /**
     * Used to retrieve object from the Preferences.
     *
     * @param key Shared Preference key with which object was saved.
     **/
    inline fun <reified T> get(key: String): T? {
        //We read JSON String which was saved.
        val value = preferences.getString(key, null)
        //JSON String was found which means object can be read.
        //We convert this JSON String to model object. Parameter "c" (of
        //type Class < T >" is used to cast.
        return GsonBuilder().create().fromJson(value, T::class.java)
    }

    /**
     * Encapsulates list in a object a saves to shared preferences
     *
     * @param objects List of objects to be saved
     * @param key Key string upon which shared preference may be accessed
     */
    fun <T> putList(objects: List<T>, key: String) {
        val container = Container<T>(objects)
        put(container, key)
    }

    /**
     * Returns list of objects from shared preferences
     *
     * @param key Key string upon which shared preference may be accessed
     */
    inline fun <reified T> getList(key: String): List<T>? {
        return get<Container<T>>(key)?.values?.map { value ->
            value ?: throw Exception("${this.javaClass.simpleName} getList() value was null!")
            val jsonObject = gson.toJsonTree(value).asJsonObject
            gson.fromJson(jsonObject, T::class.java)
        }
    }

    /**
     * Delete Shared Preference based on key
     */
    fun delete(key: String) {
        preferences.edit().remove(key).apply()
    }

    /**
     * Data class encapsulating generic list type
     */
    data class Container<T>(val values: List<T>)
}