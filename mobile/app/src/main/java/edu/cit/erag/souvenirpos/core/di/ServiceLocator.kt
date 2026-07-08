package edu.cit.erag.souvenirpos.core.di

import android.content.Context
import edu.cit.erag.souvenirpos.BuildConfig
import edu.cit.erag.souvenirpos.core.data.TokenStore
import edu.cit.erag.souvenirpos.core.network.ApiService
import edu.cit.erag.souvenirpos.core.network.AuthInterceptor
import edu.cit.erag.souvenirpos.feature.auth.AuthRepository
import edu.cit.erag.souvenirpos.feature.pos.PosRepository
import edu.cit.erag.souvenirpos.data.repository.UserRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Minimal manual dependency container. Initialized once in SouvenirPosApp.onCreate,
 * then read by the ViewModels. Keeps the project free of a DI framework for a small app.
 */
object ServiceLocator {

    lateinit var tokenStore: TokenStore
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var posRepository: PosRepository
        private set
    lateinit var userRepository: UserRepository
        private set

    fun init(context: Context) {
        tokenStore = TokenStore(context.applicationContext)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenStore.token() })
            .addInterceptor(logging)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        authRepository = AuthRepository(api, tokenStore)
        posRepository = PosRepository(api)
        userRepository = UserRepository(api)
    }
}
