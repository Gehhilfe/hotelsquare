package tk.internet.praktikum.foursquare.api;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.ImageComment;
import tk.internet.praktikum.foursquare.api.bean.TextComment;


public class ServiceFactory {

    private static String stToken = "";

    /**
     * service factory for creating retrofit services for all server apis
     *
     * @param clazz    class of created service
     * @param endpoint web url of server
     * @param <T>      class of returned service
     * @return service
     */
    public static <T> T createRetrofitService(final Class<T> clazz, final String endpoint, final String token) {

        GsonBuilder gsonBuilder = new GsonBuilder();
        CommentDeserializer deserializer = new CommentDeserializer("kind");
        deserializer.registerComment("TextComment", TextComment.class);
        deserializer.registerComment("ImageComment", ImageComment.class);
        gsonBuilder.registerTypeAdapter(Comment.class, deserializer);
        stToken = token;
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        if(stToken == null)
                            return chain.proceed(chain.request());
                        else
                            return chain.proceed(chain.request().newBuilder().addHeader("x-auth", stToken).build());
                    }
                })
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        /**
        if (token != null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder().addHeader("x-auth", token).build();
                            return chain.proceed(request);
                        }
                    })
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();
        } else {
            client = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();
        }*/

        final Retrofit retrofit = new Retrofit.Builder().baseUrl(endpoint)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .client(client)
                .build();

        return retrofit.create(clazz);
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
