package com.bumptech.glide.load.resource.apng;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.gif.GifDrawable;

/**
 * A resource wrapping an {@link GifDrawable}.
 */
public class ApngDrawableResource implements Resource<ApngDrawable> {

    private final ApngDrawable apngDrawable;

    public ApngDrawableResource(ApngDrawable animationDrawable) {
        this.apngDrawable = animationDrawable;
    }

    @Override
    public ApngDrawable get() {
        return apngDrawable;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void recycle() {
        apngDrawable.stop();
    }
}
