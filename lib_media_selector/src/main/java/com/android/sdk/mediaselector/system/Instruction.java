package com.android.sdk.mediaselector.system;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.sdk.mediaselector.common.CropOptions;
import com.android.sdk.mediaselector.common.StorageUtils;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2020-08-07 09:55
 */
public class Instruction implements Parcelable {

    static final int CAMERA = 1;
    static final int ALBUM = 2;
    static final int FILE = 3;

    private BaseSystemMediaSelector mMediaSelector;

    private final int mTakingType;
    private String mMimeType;
    private CropOptions mCropOptions;
    private boolean mNeedCrop;
    private String mCameraPhotoSavePath;
    private boolean mIsMultiple;
    private boolean mCopyToInternal;

    Instruction(BaseSystemMediaSelector mediaSelector, int type) {
        mTakingType = type;
        mMediaSelector = mediaSelector;
    }

    protected Instruction(Parcel in) {
        mTakingType = in.readInt();
        mMimeType = in.readString();
        mCropOptions = in.readParcelable(CropOptions.class.getClassLoader());
        mNeedCrop = in.readByte() != 0;
        mCameraPhotoSavePath = in.readString();
        mIsMultiple = in.readByte() != 0;
        mCopyToInternal = in.readByte() != 0;
    }

    void setMediaSelector(BaseSystemMediaSelector mediaSelector) {
        mMediaSelector = mediaSelector;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTakingType);
        dest.writeString(mMimeType);
        dest.writeParcelable(mCropOptions, flags);
        dest.writeByte((byte) (mNeedCrop ? 1 : 0));
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

    public String getCameraPhotoSavePath() {
        if (TextUtils.isEmpty(mCameraPhotoSavePath)) {
            mCameraPhotoSavePath = StorageUtils.createInternalPicturePath(mMediaSelector.getContext(), StorageUtils.JPEG);
        }
        return mCameraPhotoSavePath;
    }

    public CropOptions getCropOptions() {
        return mCropOptions;
    }

    public boolean needCrop() {
        return mNeedCrop;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public Instruction setNeedCrop() {
        mNeedCrop = true;
        mCropOptions = new CropOptions();
        return this;
    }

    public Instruction setNeedCrop(CropOptions cropOptions) {
        mNeedCrop = true;
        mCropOptions = cropOptions;
        return this;
    }

    public Instruction setMultiple(boolean multiple) {
        mIsMultiple = multiple;
        return this;
    }

    public void setCopyToInternal(boolean copyToInternal) {
        mCopyToInternal = copyToInternal;
    }

    /**
     * @param mimeType there are many MIMETYPEs defined in {@link android.media.MediaFormat}
     */
    public Instruction setMimeType(String mimeType) {
        mMimeType = mimeType;
        return this;
    }

    public boolean isMultiple() {
        return mIsMultiple;
    }

    public boolean isCopyToInternal() {
        return mCopyToInternal;
    }

}