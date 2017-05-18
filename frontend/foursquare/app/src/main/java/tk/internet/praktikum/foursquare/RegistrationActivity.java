package tk.internet.praktikum.foursquare;

import android.app.LoaderManager.LoaderCallbacks;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by truongtud on 17.05.2017.
 */

public class RegistrationActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
