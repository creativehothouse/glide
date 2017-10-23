package com.bumptech.glide.load.resource.apng;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import net.ellerton.japng.argb8888.Argb8888Bitmap;

/**
 * Implements {@link GifDecoder.BitmapProvider} by wrapping Glide's
 * {@link BitmapPool}.
 */
public final class ApngBitmapProvider implements GifDecoder.BitmapProvider {
  private final BitmapPool bitmapPool;
  @Nullable private final ArrayPool arrayPool;

  /**
   * Constructs an instance without a shared byte array pool. Byte arrays will be always constructed
   * when requested.
   */
  public ApngBitmapProvider(BitmapPool bitmapPool) {
    this(bitmapPool, null /* arrayPool */);
  }

  /**
   * Constructs an instance with a shared array pool. Arrays will be reused where
   * possible.
   */
  public ApngBitmapProvider(BitmapPool bitmapPool, ArrayPool arrayPool) {
    this.bitmapPool = bitmapPool;
    this.arrayPool = arrayPool;
  }

  @NonNull
  @Override
  public Bitmap obtain(int width, int height, Bitmap.Config config) {
    return bitmapPool.getDirty(width, height, config);
  }

  @Override
  public void release(Bitmap bitmap) {
    bitmapPool.put(bitmap);
  }

  @Override
  public byte[] obtainByteArray(int size) {
    if (arrayPool == null) {
      return new byte[size];
    }
    return arrayPool.get(size, byte[].class);
  }

  @SuppressWarnings("PMD.UseVarargs")
  @Override
  public void release(byte[] bytes) {
    if (arrayPool == null) {
      return;
    }
    arrayPool.put(bytes, byte[].class);
  }

  @Override
  public int[] obtainIntArray(int size) {
    if (arrayPool == null) {
      return new int[size];
    }
    return arrayPool.get(size, int[].class);
  }

  @SuppressWarnings("PMD.UseVarargs")
  @Override
  public void release(int[] array) {
    if (arrayPool == null) {
      return;
    }
    arrayPool.put(array, int[].class);
  }

  public Bitmap toBitmap(Argb8888Bitmap src) {
    int offset = 0;
    int stride = src.width;
    Bitmap bitmap = obtain(src.width,src.height,Bitmap.Config.ARGB_8888);
    bitmap.setPixels(src.getPixelArray(), offset, stride,0,0, src.width, src.height );
    return bitmap;
  }
}
