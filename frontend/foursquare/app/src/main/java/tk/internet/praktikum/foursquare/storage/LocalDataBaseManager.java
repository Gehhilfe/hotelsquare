package tk.internet.praktikum.foursquare.storage;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import tk.internet.praktikum.foursquare.history.DaoMaster;
import tk.internet.praktikum.foursquare.history.DaoSession;


public class LocalDataBaseManager {
    private Context context;
    private   DaoSession daoSession;
    private static  LocalDataBaseManager localDataBaseManager;

    public static LocalDataBaseManager getLocalDatabaseManager(Context context){
        return localDataBaseManager!=null?  localDataBaseManager:new LocalDataBaseManager(context);
    }
    public LocalDataBaseManager(Context context){
         this.context=context;
         initDatabase();
    }
    public void initDatabase(){
        DaoMaster.DevOpenHelper helper=new DaoMaster.DevOpenHelper(context,"history-db");
        Database database=helper.getWritableDb();
        daoSession=new DaoMaster(database).newSession();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public  DaoSession getDaoSession() {
        return daoSession;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
    }
}
