package com.example.tailorz.customerAdapters;

import android.graphics.Bitmap;

public interface BitmapCallback {
    void onSuccess(Bitmap bitmap);
    void onError(String errorMessage);
}
