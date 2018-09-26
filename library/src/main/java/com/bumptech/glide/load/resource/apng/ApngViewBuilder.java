package com.bumptech.glide.load.resource.apng;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import net.ellerton.japng.PngScanlineBuffer;
import net.ellerton.japng.argb8888.Argb8888Bitmap;
import net.ellerton.japng.argb8888.Argb8888Processors;
import net.ellerton.japng.argb8888.Argb8888ScanlineProcessor;
import net.ellerton.japng.argb8888.BasicArgb8888Director;
import net.ellerton.japng.chunks.PngAnimationControl;
import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;
import net.ellerton.japng.error.PngException;

/**
 * Able to build android Views from PNG (ARGB8888) content.
 */
public class ApngViewBuilder extends BasicArgb8888Director<ApngDrawable> {

  private final Resources resources;
  private PngHeader header;
  private ApngAnimationComposer animationComposer = null;
  private ApngBitmapProvider apngBitmapProvider;

  @ColorInt private int[] mainScratch;
  private Bitmap defaultBitmap;

  public ApngViewBuilder(Resources resources, ApngBitmapProvider apngBitmapProvider) {
    this.resources = resources;
    this.apngBitmapProvider = apngBitmapProvider;
  }

  @Override public void receiveHeader(PngHeader header, PngScanlineBuffer buffer)
      throws PngException {
    this.header = header;
    mainScratch = apngBitmapProvider.obtainIntArray(header.width * header.height);
    Argb8888Bitmap pngBitmap = new Argb8888Bitmap(mainScratch, header.width, header.height);
    this.scanlineProcessor = Argb8888Processors.from(header, buffer, pngBitmap);
  }

  @Override public boolean wantDefaultImage() {
    return false;
  }

  @Override public boolean wantAnimationFrames() {
    return true;
  }

  @Override public Argb8888ScanlineProcessor beforeDefaultImage() {
    return scanlineProcessor;
  }

  @Override public void receiveDefaultImage(Argb8888Bitmap defaultImage) {
    int offset = 0;
    int stride = defaultImage.width;
    defaultBitmap =
        apngBitmapProvider.obtain(defaultImage.width, defaultImage.height, Bitmap.Config.ARGB_8888);
    defaultBitmap.setPixels(defaultImage.getPixelArray(), offset, stride, 0, 0, defaultImage.width,
        defaultImage.height);
  }

  @Override public void receiveAnimationControl(PngAnimationControl animationControl) {
    this.animationComposer = new ApngAnimationComposer(resources, header, scanlineProcessor,
            animationControl, apngBitmapProvider);
  }

  @Override public Argb8888ScanlineProcessor receiveFrameControl(PngFrameControl frameControl) {
    assert (animationComposer != null);
    return animationComposer.beginFrame(frameControl);
  }

  @Override public void receiveFrameImage(Argb8888Bitmap frameImage) {
    assert (animationComposer != null);
    animationComposer.completeFrame(frameImage);
  }

  @Override public ApngDrawable getResult() {
    return animationComposer.assemble();
  }

  public void clear() {
    apngBitmapProvider.release(mainScratch);
    if (defaultBitmap != null) {
      apngBitmapProvider.release(defaultBitmap);
    }
    animationComposer.clear();
  }
}
