package com.bumptech.glide.load.resource.apng;

import android.content.Context;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import net.ellerton.japng.argb8888.Argb8888Processor;
import net.ellerton.japng.error.PngException;
import net.ellerton.japng.reader.DefaultPngChunkReader;
import net.ellerton.japng.reader.PngReadHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link ResourceDecoder} that decodes
 * {@link GifDrawable} from {@link InputStream} data.
 */
public class ApngResourceDecoder implements ResourceDecoder<InputStream, ApngDrawable> {

    final ApngViewBuilder apngViewBuilder;

    public ApngResourceDecoder(Context context) {
        this.apngViewBuilder = new ApngViewBuilder(context);
    }

    @Override
    public Resource<ApngDrawable> decode(InputStream source, int width, int height)
            throws IOException {
        Argb8888Processor<ApngDrawable> processor = new Argb8888Processor<ApngDrawable>(apngViewBuilder);
        try {
            return new ApngDrawableResource(
                    PngReadHelper.read(source, new DefaultPngChunkReader<ApngDrawable>(processor)));
        } catch (PngException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getId() {
        return "";
    }
}
