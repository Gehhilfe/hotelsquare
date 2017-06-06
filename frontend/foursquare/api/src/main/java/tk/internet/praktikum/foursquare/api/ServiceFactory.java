package tk.internet.praktikum.foursquare.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServiceFactory {

    /**
     * service factory for creating retrofit services for all server apis
     *
     * @param clazz    class of created service
     * @param endpoint web url of server
     * @param <T>      class of returned service
     * @return service
     */
    public static <T> T createRetrofitService(final Class<T> clazz, final String endpoint, final String token) {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.connectTimeout(5, TimeUnit.SECONDS);

        if (token != null) {
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("x-auth", token).build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = builder.build();

        final Retrofit retrofit = new Retrofit.Builder().baseUrl(endpoint)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        T service = retrofit.create(clazz);

        return service;
    }

    /**
     * service factory for creating retrofit services for all server apis
     *
     * @param clazz    class of created service
     * @param endpoint web url of server
     * @param <T>      class of returned service
     * @return service
     */
    public static <T> T createRetrofitService(final Class<T> clazz, final String endpoint) {

        return createRetrofitService(clazz, endpoint, null);
    }


}
