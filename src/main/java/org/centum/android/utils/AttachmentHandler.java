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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.LruCache;
import android.widget.Toast;

import org.centum.android.model.Card;
import org.centum.android.model.Stack;
import org.centum.android.model.StackManager;
import org.centum.android.stack.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Phani on 1/23/14.
 */
public class AttachmentHandler {

    private static final int MAX_CACHE_SIZE = 2 * 1024 * 1024; //2 mb cache
    private static final int MAX_IMAGE_DIMENSION = 256;
    private static AttachmentHandler instance = null;

    private final Context mContext;
    private final LruCache<String, Bitmap> mScaledCache; //Cache of scaled attachments
    private final String mCacheDir;

    private AttachmentHandler(Context ctx) {
        mContext = ctx;
        mCacheDir = Environment.getExternalStorageDirectory() + File.separator + "Stacks" + File.separator + "Attachments";
        new File(mCacheDir).mkdirs();

        mScaledCache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public static AttachmentHandler get(Context ctx) {
        if (instance == null) {
            instance = new AttachmentHandler(ctx);
        }
        return instance;
    }

    public void showBitmap(String name) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + mCacheDir + File.separator + name), "image/*");
        getContext().startActivity(intent);
    }

    public boolean cacheDirContains(String name) {
        return new File(mCacheDir, name).exists();
    }

    public Bitmap getScaledBitmap(String name) throws IOException {
        if (name == null) {
            return null;
        }
        if (mScaledCache.get(name) == null) {
            addBitmapToCache(loadScaledBitmap(getCacheDir(), name), name);
        }
        return mScaledCache.get(name);
    }

    public Bitmap getFullBitmap(String name) throws IOException {
        if (name == null) {
            return null;
        }
        return loadBitmapFromCacheDir(name);
    }

    public void copyToCacheDir(String dir, String name) throws IOException {
        if (isExternalStorageReadable() && isExternalStorageWritable() && !dir.equals(getCacheDir())) {
            File file = new File(dir, name);
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                FileOutputStream fileOutputStream = new FileOutputStream(new File(getCacheDir(), name));

                byte buffer[] = new byte[1024];
                int read;
                while ((read = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                }

                fileInputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
    }

    public String loadBitmapFromURL(String url) throws IOException {
        return loadBitmapFromURL(new URL(url));
    }

    public String loadBitmapFromURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();

        String name = url.getFile().replace("/", "").replace("\\", "");

        InputStream inputStream = connection.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(getCacheDir(), name));

        byte buffer[] = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, read);
        }

        return name;
    }

    public boolean writeBitmapToCacheDir(Bitmap bitmap, String name) throws IOException {
        return writeBitmap(bitmap, mCacheDir, name);
    }

    private boolean writeBitmap(Bitmap bitmap, String dir, String name) throws IOException {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File file = new File(dir, name);
            if (bitmap != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
                fileOutputStream.close();
                return true;
            }
        }
        return false;
    }

    private Bitmap loadBitmapFromCacheDir(String name) throws IOException {
        return loadBitmap(mCacheDir, name);
    }

    private Bitmap loadBitmap(String dir, String name) throws IOException {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File file = new File(dir, name);
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                fileInputStream.close();
                return bitmap;
            }
        }
        return null;
    }

    private Bitmap loadScaledBitmap(String dir, String name) throws IOException {
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File file = new File(dir, name);
            if (file.exists()) {
                int maxImageSize = MAX_IMAGE_DIMENSION * MAX_IMAGE_DIMENSION;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                InputStream inputStream = new FileInputStream(file);
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                int imageSize = options.outWidth * options.outHeight;
                int sampleSize = 1;
                if (imageSize <= maxImageSize) {
                    return loadBitmap(dir, name);
                } else {
                    while (imageSize / Math.pow(sampleSize, 2) > maxImageSize) {
                        sampleSize++;
                    }
                    options = new BitmapFactory.Options();
                    options.inSampleSize = sampleSize;
                    inputStream = new FileInputStream(file);
                    return BitmapFactory.decodeStream(inputStream, null, options);
                }
            }
        }
        return null;
    }

    private boolean addBitmapToCache(Bitmap bitmap, String name) {
        if (bitmap != null && name != null) {
            int width = bitmap.getWidth();
            int oWidth = width;
            int height = bitmap.getHeight();
            int oHeight = height;
            if (width * height > MAX_IMAGE_DIMENSION * MAX_IMAGE_DIMENSION) {
                if (width > MAX_IMAGE_DIMENSION) {
                    width = MAX_IMAGE_DIMENSION;
                    height = (width * oHeight) / oWidth;
                }
                if (height > MAX_IMAGE_DIMENSION) {
                    height = MAX_IMAGE_DIMENSION;
                    width = (height * oWidth) / oHeight;
                }
            }
            mScaledCache.put(name, Bitmap.createScaledBitmap(bitmap, width, height, true));
            return true;
        }
        return false;
    }

    public boolean cacheContains(String key) {
        if (key == null) {
            return false;
        }
        return mScaledCache.get(key) != null;
    }

    public void cleanCacheDir() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                List<Stack> stacks = StackManager.get().getStackList();
                List<String> currentAttachments = new LinkedList<String>();

                for (Stack stack : stacks) {
                    for (Card c : stack.getCardList()) {
                        if (c.hasAttachment() && !currentAttachments.contains(c.getAttachment())) {
                            currentAttachments.add(c.getAttachment());
                        }
                    }
                    for (Card c : stack.getArchivedCards()) {
                        if (c.hasAttachment() && !currentAttachments.contains(c.getAttachment())) {
                            currentAttachments.add(c.getAttachment());
                        }
                    }
                }

                stacks = StackManager.get().getArchivedStacksList();
                for (Stack stack : stacks) {
                    for (Card c : stack.getCardList()) {
                        if (c.hasAttachment() && !currentAttachments.contains(c.getAttachment())) {
                            currentAttachments.add(c.getAttachment());
                        }
                    }
                    for (Card c : stack.getArchivedCards()) {
                        if (c.hasAttachment() && !currentAttachments.contains(c.getAttachment())) {
                            currentAttachments.add(c.getAttachment());
                        }
                    }
                }

                File files[] = new File(mCacheDir).listFiles();
                for (File f : files) {
                    if (!currentAttachments.contains(f.getName())) {
                        f.delete();
                    }
                }
                return null;
            }

            @Override
            public void onPostExecute(Void result) {
                try {
                    Toast.makeText(getContext(), R.string.cleaned_attachment_directory, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    //incase something wrong with the context
                }
            }
        }.execute();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public String getCacheDir() {
        return mCacheDir;
    }

    private Context getContext() {
        return mContext;
    }
}
