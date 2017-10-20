package net.ellerton.japng.android.api;

import android.graphics.Bitmap;
import net.ellerton.japng.argb8888.Argb8888Bitmap;

/**
 * Convenience functions to load PNGs for Android.
 */
public class PngAndroid {
  public static Bitmap toBitmap(Argb8888Bitmap src) {
    int offset = 0;
    int stride = src.width;
    return Bitmap.createBitmap(src.getPixelArray(), offset, stride, src.width, src.height,
        Bitmap.Config.ARGB_8888);
  }
}
