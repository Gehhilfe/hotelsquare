package tk.internet.praktikum.foursquare.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by robert on 14.05.2017.
 */

public class ServiceFactory {

    public static <T> T createRetrofitService(final Class<T> clazz, final String endpoint)
    {
        final Retrofit retrofit = new Retrofit.Builder().baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        T service = retrofit.create(clazz);

        return service;
    }

}
