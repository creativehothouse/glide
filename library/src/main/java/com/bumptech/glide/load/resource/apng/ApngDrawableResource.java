package com.bumptech.glide.load.resource.apng;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.engine.Initializable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.gif.GifDrawable;

/**
 * A resource wrapping an {@link GifDrawable}.
 */
public class ApngDrawableResource implements Resource<ApngDrawable>, Initializable {

  private final ApngDrawable apngDrawable;

  public ApngDrawableResource(ApngDrawable animationDrawable) {
    this.apngDrawable = animationDrawable;
  }

  @Override public Class<ApngDrawable> getResourceClass() {
    return ApngDrawable.class;
  }

  @Override public ApngDrawable get() {
    return apngDrawable;
  }

  @Override public int getSize() {
    return 0;
  }

  @Override public void recycle() {
    apngDrawable.stop();
  }

  @Override public void initialize() {
    Drawable firstFrame = apngDrawable.getFrame(0);
    if (firstFrame instanceof BitmapDrawable) {
      ((BitmapDrawable) firstFrame).getBitmap().prepareToDraw();
    }
  }
}
