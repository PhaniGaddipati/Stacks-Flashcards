/**
 Stacks Flashcards - A flashcards application for Android devices 4.0+
 Copyright (C) 2014  Phani Gaddipati

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.centum.android.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class provides an easy way to write and read the entire model to/from the disk
 * Created by Phani on 1/25/14.
 */
public class Serializer {

    private static final int BACKUP_INTERVAL = 5;
    private static final int externalizableVersion = 1;
    private static Serializer instance = null;
    private Context context;
    private boolean isSaving = false;
    private File saveDir;
    private int save = 0;

    private Serializer(Context context) {
        this.context = context;
        saveDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Stacks");
        saveDir.mkdirs();
    }

    public LoadedData loadData(String name) {
        long start = System.currentTimeMillis();
        LoadedData loadedData = new LoadedData();
        try {
            FileInputStream fis;
            try {
                fis = new FileInputStream(new File(saveDir, name));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                fis = context.openFileInput("stacks");
            }
            ObjectInputStream ois = new ObjectInputStream(fis);

            int ver = ois.readInt();
            int stack = ois.readInt();
            int archived = ois.readInt();

            for (int i = 0; i < stack; i++) {
                //StackManager.get().addStack((Stack) ois.readObject());
                loadedData.addLoadedStack((Stack) ois.readObject());
            }
            for (int i = 0; i < archived; i++) {
                //StackManager.get().addArchiveStack((Stack) ois.readObject());
                loadedData.addLoadedArchivedStack((Stack) ois.readObject());
            }

            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long stop = System.currentTimeMillis();
        Log.d("Serializer", "Loaded data in " + (stop - start) + " ms");

        return loadedData;
    }

    private synchronized boolean isSaving() {
        return isSaving;
    }

    private synchronized void setSaving(boolean saving) {
        isSaving = saving;
    }

    public void saveData() {
        writeData("stacks.stk");
    }

    public void backup() {
        writeData("stacks_bak.stk");
    }

    public void writeData(final String name) {
        if (!isSaving()) {
            setSaving(true);
            String tmpName = name + ".tmp";
            long start = System.currentTimeMillis();
            try {
                File tmpFile = new File(saveDir, tmpName);
                File savedFile = new File(saveDir, name);
                FileOutputStream fos = new FileOutputStream(tmpFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeInt(externalizableVersion);
                oos.writeInt(StackManager.get().getNumberOfStacks());
                oos.writeInt(StackManager.get().getNumberOfArchivedStacks());
                for (int i = 0; i < StackManager.get().getNumberOfStacks(); i++) {
                    oos.writeObject(StackManager.get().getStack(i));
                }
                for (int i = 0; i < StackManager.get().getNumberOfArchivedStacks(); i++) {
                    oos.writeObject(StackManager.get().getArchivedStacks()[i]);
                }

                oos.flush();
                oos.close();

                tmpFile.renameTo(savedFile);
                tmpFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            long stop = System.currentTimeMillis();
            Log.d("Serializer", "Saved data in " + (stop - start) + " ms: " + name);
            setSaving(false);
        }
    }

    public static Serializer getInstance(Context context) {
        if (instance == null) {
            instance = new Serializer(context);
        }
        return instance;
    }

}
