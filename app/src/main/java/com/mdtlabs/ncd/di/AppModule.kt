package com.mdtlabs.ncd.di

import android.content.Context
import com.mdtlabs.ncd.BuildConfig
import com.mdtlabs.ncd.common.TranslateLanguage
import com.mdtlabs.ncd.db.NCDMergerDatabase
import com.mdtlabs.ncd.db.dao.LanguageDAO
import com.mdtlabs.ncd.db.local.RoomHelper
import com.mdtlabs.ncd.db.local.RoomHelperImpl
import com.mdtlabs.ncd.network.ApiHelper
import com.mdtlabs.ncd.network.ApiHelperImpl
import com.mdtlabs.ncd.network.ApiService
import com.mdtlabs.ncd.network.NetworkConstants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }


    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient, BASE_URL: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    fun providesUserApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }


    @Singleton
    @Provides
    fun provideApiHelper(apiHelper: ApiHelperImpl): ApiHelper {
        return apiHelper
    }

    @Singleton
    @Provides
    fun provideNcdDatabase(@ApplicationContext context: Context): NCDMergerDatabase {
        return NCDMergerDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideLanguageDao(db: NCDMergerDatabase): LanguageDAO {
        return db.languageDao()
    }


    @Singleton
    @Provides
    fun provideRoomHelper(roomHelper: RoomHelperImpl): RoomHelper {
        return roomHelper
    }

    @Singleton
    @Provides
    fun provideTranslateLanguage(@ApplicationContext context: Context, database: NCDMergerDatabase): TranslateLanguage {
        return TranslateLanguage(database,context)
    }



}