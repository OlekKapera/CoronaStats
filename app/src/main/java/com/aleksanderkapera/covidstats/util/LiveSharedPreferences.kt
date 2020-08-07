package com.aleksanderkapera.covidstats.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.aleksanderkapera.covidstats.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.subjects.PublishSubject

object LiveSharedPreferences{

    private val updates = PublishSubject.create<String>()

    private val listener = OnSharedPreferenceChangeListener { _, key ->
        updates.onNext(key)
    }

    private lateinit var preferences: SharedPreferences

    val gson = Gson()

    fun with(application: Application) {
        preferences = application.getSharedPreferences(
            R.string.preferences_file_name.asString(),
            Context.MODE_PRIVATE
        )
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Retrieve standard types from preferences
     */
    fun <T> get(key: String, defaultValue: T?) =
        LivePreference(updates, preferences, key, defaultValue)

    /**
     * Used to retrieve object from the Preferences as liveData.
     **/
    inline fun <reified T> getObject(key: String): LiveData<T?> {
        return Transformations.map(get<String>(key, null)) {
            GsonBuilder().create().fromJson(it, T::class.java)
        }
    }

    /**
     * Used to retrieve list of objects from the Preferences as liveData.
     **/
    inline fun <reified T> getObjectList(key: String): LiveData<List<T>?> {
        return Transformations.map(getObject<Container<T>>(key)) { container ->
            container?.values?.map { value ->
                value ?: throw Exception("${this.javaClass.simpleName} getList() value was null!")
                val jsonObject = gson.toJsonTree(value).asJsonObject
                gson.fromJson(jsonObject, T::class.java)
            }
        }
    }

    /**
     * Data class encapsulating generic list type
     */
    data class Container<T>(val values: List<T>)
}