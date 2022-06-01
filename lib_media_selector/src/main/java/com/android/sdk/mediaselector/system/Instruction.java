package com.android.sdk.mediaselector.system;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.sdk.mediaselector.common.CropOptions;
import com.android.sdk.mediaselector.common.StorageUtils;

import timber.log.Timber;

/**
 * @author Ztiany
 */
public class Instruction implements Parcelable {

    static final int CAMERA = 1;
    static final int ALBUM = 2;
    static final int FILE = 3;

    private BaseSystemMediaSelector mMediaSelector;

    private final int mTakingType;
    private String mMimeType;
    private CropOptions mCropOptions;
    private String mCameraPhotoSavePath;
    private boolean mIsMultiple;
    private boolean mCopyToInternal;

    Instruction(BaseSystemMediaSelector mediaSelector, int type) {
        mTakingType = type;
        mMediaSelector = mediaSelector;
    }

    void setMediaSelector(BaseSystemMediaSelector mediaSelector) {
        mMediaSelector = mediaSelector;
    }

    public boolean start() {
        switch (mTakingType) {
            case CAMERA:
                return mMediaSelector.takePhotoFromCamera(this);
            case ALBUM:
                return mMediaSelector.takePhotoFormSystem(this);
            case FILE:
                return mMediaSelector.takeFile(this);
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // checker
    ///////////////////////////////////////////////////////////////////////////
    private void notSupportForFile(String action) {
        if (mTakingType == FILE) {
            throw new UnsupportedOperationException(action + " is not supported when taking files");
        }
    }

    private void notSupportForCrop(String action) {
        if (mCropOptions != null) {
            throw new UnsupportedOperationException(action + " is not supported when cropping is enabled");
        }
    }

    private void notSupportForCamera(String action) {
        if (mTakingType == CAMERA) {
            throw new UnsupportedOperationException(action + " is not supported when taking photos by camera");
        }
    }

    private void notSupportForMultiSelecting(String action, boolean crash) {
        if (mIsMultiple) {
            if (crash) {
                throw new UnsupportedOperationException(action + " is not supported when taking items more than one");
            } else {
                Timber.e("%s is not supported properly when taking items more than one", action);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // get
    ///////////////////////////////////////////////////////////////////////////
    String getCameraPhotoSavePath() {
        if (TextUtils.isEmpty(mCameraPhotoSavePath)) {
            mCameraPhotoSavePath = StorageUtils.createInternalPicturePath(mMediaSelector.getContext(), StorageUtils.JPEG);
        }
        return mCameraPhotoSavePath;
    }

    CropOptions getCropOptions() {
        return mCropOptions;
    }

    boolean needCrop() {
        return mCropOptions != null;
    }

    String getMimeType() {
        return mMimeType;
    }

    boolean isMultiple() {
        return mIsMultiple;
    }

    boolean isCopyToInternal() {
        return mCopyToInternal;
    }

    ///////////////////////////////////////////////////////////////////////////
    // config
    ///////////////////////////////////////////////////////////////////////////
    public Instruction crop() {
        return crop(new CropOptions());
    }

    public Instruction crop(CropOptions cropOptions) {
        notSupportForFile("cropping");
        notSupportForMultiSelecting("cropping", true);

        mCropOptions = cropOptions;
        return this;
    }

    public Instruction multiple(boolean multiple) {
        notSupportForCamera("taking more than 1  item");
        notSupportForCrop("taking more than 1  item");

        mIsMultiple = multiple;
        return this;
    }

    public void copyToInternal(boolean copyToInternal) {
        mCopyToInternal = copyToInternal;
    }

    /**
     * @param mimeType there are many MIME-TYPEs defined in {@link android.media.MediaFormat}
     */
    public Instruction mimeType(String mimeType) {
        mMimeType = mimeType;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // for Parcelable
    ///////////////////////////////////////////////////////////////////////////
    protected Instruction(Parcel in) {
        mTakingType = in.readInt();
        mMimeType = in.readString();
        mCropOptions = in.readParcelable(CropOptions.class.getClassLoader());
        mCameraPhotoSavePath = in.readString();
        mIsMultiple = in.readByte() != 0;
        mCopyToInternal = in.readByte() != 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTakingType);
        dest.writeString(mMimeType);
        dest.writeParcelable(mCropOptions, flags);
        dest.writeString(mCameraPhotoSavePath);
        dest.writeByte((byte) (mIsMultiple ? 1 : 0));
        dest.writeByte((byte) (mCopyToInternal ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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

}