package com.bumptech.glide.load.resource.apng;

import android.content.Context;

import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.provider.DataLoadProvider;

import java.io.File;
import java.io.InputStream;

/**
 * An {@link DataLoadProvider} that loads an {@link InputStream} into
 * {@link GifDrawable} that can be used to display an animated GIF.
 */
public class ApngDrawableLoadProvider implements DataLoadProvider<InputStream, ApngDrawable> {
    private final ApngResourceDecoder decoder;
    private final ApngResourceEncoder encoder;
    private final StreamEncoder sourceEncoder;
    private final FileToStreamDecoder<ApngDrawable> cacheDecoder;

    public ApngDrawableLoadProvider(Context context, BitmapPool bitmapPool) {
        decoder = new ApngResourceDecoder(context);
        cacheDecoder = new FileToStreamDecoder<ApngDrawable>(decoder);
        encoder = new ApngResourceEncoder();
        sourceEncoder = new StreamEncoder();
    }

    @Override
    public ResourceDecoder<File, ApngDrawable> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<InputStream, ApngDrawable> getSourceDecoder() {
        return decoder;
    }

    @Override
    public Encoder<InputStream> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<ApngDrawable> getEncoder() {
        return encoder;
    }
}
