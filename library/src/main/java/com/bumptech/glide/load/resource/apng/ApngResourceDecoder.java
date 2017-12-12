package com.bumptech.glide.load.resource.apng;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import java.io.IOException;
import java.io.InputStream;
import net.ellerton.japng.android.api.PngViewBuilder;
import net.ellerton.japng.argb8888.Argb8888Processor;
import net.ellerton.japng.error.PngException;
import net.ellerton.japng.reader.DefaultPngChunkReader;
import net.ellerton.japng.reader.PngReadHelper;

/**
 * An {@link ResourceDecoder} that decodes
 * {@link GifDrawable} from {@link InputStream} data.
 */
public class ApngResourceDecoder implements ResourceDecoder<InputStream, ApngDrawable> {

  final Resources resources;
  final ApngBitmapProvider apngBitmapProvider;

  public ApngResourceDecoder(Context context, BitmapPool bitmapPool, ArrayPool arrayPool) {
    this.resources = context.getResources();
    this.apngBitmapProvider = new ApngBitmapProvider(bitmapPool, arrayPool);
  }

  @Override public boolean handles(InputStream source, Options options) throws IOException {
    return true;
  }

  @Nullable @Override
  public Resource<ApngDrawable> decode(InputStream source, int width, int height, Options options)
      throws IOException {
    PngViewBuilder pngViewBuilder = new PngViewBuilder(resources, apngBitmapProvider);
    Argb8888Processor<ApngDrawable> processor = new Argb8888Processor<>(pngViewBuilder);
    try {
      return new ApngDrawableResource(
          PngReadHelper.read(source, new DefaultPngChunkReader<>(processor)));
    } catch (PngException e) {
      e.printStackTrace();
    } finally {
      pngViewBuilder.clear();
    }
    return null;
  }
}
