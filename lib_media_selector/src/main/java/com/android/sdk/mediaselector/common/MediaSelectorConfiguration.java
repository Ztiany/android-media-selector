package com.android.sdk.mediaselector.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;

import com.android.sdk.mediaselector.permission.MediaAccessPermission;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.utils.BoxingFileHelper;
import com.ztiany.mediaselector.R;

/**
 * @author Ztiany
 */
public class MediaSelectorConfiguration {

    private static String sAuthority = "";
    private static boolean sForceUseLegacyApi = false;

    private static MediaAccessPermission sMediaAccessPermission;

    /**
     * @see <a href='https://stackoverflow.com/questions/27611173/how-to-get-accent-color-programmatically'>how-to-get-accent-color-programmatically</>
     */
    public static int getPrimaryColor(Context context) {
        TypedValue outValue = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getTheme().resolveAttribute(R.attr.colorPrimary, outValue, true);
        } else {
            // get color defined for AppCompat
            Resources resources = context.getResources();
            int appCompatAttribute = resources.getIdentifier("colorPrimary", "attr", context.getPackageName());
            context.getTheme().resolveAttribute(appCompatAttribute, outValue, true);
        }
        String color = String.format("#%06X", (0xFFFFFF & outValue.data));
        return Color.parseColor(color);
    }

    /**
     * The default authority is "package-name.fileProvider".
     */
    public static void setAuthority(String authority) {
        sAuthority = authority;
    }

    public static String getAuthority(Context context) {
        if (!TextUtils.isEmpty(sAuthority)) {
            return sAuthority;
        }
        return context.getPackageName().concat(".file.provider");
    }

    public static void forceUseLegacyApi(boolean forceUseLegacyApi) {
        sForceUseLegacyApi = forceUseLegacyApi;
    }

    public static boolean isForceUseLegacyApi() {
        return sForceUseLegacyApi;
    }

    /**
     * using <a href='https://github.com/bumptech/glide'>glide</a> as the media loader in default. you can change it by providing your own {@link IBoxingMediaLoader}.
     */
    public static void setImageLoader(IBoxingMediaLoader boxingMediaLoader) {
        BoxingMediaLoader.getInstance().init(boxingMediaLoader);
    }

    /**
     * the default is "boxing".
     */
    public static void setCameraPhotoFolderName(String folderName) {
        BoxingFileHelper.DEFAULT_SUB_DIR = folderName;
    }

}