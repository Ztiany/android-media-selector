/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.bilibili.boxing.model.task.impl;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.collection.ArrayMap;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.callback.IAlbumTaskCallback;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.utils.BoxingExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A task to load albums.
 *
 * @author ChenSL
 */
@WorkerThread
public class AlbumTask {

    private static final String UNKNOWN_ALBUM_NAME = "unknow";
    private static final String SELECTION_IMAGE_MIME_TYPE = Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=?";
    private static final String SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF = Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=? or " + Media.MIME_TYPE + "=?";
    private static final String SELECTION_ID = Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE + " )";
    private static final String SELECTION_ID_WITHOUT_GIF = Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF + " )";
    private static final String[] SELECTION_ARGS_IMAGE_MIME_TYPE = {"image/jpeg", "image/png", "image/jpg", "image/gif"};
    private static final String[] SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF = {"image/jpeg", "image/png", "image/jpg"};

    private int mUnknownAlbumNumber = 1;
    private final Map<String, AlbumEntity> mBucketMap;
    private final AlbumEntity mDefaultAlbum;
    private final BoxingConfig mPickerConfig;

    public AlbumTask() {
        this.mBucketMap = new ArrayMap<>();
        this.mDefaultAlbum = AlbumEntity.createDefaultAlbum();
        this.mPickerConfig = BoxingManager.getInstance().getBoxingConfig();
    }

    public void start(@NonNull final ContentResolver cr, @NonNull final IAlbumTaskCallback callback) {
        buildAlbumInfo(cr);
        buildAndPostAlbumList(callback);
    }

    private void buildAlbumInfo(ContentResolver cr) {
        String[] distinctBucketColumns = new String[]{Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME};

        Uri mediaCollection = UriUtils.getExternalImageUriVersionChecked();

        try (Cursor bucketCursor = cr.query(mediaCollection, distinctBucketColumns, null, null, Media.DATE_MODIFIED + " desc")) {

            if (bucketCursor == null) {
                return;
            }

            HashSet<AlbumItem> hashSet = new HashSet<>();
            int bucketCursorColumnIndex = bucketCursor.getColumnIndex(Media.BUCKET_ID);
            int displayNameColumnIndex = bucketCursor.getColumnIndex(Media.BUCKET_DISPLAY_NAME);

            if (bucketCursor.moveToFirst()) {
                do {
                    String buckId = bucketCursor.getString(bucketCursorColumnIndex);
                    String name = bucketCursor.getString(displayNameColumnIndex);
                    if (!TextUtils.isEmpty(buckId)) {
                        hashSet.add(new AlbumItem(buckId, name));
                    }
                } while (bucketCursor.moveToNext());
            }

            for (AlbumItem albumItem : hashSet) {
                if (!TextUtils.isEmpty(albumItem.bucketId)) {
                    AlbumEntity album = buildAlbumInfo(albumItem.albumName, albumItem.bucketId);
                    buildAlbumCoverVersionChecked(cr, albumItem.bucketId, album);
                }
            }
        }
    }

    private void buildAlbumCoverVersionChecked(ContentResolver cr, String buckId, AlbumEntity album) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            buildAlbumCoverAboveAndroidQ(cr, buckId, album);
        } else {
            buildAlbumCoverBelowAndroidQ(cr, buckId, album);
        }
    }

    private void buildAndPostAlbumList(@NonNull final IAlbumTaskCallback callback) {
        mDefaultAlbum.mCount = 0;
        List<AlbumEntity> tmpList = new ArrayList<>();
        if (mBucketMap == null) {
            postAlbums(callback, tmpList);
            return;
        }
        for (Map.Entry<String, AlbumEntity> entry : mBucketMap.entrySet()) {
            tmpList.add(entry.getValue());
            mDefaultAlbum.mCount += entry.getValue().mCount;
        }
        if (tmpList.size() > 0 && tmpList.get(0) != null) {
            mDefaultAlbum.mImageList = tmpList.get(0).mImageList;
            tmpList.add(0, mDefaultAlbum);
        }
        postAlbums(callback, tmpList);
        clear();
    }

    private void postAlbums(@NonNull final IAlbumTaskCallback callback, final List<AlbumEntity> result) {
        BoxingExecutor.getInstance().runUI(() -> callback.postAlbumList(result));
    }

    @NonNull
    private AlbumEntity buildAlbumInfo(String bucketName, String bucketId) {
        AlbumEntity album = null;

        if (!TextUtils.isEmpty(bucketId)) {
            album = mBucketMap.get(bucketId);
        }

        if (album == null) {
            album = new AlbumEntity();
            if (!TextUtils.isEmpty(bucketId)) {
                album.mBucketId = bucketId;
            } else {
                album.mBucketId = String.valueOf(mUnknownAlbumNumber);
                mUnknownAlbumNumber++;
            }
            if (!TextUtils.isEmpty(bucketName)) {
                album.mBucketName = bucketName;
            } else {
                album.mBucketName = UNKNOWN_ALBUM_NAME;
                mUnknownAlbumNumber++;
            }
            if (album.mImageList.size() > 0) {
                mBucketMap.put(bucketId, album);
            }
        }
        return album;
    }

    private void clear() {
        if (mBucketMap != null) {
            mBucketMap.clear();
        }
    }

    /**
     * get the cover and count
     *
     * @param buckId album id
     */
    private void buildAlbumCoverBelowAndroidQ(ContentResolver cr, String buckId, AlbumEntity album) {
        String[] photoColumn = new String[]{Media._ID, Media.DATA};
        boolean isNeedGif = mPickerConfig != null && mPickerConfig.isNeedGif();
        String selectionId = isNeedGif ? SELECTION_ID : SELECTION_ID_WITHOUT_GIF;
        String[] args = isNeedGif ? SELECTION_ARGS_IMAGE_MIME_TYPE : SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF;
        String[] selectionArgs = new String[args.length + 1];
        selectionArgs[0] = buckId;

        System.arraycopy(args, 0, selectionArgs, 1, selectionArgs.length - 1);

        try (Cursor coverCursor = cr.query(Media.EXTERNAL_CONTENT_URI, photoColumn, selectionId, selectionArgs, Media.DATE_MODIFIED + " desc")) {
            if (coverCursor != null && coverCursor.moveToFirst()) {
                int dataIndex = coverCursor.getColumnIndex(Media.DATA);
                int idIndex = coverCursor.getColumnIndex(Media._ID);

                String picPath = coverCursor.getString(dataIndex);
                String id = coverCursor.getString(idIndex);

                album.mCount = coverCursor.getCount();
                album.mImageList.add(new ImageMedia(id, Uri.fromFile(new File(picPath))));
                album.mImageList.size();

                mBucketMap.put(buckId, album);
            }
        }
    }

    private void buildAlbumCoverAboveAndroidQ(ContentResolver cr, String buckId, AlbumEntity album) {
        String[] photoColumn = new String[]{Media._ID};
        boolean isNeedGif = mPickerConfig != null && mPickerConfig.isNeedGif();

        String selectionId = isNeedGif ? SELECTION_ID : SELECTION_ID_WITHOUT_GIF;
        String[] args = isNeedGif ? SELECTION_ARGS_IMAGE_MIME_TYPE : SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF;
        String[] selectionArgs = new String[args.length + 1];
        selectionArgs[0] = buckId;

        System.arraycopy(args, 0, selectionArgs, 1, selectionArgs.length - 1);

        try (Cursor coverCursor = cr.query(Media.EXTERNAL_CONTENT_URI, photoColumn, selectionId, selectionArgs, Media.DATE_MODIFIED + " desc")) {
            if (coverCursor != null && coverCursor.moveToFirst()) {
                int idIndex = coverCursor.getColumnIndex(Media._ID);
                String id = coverCursor.getString(idIndex);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.parseInt(id));
                album.mCount = coverCursor.getCount();
                album.mImageList.add(new ImageMedia(id, contentUri));
                album.mImageList.size();
                mBucketMap.put(buckId, album);
            }
        }
    }

    private static class AlbumItem {

        final String bucketId;
        final String albumName;

        private AlbumItem(String bucketId, String albumName) {
            this.bucketId = bucketId;
            this.albumName = albumName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AlbumItem albumItem = (AlbumItem) o;
            return Objects.equals(bucketId, albumItem.bucketId) && Objects.equals(albumName, albumItem.albumName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bucketId, albumName);
        }
    }

}