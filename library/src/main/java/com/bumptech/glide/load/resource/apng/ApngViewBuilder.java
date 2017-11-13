package com.bumptech.glide.load.resource.apng;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
 * Able to build android Views from APNG (ARGB8888) content.
 */
public class ApngViewBuilder extends BasicArgb8888Director<ApngDrawable> {
    final Context context;
    //View result = null;
    //ImageView iv = null;
    Drawable drawableResult = null;
    Argb8888Bitmap pngBitmap;
    PngHeader header;
    ApngAnimationComposer animationComposer = null;
    PngScanlineBuffer buffer;

    public ApngViewBuilder(Context context) {
        this.context = context;
    }

    @Override
    public void receiveHeader(PngHeader header, PngScanlineBuffer buffer)
            throws PngException {
        this.header = header;
        this.buffer = buffer;
        this.pngBitmap = new Argb8888Bitmap(header.width, header.height);
        this.scanlineProcessor = Argb8888Processors.from(header, buffer, pngBitmap);
    }

    @Override
    public boolean wantDefaultImage() {
        return false;
    }

    @Override
    public boolean wantAnimationFrames() {
        return true; // isAnimated;
    }

    @Override
    public Argb8888ScanlineProcessor beforeDefaultImage() {
        //        this.pngBitmap = new Argb8888Bitmap(header.width, header.height);
        //        try {
        //            this.scanlineProcessor = Argb8888Processors.from(header, buffer, pngBitmap);
        //        } catch (PngException e) {
        //            // should never happen
        //        }
        return scanlineProcessor;
    }

    @Override
    public void receiveDefaultImage(Argb8888Bitmap defaultImage) {
        //iv = new ImageView(context);
        //iv.setImageBitmap(PngAndroid.toBitmap(defaultImage));
        int offset = 0;
        int stride = defaultImage.width;
        drawableResult = new BitmapDrawable(context.getResources(),
                Bitmap.createBitmap(defaultImage.getPixelArray(), offset, stride, defaultImage.width,
                        defaultImage.height, Bitmap.Config.ARGB_8888));
    }

    @Override
    public void receiveAnimationControl(PngAnimationControl animationControl) {
        this.animationComposer =
                new ApngAnimationComposer(context.getResources(), header, scanlineProcessor,
                        animationControl);
    }

    @Override
    public Argb8888ScanlineProcessor receiveFrameControl(PngFrameControl frameControl) {
        assert (animationComposer != null);
        return animationComposer.beginFrame(frameControl);
    }

    @Override
    public void receiveFrameImage(Argb8888Bitmap frameImage) {
        assert (animationComposer != null);
        animationComposer.completeFrame(frameImage);
    }


    /*
    @Override
    public View getResult() {
        if (isAnimated) {
            if (animationComposer==null) {
                // error
                Log.e(getClass().getName(), "animated result but no composer in place");
            } else {
                //AnimationDrawable ad = animationComposer.assemble();
                iv = new ImageView(context);
                animationComposer.buildInto(iv);
                //iv.setBackgroundDrawable(ad);
                return iv;
            }
        } else {
            if (iv==null) {
                // error
                Log.e(getClass().getName(), "non-animated result but no image view ready");
            } else {
                return iv;
            }
        }
        //return result;
        return null;
    }*/

    @Override
    public ApngDrawable getResult() {
        return animationComposer.assemble();
    }
}

