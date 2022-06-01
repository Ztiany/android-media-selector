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
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.collection.ArrayMap;

import com.bilibili.boxing.model.BoxingManager;
import com.bilibili.boxing.model.callback.IMediaTaskCallback;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing.model.task.IMediaTask;
import com.bilibili.boxing.utils.BoxingExecutor;
import com.bilibili.boxing.utils.BoxingLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * A Task to load photos.
 *
 * @author ChenSL
 */
@WorkerThread
public class ImageTask implements IMediaTask<ImageMedia> {

    private static final String CONJUNCTION_SQL = "=? or";

    private static final String SELECTION_IMAGE_MIME_TYPE =
            Images.Media.MIME_TYPE + CONJUNCTION_SQL
                    + " "
                    + Images.Media.MIME_TYPE + CONJUNCTION_SQL
                    + " "
                    + Images.Media.MIME_TYPE + CONJUNCTION_SQL
                    + " "
                    + Images.Media.MIME_TYPE + "=?";

    private static final String SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF =
            Images.Media.MIME_TYPE + CONJUNCTION_SQL
                    + " "
                    + Images.Media.MIME_TYPE + CONJUNCTION_SQL
                    + " "
                    + Images.Media.MIME_TYPE
                    + "=?";

    private static final String SELECTION_ID = Images.Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE + " )";

    private static final String SELECTION_ID_WITHOUT_GIF =
            Images.Media.BUCKET_ID + "=? and (" + SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF + " )";

    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_JPG = "image/jpg";
    private static final String IMAGE_GIF = "image/gif";

    private static final String[] SELECTION_ARGS_IMAGE_MIME_TYPE = {IMAGE_JPEG, IMAGE_PNG, IMAGE_JPG, IMAGE_GIF};
    private static final String[] SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF = {IMAGE_JPEG, IMAGE_PNG, IMAGE_JPG};

    private static final String DESC = " desc";

    private final BoxingConfig mPickerConfig;
    private final Map<String, Uri> mThumbnailMap;

    public ImageTask() {
        this.mThumbnailMap = new ArrayMap<>();
        this.mPickerConfig = BoxingManager.getInstance().getBoxingConfig();
    }

    @Override
    public void load(@NonNull final ContentResolver cr, final int page, final String id, @NonNull final IMediaTaskCallback<ImageMedia> callback) {
        buildThumbnail(cr);
        buildAlbumList(cr, id, page, callback);
    }

    private void buildThumbnail(ContentResolver cr) {
        buildThumbnailVersionChecked(cr);
    }

    private void buildThumbnailVersionChecked(ContentResolver contentResolver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryThumbnailsAboveAndroidQ(contentResolver);
        } else {
            queryThumbnailsBelowAndroidQ(contentResolver);
        }
    }

    private void queryThumbnailsBelowAndroidQ(ContentResolver cr) {
        Timber.d("queryThumbnailsBelowAndroidQ");

        String[] projection = {Images.Thumbnails.IMAGE_ID, Images.Thumbnails.DATA};
        try (Cursor cur = Images.Thumbnails.queryMiniThumbnails(cr, Images.Thumbnails.EXTERNAL_CONTENT_URI, Images.Thumbnails.MINI_KIND, projection)) {
            if (cur != null && cur.moveToFirst()) {
                int idIndex = cur.getColumnIndex(Images.Thumbnails.IMAGE_ID);
                int dataIndex = cur.getColumnIndex(Images.Thumbnails.DATA);
                do {
                    String imageId = cur.getString(idIndex);
                    String imagePath = cur.getString(dataIndex);
                    mThumbnailMap.put(imageId, Uri.fromFile(new File(imagePath)));
                } while (cur.moveToNext() && !cur.isLast());
            }
        }

        Timber.d("queryThumbnailsBelowAndroidQ returning %d", mThumbnailMap.size());
    }

    private void queryThumbnailsAboveAndroidQ(ContentResolver cr) {
        Timber.d("queryThumbnailsAboveAndroidQ");

        String[] projection = {Images.Thumbnails.IMAGE_ID};
        try (Cursor cur = Images.Thumbnails.queryMiniThumbnails(cr, Images.Thumbnails.EXTERNAL_CONTENT_URI, Images.Thumbnails.MINI_KIND, projection)) {
            if (cur != null && cur.moveToFirst()) {
                int idIndex = cur.getColumnIndex(Images.Thumbnails.IMAGE_ID);
                do {
                    String imageId = cur.getString(idIndex);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.parseInt(imageId));
                    mThumbnailMap.put(imageId, imageUri);
                } while (cur.moveToNext() && !cur.isLast());
            }
        }

        Timber.d("queryThumbnailsAboveAndroidQ returning %d", mThumbnailMap.size());
    }

    private void buildAlbumList(
            ContentResolver cr,
            String bucketId,
            int page,
            @NonNull IMediaTaskCallback<ImageMedia> callback
    ) {

        Timber.d("buildAlbumList");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            buildAlbumListBelowAndroidQ(cr, bucketId, page, callback);
        } else {
            buildAlbumListAboveAndroidQ(cr, bucketId, page, callback);
        }
    }

    private void buildAlbumListBelowAndroidQ(
            ContentResolver cr,
            String bucketId,
            int page,
            @NonNull IMediaTaskCallback<ImageMedia> callback
    ) {

        Timber.d("buildAlbumListBelowAndroidQ");

        List<ImageMedia> result = new ArrayList<>();

        String[] columns;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            columns = new String[]{
                    Images.Media._ID,
                    Images.Media.DATA,
                    Images.Media.SIZE,
                    Images.Media.MIME_TYPE,
                    Images.Media.WIDTH,
                    Images.Media.HEIGHT};
        } else {
            columns = new String[]{
                    Images.Media._ID,
                    Images.Media.DATA,
                    Images.Media.SIZE,
                    Images.Media.MIME_TYPE};
        }

        Cursor cursor = null;

        try {
            boolean isDefaultAlbum = TextUtils.isEmpty(bucketId);
            boolean isNeedPaging = mPickerConfig == null || mPickerConfig.isNeedPaging();
            boolean isNeedGif = mPickerConfig != null && mPickerConfig.isNeedGif();
            int totalCount = getTotalCount(cr, bucketId, columns, isDefaultAlbum, isNeedGif);

            String imageMimeType = isNeedGif ? SELECTION_IMAGE_MIME_TYPE : SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF;

            String[] args = isNeedGif ? SELECTION_ARGS_IMAGE_MIME_TYPE : SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF;

            String order = isNeedPaging ? Images.Media.DATE_MODIFIED +
                    DESC + " LIMIT " + page * IMediaTask.PAGE_LIMIT + " , " +
                    IMediaTask.PAGE_LIMIT : Images.Media.DATE_MODIFIED + DESC;

            String selectionId = isNeedGif ? SELECTION_ID : SELECTION_ID_WITHOUT_GIF;

            Uri contentUri = UriUtils.getExternalImageUriVersionChecked();
            if (isDefaultAlbum) {
                cursor = cr.query(contentUri, columns, imageMimeType, args, order);
            } else {
                if (isNeedGif) {
                    cursor = cr.query(contentUri, columns, selectionId, new String[]{bucketId, args[0], args[1], args[2], args[3]}, order);
                } else {
                    cursor = cr.query(contentUri, columns, selectionId, new String[]{bucketId, args[0], args[1], args[2]}, order);
                }
            }

            if (cursor != null && cursor.moveToFirst()) {

                int idIndex = cursor.getColumnIndex(Images.Media._ID);
                int sizeIndex = cursor.getColumnIndex(Images.Media.SIZE);
                int typeIndex = cursor.getColumnIndex(Images.Media.MIME_TYPE);
                int widthIndex = cursor.getColumnIndex(Images.Media.WIDTH);
                int heightIndex = cursor.getColumnIndex(Images.Media.HEIGHT);
                int dataIndex = cursor.getColumnIndex(Images.Media.DATA);

                do {
                    String picPath = cursor.getString(dataIndex);

                    Uri uri = Uri.fromFile(new File(picPath));
                    long size = cursor.getLong(sizeIndex);
                    String mimeType = cursor.getString(typeIndex);
                    String id = cursor.getString(idIndex);

                    int width = 0;
                    int height = 0;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        width = cursor.getInt(widthIndex);
                        height = cursor.getInt(heightIndex);
                    }

                    ImageMedia imageItem = new ImageMedia.Builder(id, uri)
                            .setThumbnailPath(mThumbnailMap.get(id))
                            .setSize(String.valueOf(size))
                            .setMimeType(mimeType)
                            .setHeight(height)
                            .setWidth(width)
                            .build();

                    if (callback.needFilter(imageItem)) {
                        BoxingLog.d("path:" + picPath + " has been filter");
                    } else if (!result.contains(imageItem)) {
                        result.add(imageItem);
                    }

                } while (!cursor.isLast() && cursor.moveToNext());

                postMedias(result, totalCount, callback);
            } else {
                postMedias(result, 0, callback);
            }

            clear();

        } catch (Exception e) {
            Timber.e(e, "buildAlbumListBelowAndroidQ crash");
        } finally {
            Timber.d("buildAlbumListBelowAndroidQ finally (result size = %d)", result.size());
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildAlbumListAboveAndroidQ(
            ContentResolver cr,
            String bucketId,
            int page,
            IMediaTaskCallback<ImageMedia> callback
    ) {

        Timber.d("buildAlbumListAboveAndroidQ");

        List<ImageMedia> result = new ArrayList<>();

        String[] columns = new String[]{
                Images.Media._ID,
                Images.Media.SIZE,
                Images.Media.MIME_TYPE,
                Images.Media.WIDTH,
                Images.Media.HEIGHT
        };

        Cursor cursor = null;

        try {
            boolean isDefaultAlbum = TextUtils.isEmpty(bucketId);
            boolean isNeedPaging = mPickerConfig == null || mPickerConfig.isNeedPaging();
            boolean isNeedGif = mPickerConfig != null && mPickerConfig.isNeedGif();
            int totalCount = getTotalCount(cr, bucketId, columns, isDefaultAlbum, isNeedGif);

            Uri contentUri = UriUtils.getExternalImageUriVersionChecked();
            Bundle bundle = new Bundle();

            /*
            fix invalid token limit android 11.
                referring https://stackoverflow.com/questions/10390577/limiting-number-of-rows-in-a-contentresolver-query-function
             */
            if (isDefaultAlbum) {
                if (isNeedGif) {
                    bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, SELECTION_IMAGE_MIME_TYPE);
                    bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, SELECTION_ARGS_IMAGE_MIME_TYPE);
                } else {
                    bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, SELECTION_IMAGE_MIME_TYPE_WITHOUT_GIF);
                    bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF);
                }
            } else {
                if (isNeedGif) {
                    String[] args = SELECTION_ARGS_IMAGE_MIME_TYPE;
                    bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, SELECTION_ID);
                    bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, new String[]{bucketId, args[0], args[1], args[2], args[3]});
                } else {
                    String[] args = SELECTION_ARGS_IMAGE_MIME_TYPE_WITHOUT_GIF;
                    bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, SELECTION_ID_WITHOUT_GIF);
                    bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, new String[]{bucketId, args[0], args[1], args[2]});
                }
            }
            if (isNeedPaging) {
                bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, IMediaTask.PAGE_LIMIT);
                bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, page * IMediaTask.PAGE_LIMIT);
            }
            bundle.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
            bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{Images.Media.DATE_MODIFIED});

            cursor = cr.query(contentUri, columns, bundle, null);

            if (cursor != null && cursor.moveToFirst()) {

                int idIndex = cursor.getColumnIndex(Images.Media._ID);
                int sizeIndex = cursor.getColumnIndex(Images.Media.SIZE);
                int typeIndex = cursor.getColumnIndex(Images.Media.MIME_TYPE);
                int widthIndex = cursor.getColumnIndex(Images.Media.WIDTH);
                int heightIndex = cursor.getColumnIndex(Images.Media.HEIGHT);

                do {
                    String id = cursor.getString(idIndex);
                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.parseInt(id));
                    long size = cursor.getLong(sizeIndex);
                    String mimeType = cursor.getString(typeIndex);
                    int width = cursor.getInt(widthIndex);
                    int height = cursor.getInt(heightIndex);

                    ImageMedia imageItem = new ImageMedia.Builder(id, uri)
                            .setThumbnailPath(mThumbnailMap.get(id))
                            .setSize(String.valueOf(size))
                            .setMimeType(mimeType)
                            .setHeight(height)
                            .setWidth(width)
                            .build();

                    if (callback.needFilter(imageItem)) {
                        BoxingLog.d("uri:" + uri + " has been filter");
                    } else if (!result.contains(imageItem)) {
                        result.add(imageItem);
                    }

                } while (!cursor.isLast() && cursor.moveToNext());

                postMedias(result, totalCount, callback);
            } else {
                postMedias(result, 0, callback);
            }

            clear();

        } catch (Exception e) {
            Timber.e(e, "buildAlbumListAboveAndroidQ crash");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            Timber.d("buildAlbumListAboveAndroidQ finally (result size = %d)", result.size());
        }
    }

    private void postMedias(final List<ImageMedia> result, final int count, @NonNull final IMediaTaskCallback<ImageMedia> callback) {
        Timber.d("postMedias (result size = %d)", count);
        BoxingExecutor.getInstance().runUI(() -> callback.postMedia(result, count));
    }

    private int getTotalCount(ContentResolver cr, String bucketId, String[] columns, boolean isDefaultAlbum, boolean isNeedGif) {
        Cursor allCursor = null;
        int result = 0;

        try {
            if (isDefaultAlbum) {
                allCursor = cr.query(
                        Images.Media.EXTERNAL_CONTENT_URI,
                        columns,
                        SELECTION_IMAGE_MIME_TYPE,
                        SELECTION_ARGS_IMAGE_MIME_TYPE,
                        Images.Media.DATE_MODIFIED + DESC
                );
            } else {
                if (isNeedGif) {
                    allCursor = cr.query(
                            Images.Media.EXTERNAL_CONTENT_URI,
                            columns,
                            SELECTION_ID,
                            new String[]{bucketId, IMAGE_JPEG, IMAGE_PNG, IMAGE_JPG, IMAGE_GIF},
                            Images.Media.DATE_MODIFIED + DESC
                    );
                } else {
                    allCursor = cr.query(
                            Images.Media.EXTERNAL_CONTENT_URI,
                            columns, SELECTION_ID_WITHOUT_GIF,
                            new String[]{bucketId, IMAGE_JPEG, IMAGE_PNG, IMAGE_JPG},
                            Images.Media.DATE_MODIFIED + DESC
                    );
                }
            }
            if (allCursor != null) {
                result = allCursor.getCount();
            }
        } finally {
            if (allCursor != null) {
                allCursor.close();
            }
        }
        return result;
    }

    private void clear() {
        if (mThumbnailMap != null) {
            mThumbnailMap.clear();
        }
    }

}