package com.android.sdk.mediaselector.mediastore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.sdk.mediaselector.processor.crop.CropOptions;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.model.callback.MediaFilter;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * @author Ztiany
 */
public class Instruction implements Parcelable {

    static {
        BoxingMediaLoader.getInstance().init(new BoxingGlideLoader());
    }

    private static final int MAX_COUNT = 30;

    static final int PICTURE = 1;
    static final int VIDEO = 2;

    private boolean mNeedCamera;
    private final int mTakingType;
    private boolean mCopyToInternal;
    private boolean mNeedGif;
    private boolean mNeedAccessMediaLocation = false;
    private CropOptions mCropOptions;
    private int mCount = 1;
    private MediaFilter mMediaFilter;

    private BaseMediaSelector mBaseMediaSelector;

    Instruction(BaseMediaSelector mediaSelector, int takingType) {
        mBaseMediaSelector = mediaSelector;
        mTakingType = takingType;
    }

    public boolean start() {
        Timber.d("start()");
        return mBaseMediaSelector.start(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // config
    ///////////////////////////////////////////////////////////////////////////
    public Instruction count(int count) {
        if (count > 1) {
            notSupportForVideo("taking more than 1 items");
            notSupportForCrop("taking more than 1 items");
            notSupportForCamera("taking more than 1 items", false);
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }
        mCount = count;
        return this;
    }

    public Instruction enableCamera() {
        notSupportForVideo("camera");
        notSupportForMultiSelecting("camera", false);

        mNeedCamera = true;
        return this;
    }

    public Instruction copyToInternal() {
        mCopyToInternal = true;
        return this;
    }

    public Instruction needGif() {
        notSupportForVideo("selecting gif");

        mNeedGif = true;
        return this;
    }

    public Instruction crop() {
        return crop(new CropOptions());
    }

    public Instruction crop(CropOptions cropOptions) {
        notSupportForMultiSelecting("cropping", true);
        notSupportForVideo("cropping");

        mCropOptions = cropOptions;
        return this;
    }

    public Instruction mediaFilter(MediaFilter mediaFilter) {
        mMediaFilter = mediaFilter;
        return this;
    }

    /**
     * 貌似使用内置相机拍照得到照片没有位置信息。
     */
    public Instruction needMediaLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(mBaseMediaSelector.getContext(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mNeedAccessMediaLocation = true;
            } else {
                Timber.w("You don't have the permission ACCESS_MEDIA_LOCATION to access media location.");
            }
        }
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // checker
    ///////////////////////////////////////////////////////////////////////////
    private void notSupportForVideo(String action) {
        if (mTakingType == VIDEO) {
            throw new UnsupportedOperationException(action + " is not supported when taking Videos");
        }
    }

    private void notSupportForCrop(String action) {
        if (mCropOptions != null) {
            throw new UnsupportedOperationException(action + " is not supported when cropping is enabled");
        }
    }

    private void notSupportForCamera(String action, boolean crash) {
        if (mNeedCamera) {
            if (crash) {
                throw new UnsupportedOperationException(action + " is not supported when camera is enabled");
            } else {
                Timber.e("%s is not supported properly when camera is enabled", action);
            }
        }
    }

    private void notSupportForMultiSelecting(String action, boolean crash) {
        if (mCount > 1) {
            if (crash) {
                throw new UnsupportedOperationException(action + " is not supported when taking items more than one");
            } else {
                Timber.e("%s is not supported properly when taking items more than one", action);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // getter
    ///////////////////////////////////////////////////////////////////////////

    @Nullable
    CropOptions getCropOptions() {
        return mCropOptions;
    }

    boolean isNeedGif() {
        return mNeedGif;
    }

    boolean isNeedCrop() {
        return mCropOptions != null;
    }

    boolean isNeedCamera() {
        return mNeedCamera;
    }

    int getTakingType() {
        return mTakingType;
    }

    boolean isCopyToInternal() {
        return mCopyToInternal;
    }

    boolean moreThanOne() {
        return mCount > 1;
    }

    int getCount() {
        return mCount;
    }

    void setMediaSelector(@NotNull BaseMediaSelector baseMediaSelector) {
        mBaseMediaSelector = baseMediaSelector;
    }

    boolean isNeedAccessMediaLocation() {
        return mNeedAccessMediaLocation;
    }

    MediaFilter getMediaFilter() {
        return mMediaFilter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // for Parcelable
    ///////////////////////////////////////////////////////////////////////////

    protected Instruction(Parcel in) {
        mNeedCamera = in.readByte() != 0;
        mTakingType = in.readInt();
        mCopyToInternal = in.readByte() != 0;
        mNeedGif = in.readByte() != 0;
        mNeedAccessMediaLocation = in.readByte() != 0;
        mCropOptions = in.readParcelable(CropOptions.class.getClassLoader());
        mCount = in.readInt();
        mMediaFilter = in.readParcelable(MediaFilter.class.getClassLoader());
    }

    public static final Creator<Instruction> CREATOR = new Creator<Instruction>() {
        @Override
        public Instruction createFromParcel(Parcel in) {
            return new Instruction(in);
        }

        @Override
        public Instruction[] newArray(int size) {
            return new Instruction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mNeedCamera ? 1 : 0));
        dest.writeInt(mTakingType);
        dest.writeByte((byte) (mCopyToInternal ? 1 : 0));
        dest.writeByte((byte) (mNeedGif ? 1 : 0));
        dest.writeByte((byte) (mNeedAccessMediaLocation ? 1 : 0));
        dest.writeParcelable(mCropOptions, flags);
        dest.writeInt(mCount);
        dest.writeParcelable(mMediaFilter, flags);
    }

}