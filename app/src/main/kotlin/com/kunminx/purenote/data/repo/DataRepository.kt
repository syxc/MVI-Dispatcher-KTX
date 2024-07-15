package com.kunminx.purenote.data.repo

import androidx.room.Room
import com.kunminx.architecture.utils.Utils
import com.kunminx.purenote.data.bean.Note
import com.kunminx.purenote.data.bean.Weather
import com.kunminx.purenote.domain.intent.Api
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Create by KunMinX at 2022/6/14
 */
object DataRepository {
  private const val DATABASE_NAME = "NOTE_DB.db"
  private val dataBase: NoteDataBase = Room.databaseBuilder(
    Utils.app!!.applicationContext,
    NoteDataBase::class.java, DATABASE_NAME
  ).build()

  private var mRetrofit: Retrofit? = null

  init {
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder()
      .connectTimeout(8, TimeUnit.SECONDS)
      .readTimeout(8, TimeUnit.SECONDS)
      .writeTimeout(8, TimeUnit.SECONDS)
      .addInterceptor(logging)
      .build()
    mRetrofit = Retrofit.Builder()
      .baseUrl(Api.BASE_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  suspend fun getNotes() = withContext(Dispatchers.IO) {
    dataBase.noteDao().notes()
  }

  suspend fun insertNote(note: Note) = withContext(Dispatchers.IO) {
    dataBase.noteDao().insertNote(note)
  }

  suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
    dataBase.noteDao().updateNote(note)
  }

  suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
    dataBase.noteDao().deleteNote(note)
  }

  suspend fun getWeatherInfo(api: String, cityCode: String): Pair<Weather.Live?, String> =
    withContext(Dispatchers.IO) {
      val service = mRetrofit!!.create(WeatherService::class.java)
      try {
        val weather = service.getWeatherInfo(api, cityCode, Api.API_KEY)
        Pair(weather.lives?.get(0)!!, "")
      } catch (e: Exception) {
        Pair(null, e.message.toString())
      }
    }
}
