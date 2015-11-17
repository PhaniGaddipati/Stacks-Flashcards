package org.centum.android.sql;

import android.content.Context;
import android.os.AsyncTask;

import org.centum.android.model.LoadedData;

/**
 * Created by Phani on 5/9/2014.
 */
public abstract class StacksDatabaseLoadAsyncTask extends AsyncTask<Void, Void, LoadedData> {

    private final Context context;
    private LoadedData loadedData;

    public StacksDatabaseLoadAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    public abstract void onPreExecute();

    @Override
    public abstract void onPostExecute(LoadedData loadedData);

    @Override
    protected LoadedData doInBackground(Void... params) {
        return StacksDatabaseHelper.get(context).loadStacks();
    }
}
