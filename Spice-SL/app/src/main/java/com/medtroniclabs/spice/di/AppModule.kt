package com.medtroniclabs.spice.di

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.common.AppConstants
import com.medtroniclabs.spice.common.DefinedParams.ACTION_SESSION_EXPIRED
import com.medtroniclabs.spice.common.DefinedParams.SL_SESSION
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.SpiceDataBase
import com.medtroniclabs.spice.db.dao.AboveFiveYearsDAO
import com.medtroniclabs.spice.db.dao.AssessmentDAO
import com.medtroniclabs.spice.db.dao.DiagnosisDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.MemberClinicalDAO
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.dao.ExaminationsComplaintsDAO
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.local.RoomHelperImpl
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.ApiHelperImpl
import com.medtroniclabs.spice.network.ApiService
import com.medtroniclabs.spice.network.NetworkConstants
import com.medtroniclabs.spice.network.NetworkConstants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context) = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AppInterceptor(context))
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    } else {
        OkHttpClient.Builder()
            .addInterceptor(AppInterceptor(context))
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }


    class AppInterceptor(val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request: Request = chain.request()
            val requestBuilder = request.newBuilder()
                .header(
                    "Authorization",
                    SecuredPreference.getString(SecuredPreference.EnvironmentKey.TOKEN.toString())
                        ?: ""
                )
                .header("client", AppConstants.CLIENT_CONSTANT)

            request = requestBuilder.build()
            val response = chain.proceed(request)
            Timber.i("HEADERS ->\n${request.headers}")
            if (!request.url.toString().contains(NetworkConstants.AUTH_SESSION)
                && !response.isSuccessful && response.code == 401
            ) {
                redirectLogin(context)
            }
            return response
        }
    }

    private fun redirectLogin(context: Context) {
        val intent = Intent(ACTION_SESSION_EXPIRED)
        intent.putExtra(SL_SESSION, true)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
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
    fun provideRoomHelper(roomHelper: RoomHelperImpl): RoomHelper {
        return roomHelper
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): SpiceDataBase {
        return SpiceDataBase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideHouseholdDAO(db: SpiceDataBase): HouseholdDAO {
        return db.householdDAO()
    }

    @Singleton
    @Provides
    fun provideMemberDAO(db: SpiceDataBase): MemberDAO {
        return db.memberDAO()
    }

    @Singleton
    @Provides
    fun provideAssessmentDAO(db: SpiceDataBase): AssessmentDAO {
        return db.assessmentDAO()
    }

    @Singleton
    @Provides
    fun provideMetaDataDAO(db: SpiceDataBase): MetaDataDAO {
        return db.metaDataDAO()
    }

    @Singleton
    @Provides
    fun provideExaminationComplaintsDAO(db: SpiceDataBase): ExaminationsComplaintsDAO {
        return db.examinationsComplaintsDAO()
    }

    @Singleton
    @Provides
    fun provideDiagnosisDAO(db: SpiceDataBase): DiagnosisDAO {
        return db.diagnosisDAO()
    }

    @Singleton
    @Provides
    fun provideMemberClinicalDAO(db: SpiceDataBase): MemberClinicalDAO {
        return db.memberClinicalDAO()
    }

    @Singleton
    @Provides
    fun provideAboveFiveYearsDAO(db: SpiceDataBase): AboveFiveYearsDAO {
        return db.aboveFiveYearsDAO()
    }

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main


}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher