package com.bumptech.glide.load.resource.apng;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.ColorInt;
import com.bumptech.glide.load.resource.apng.ApngBitmapProvider;
import com.bumptech.glide.load.resource.apng.ApngDrawable;
import java.util.ArrayList;
import java.util.List;
import net.ellerton.japng.argb8888.Argb8888Bitmap;
import net.ellerton.japng.argb8888.Argb8888ScanlineProcessor;
import net.ellerton.japng.chunks.PngAnimationControl;
import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;

/**
 * Takes loaded PNG frames and composes them into an android AnimationDrawable.
 */
public class ApngAnimationComposer {
  private Resources resources;
  private Canvas canvas;
  private PngHeader header;
  private Bitmap canvasBitmap;
  private Argb8888ScanlineProcessor scanlineProcessor;
  private PngAnimationControl animationControl;
  private ApngBitmapProvider apngBitmapProvider;
  private PngFrameControl currentFrame;
  private List<Frame> frames;
  private int durationScale = 1;
  private Paint srcModePaint;

  @ColorInt private int[] mainScratch;

  public ApngAnimationComposer(Resources resources, PngHeader header,
      Argb8888ScanlineProcessor scanlineProcessor, PngAnimationControl animationControl,
      ApngBitmapProvider apngBitmapProvider) {
    this.resources = resources;
    this.header = header;
    this.scanlineProcessor = scanlineProcessor;
    this.animationControl = animationControl;
    this.apngBitmapProvider = apngBitmapProvider;

    this.canvasBitmap =
        apngBitmapProvider.obtain(this.header.width, this.header.height, Bitmap.Config.ARGB_8888);
    this.canvas = new Canvas(this.canvasBitmap);
    this.frames = new ArrayList<>(animationControl.numFrames);
    this.srcModePaint = new Paint();
    this.srcModePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
  }

  public int getDurationScale() {
    return durationScale;
  }

  public void setDurationScale(int durationScale) {
    this.durationScale = durationScale;
  }

  public boolean isSingleFrame() {
    return 1 == animationControl.numFrames;
  }

  public ApngDrawable assemble() {
    // TODO: handle special case of one frame animation as a plain ImageView
    boolean isFinite = !animationControl.loopForever();
    ApngDrawable ad = new ApngDrawable(header);
    ad.setOneShot(isFinite);

    // The AnimationDrawable doesn't support a repeat count so add
    // frames as required. At least the frames can re-use drawables.
    int repeatSequenceCount = isFinite ? animationControl.numPlays : 1;

    for (int i = 0; i < repeatSequenceCount; i++) {
      for (Frame frame : frames) {
        ad.addFrame(frame.drawable, frame.control.getDelayMilliseconds() * durationScale);
        ad.addControll(frame.control);
      }
    }

    ad.setNumFrames(animationControl.numFrames);
    ad.setNumPlays(animationControl.numPlays);

    return ad;
  }

  public Argb8888ScanlineProcessor beginFrame(PngFrameControl frameControl) {
    currentFrame = frameControl;
    return scanlineProcessor.cloneWithSharedBitmap(header.adjustFor(currentFrame));
    //return scanlineProcessor.cloneWithNewBitmap(header.adjustFor(currentFrame));
  }

  public void completeFrame(Argb8888Bitmap frameImage) {
    Bitmap frame = apngBitmapProvider.toBitmap(frameImage);
    boolean isFull = currentFrame.height == header.height && currentFrame.width == header.width;
    Paint paint = null;
    BitmapDrawable bitmapDrawable;
    Bitmap previous = null;

    // Capture the current bitmap region IF it needs to be reverted after rendering
    if (2 == currentFrame.disposeOp) {
      previous = apngBitmapProvider.obtain(currentFrame.width, currentFrame.height,
          Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(previous);
      Rect srcR = new Rect(currentFrame.xOffset, currentFrame.yOffset, currentFrame.xOffset + currentFrame.width, currentFrame.yOffset + currentFrame.height);
      RectF dstR = new RectF(0, 0, currentFrame.width, currentFrame.height);
      canvas.drawBitmap(canvasBitmap, srcR, dstR, new Paint());
      canvas.setBitmap(null);
      // or could use from frames?
      //System.out.println(String.format("Captured previous %d x %d", previous.getWidth(), previous.getHeight()));
    }

    if (0 == currentFrame.blendOp) { // SRC_OVER, not blend (for blend, leave paint null)
      //paint = new Paint();
      //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
      paint = srcModePaint;
    }

    // Draw the new frame into place
    canvas.drawBitmap(frame, currentFrame.xOffset, currentFrame.yOffset, paint);

    // Extract a drawable from the canvas. Have to copy the current bitmap.
    bitmapDrawable =
        new BitmapDrawable(resources, canvasBitmap.copy(Bitmap.Config.ARGB_8888, false));
    apngBitmapProvider.release(frame);

    // Store the drawable in the sequence of frames
    frames.add(new Frame(currentFrame, bitmapDrawable));

    // Now "dispose" of the frame in preparation for the next.

    // https://wiki.mozilla.org/APNG_Specification#.60fcTL.60:_The_Frame_Control_Chunk
    //
    // APNG_DISPOSE_OP_NONE: no disposal is done on this frame before rendering the next; the contents of the output buffer are left as is.
    // APNG_DISPOSE_OP_BACKGROUND: the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
    // APNG_DISPOSE_OP_PREVIOUS: the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.
    //
    switch (currentFrame.disposeOp) {
      case 1: // APNG_DISPOSE_OP_BACKGROUND
        //System.out.println(String.format("Frame %d clear background (full=%s, x=%d y=%d w=%d h=%d) previous=%s", currentFrame.sequenceNumber,
        //        isFull, currentFrame.xOffset, currentFrame.yOffset, currentFrame.width, currentFrame.height, previous));
        if (isFull) {
          canvas.drawColor(0, PorterDuff.Mode.CLEAR); // Clear to fully transparent black
        } else {
          Rect rt = new Rect(currentFrame.xOffset, currentFrame.yOffset,
              currentFrame.width + currentFrame.xOffset,
              currentFrame.height + currentFrame.yOffset);
          paint = new Paint();
          paint.setColor(0);
          paint.setStyle(Paint.Style.FILL);
          canvas.drawRect(rt, paint);
        }
        break;

      case 2: // APNG_DISPOSE_OP_PREVIOUS
        //System.out.println(String.format("Frame %d restore previous (full=%s, x=%d y=%d w=%d h=%d) previous=%s", currentFrame.sequenceNumber,
        //        isFull, currentFrame.xOffset, currentFrame.yOffset, currentFrame.width, currentFrame.height, previous));

        // Put the original section back
        if (null != previous) {
          //paint = new Paint();
          //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
          paint = srcModePaint;
          canvas.drawBitmap(previous, currentFrame.xOffset, currentFrame.yOffset, paint);

          //System.out.println("  Restored previous "+previous.getWidth()+" x "+previous.getHeight());

          apngBitmapProvider.release(previous);
        } else {
          System.out.println("  Huh, no previous?");
        }
        break;

      case 0: // APNG_DISPOSE_OP_NONE
      default: // Default should never happen
        // do nothing
        //System.out.println("Frame "+currentFrame.sequenceNumber+" do nothing dispose");
        break;
    }

    currentFrame = null;
  }

  public void clear() {
    apngBitmapProvider.release(canvasBitmap);
  }

  public static class Frame {
    public final PngFrameControl control;
    public final BitmapDrawable drawable;

    public Frame(PngFrameControl control, BitmapDrawable drawable) {
      this.control = control;
      this.drawable = drawable;
    }
  }
}
