package com.spartans.grabon.interfaces;

import android.net.Uri;

/**
 * Author : Sudha Amarnath on 2020-03-05
 */
public interface FileDataImageStatus {

    void onSuccess(Uri uri);

    void onError(String e);

}
