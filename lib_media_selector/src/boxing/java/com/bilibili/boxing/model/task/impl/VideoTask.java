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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

import com.bilibili.boxing.model.callback.IMediaTaskCallback;
import com.bilibili.boxing.model.entity.impl.VideoMedia;
import com.bilibili.boxing.model.task.IMediaTask;
import com.bilibili.boxing.utils.BoxingExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A Task to load {@link VideoMedia} in database.
 *
 * @author ChenSL
 */
@WorkerThread
public class VideoTask implements IMediaTask<VideoMedia> {

    @Override
    public void load(final ContentResolver cr, final int page, String id, final IMediaTaskCallback<VideoMedia> callback) {
        loadVideosVersionChecked(cr, page, callback);
    }

    private void loadVideosVersionChecked(ContentResolver cr, int page, @NonNull final IMediaTaskCallback<VideoMedia> callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            loadVideosBelowAndroidQ(cr, page, callback);
        } else {
            loadVideosAboveAndroidQ(cr, page, callback);
        }
    }

    private void loadVideosBelowAndroidQ(ContentResolver cr, int page, @NonNull final IMediaTaskCallback<VideoMedia> callback) {
        final List<VideoMedia> videoMedias = new ArrayList<>();

        String[] mediaCol = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DURATION
        };

        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc" + " LIMIT " + page * IMediaTask.PAGE_LIMIT + " , " + IMediaTask.PAGE_LIMIT;
        try (Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaCol, null, null, sortOrder)) {

            int count;
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getCount();

                int dataIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                int typeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                int dateIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

                do {
                    String data = cursor.getString(dataIndex);
                    String id = cursor.getString(idIndex);
                    String title = cursor.getString(titleIndex);
                    String type = cursor.getString(typeIndex);
                    String size = cursor.getString(sizeIndex);
                    String date = cursor.getString(dateIndex);
                    String duration = cursor.getString(durationIndex);

                    VideoMedia video = new VideoMedia.Builder(id, Uri.fromFile(new File(data)))
                            .setTitle(title)
                            .setDuration(duration)
                            .setSize(size)
                            .setDataTaken(date)
                            .setMimeType(type)
                            .build();

                    videoMedias.add(video);

                } while (!cursor.isLast() && cursor.moveToNext());
                postMedias(callback, videoMedias, count);
            } else {
                postMedias(callback, videoMedias, 0);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadVideosAboveAndroidQ(ContentResolver cr, int page, IMediaTaskCallback<VideoMedia> callback) {
        final List<VideoMedia> videoMedias = new ArrayList<>();

        String[] mediaCol = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DURATION
        };

        Bundle bundle = new Bundle();
        bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, IMediaTask.PAGE_LIMIT);
        bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, page * IMediaTask.PAGE_LIMIT);
        bundle.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
        bundle.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[]{MediaStore.Video.Media.DATE_MODIFIED});

        try (Cursor cursor = cr.query(UriUtils.getExternalVideoUriVersionChecked(), mediaCol, bundle, null)) {

            int count;

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getCount();

                int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                int typeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                int dateIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

                do {
                    String id = cursor.getString(idIndex);
                    String title = cursor.getString(titleIndex);
                    String type = cursor.getString(typeIndex);
                    String size = cursor.getString(sizeIndex);
                    String date = cursor.getString(dateIndex);
                    String duration = cursor.getString(durationIndex);
                    Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Integer.parseInt(id));

                    VideoMedia video = new VideoMedia.Builder(id, uri)
                            .setTitle(title)
                            .setDuration(duration)
                            .setSize(size)
                            .setDataTaken(date)
                            .setMimeType(type)
                            .build();

                    videoMedias.add(video);

                } while (!cursor.isLast() && cursor.moveToNext());
                postMedias(callback, videoMedias, count);
            } else {
                postMedias(callback, videoMedias, 0);
            }
        }
    }

    private void postMedias(
            @NonNull final IMediaTaskCallback<VideoMedia> callback,
            final List<VideoMedia> videoMedias,
            final int count
    ) {
        BoxingExecutor.getInstance().runUI(() -> callback.postMedia(videoMedias, count));
    }

}