package us.laderman.android.blackjack.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/"; // Android emulator localhost
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = DEFAULT_BASE_URL;

    public static Retrofit getClient() {
        return getClient(currentBaseUrl);
    }

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null || !currentBaseUrl.equals(baseUrl)) {
            currentBaseUrl = baseUrl;

            // Add logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static BlackjackApiService getBlackjackService() {
        return getClient().create(BlackjackApiService.class);
    }

    public static BlackjackApiService getBlackjackService(String baseUrl) {
        return getClient(baseUrl).create(BlackjackApiService.class);
    }
}
