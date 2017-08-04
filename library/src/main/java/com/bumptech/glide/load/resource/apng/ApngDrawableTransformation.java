package com.bumptech.glide.load.resource.apng;

import android.graphics.Bitmap;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.gif.GifDrawable;

/**
 * An {@link Transformation} that wraps a transformation for a {@link Bitmap}
 * and can apply it to every frame of any {@link GifDrawable}.
 */
public class ApngDrawableTransformation implements Transformation<ApngDrawable> {
    private final Transformation<Bitmap> wrapped;
    private final BitmapPool bitmapPool;

    public ApngDrawableTransformation(Transformation<Bitmap> wrapped, BitmapPool bitmapPool) {
        this.wrapped = wrapped;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<ApngDrawable> transform(Resource<ApngDrawable> resource, int outWidth,
            int outHeight) {
        return resource;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }
}
