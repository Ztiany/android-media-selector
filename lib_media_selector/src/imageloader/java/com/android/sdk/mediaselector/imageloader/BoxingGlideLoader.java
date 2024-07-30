package com.android.sdk.mediaselector.imageloader;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import timber.log.Timber;

/**
 * use <a href='https://github.com/bumptech/glide'>glide</a> as the media loader.
 *
 * @author ChenSL
 */
public class BoxingGlideLoader implements IBoxingMediaLoader {

    @Override
    public void displayThumbnail(@NonNull ImageView img, @NonNull Uri uri, int width, int height) {
        try {
            RequestOptions requestOptions = new RequestOptions();
            Glide.with(img.getContext()).load(uri).apply(requestOptions).into(img);
        } catch (IllegalArgumentException e) {
            Timber.e(e, "displayThumbnail");
        }
    }

    @Override
    public void displayRaw(@NonNull final ImageView imageView, @NonNull Uri uri, int width, int height, final IBoxingCallback iBoxingCallback) {
        try {
            Glide.with(imageView.getContext())
                    .asBitmap()
                    .load(uri)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Bitmap> target, boolean isFirstResource) {
                            if (iBoxingCallback != null) {
                                iBoxingCallback.onFail(e);
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            if (iBoxingCallback != null) {
                                imageView.setImageBitmap(resource);
                                iBoxingCallback.onSuccess();
                                return true;
                            }
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (IllegalArgumentException e) {
            Timber.e(e, "displayRaw");
        }
    }

}