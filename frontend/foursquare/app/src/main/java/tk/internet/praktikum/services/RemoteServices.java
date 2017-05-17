package tk.internet.praktikum.services;

import tk.internet.praktikum.foursquare.api.ServiceFactory;

/**
 * Created by truongtud on 16.05.2017.
 */

public class RemoteServices {
    public static <T> T createRemoteService(final Class<T> clazz, final String endpoint){
        return ServiceFactory.createRetrofitService(clazz,endpoint);
    }
}
