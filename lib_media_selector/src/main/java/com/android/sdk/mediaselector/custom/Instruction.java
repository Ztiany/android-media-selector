package com.android.sdk.mediaselector.custom;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.android.sdk.mediaselector.common.CropOptions;
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
    private int mTakingType = PICTURE;
    private boolean mCopyToInternal;
    private CropOptions mCropOptions;
    private boolean mIsMulti;
    private int mCount = 9;
    private MediaFilter mMediaFilter;

    private BaseMediaSelector mBaseMediaSelector;

    Instruction(BaseMediaSelector mediaSelector) {
        mBaseMediaSelector = mediaSelector;
    }

    protected Instruction(Parcel in) {
        mNeedCamera = in.readByte() != 0;
        mTakingType = in.readInt();
        mCopyToInternal = in.readByte() != 0;
        mCropOptions = in.readParcelable(CropOptions.class.getClassLoader());
        mIsMulti = in.readByte() != 0;
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

    public Instruction needMulti(int count) {
        mIsMulti = true;
        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }
        mCount = count;
        return this;
    }

    public Instruction takePicture() {
        mTakingType = PICTURE;
        return this;
    }

    public Instruction takeVideo() {
        mTakingType = VIDEO;
        return this;
    }

    public Instruction needCamera() {
        mNeedCamera = true;
        return this;
    }

    public Instruction copyToInternal() {
        mCopyToInternal = true;
        return this;
    }

    public Instruction crop(CropOptions cropOptions) {
        mCropOptions = cropOptions;
        return this;
    }

    public Instruction crop() {
        mCropOptions = new CropOptions();
        return this;
    }

    public Instruction setMediaFilter(MediaFilter mediaFilter) {
        mMediaFilter = mediaFilter;
        return this;
    }

    public MediaFilter getMediaFilter() {
        return mMediaFilter;
    }

    public boolean start() {
        Timber.d("start()");
        return mBaseMediaSelector.start(this);
    }

    @Nullable
    public CropOptions getCropOptions() {
        return mCropOptions;
    }

    public boolean isNeedCrop() {
        return mCropOptions != null;
    }

    public boolean isNeedCamera() {
        return mNeedCamera;
    }

    public int getTakingType() {
        return mTakingType;
    }

    public boolean isCopyToInternal() {
        return mCopyToInternal;
    }

    public boolean isMulti() {
        return mIsMulti;
    }

    public int getCount() {
        return mCount;
    }

    void setMediaSelector(@NotNull BaseMediaSelector baseMediaSelector) {
        mBaseMediaSelector = baseMediaSelector;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mNeedCamera ? 1 : 0));
        dest.writeInt(mTakingType);
        dest.writeByte((byte) (mCopyToInternal ? 1 : 0));
        dest.writeParcelable(mCropOptions, flags);
        dest.writeByte((byte) (mIsMulti ? 1 : 0));
        dest.writeInt(mCount);
        dest.writeParcelable(mMediaFilter, flags);
    }

}