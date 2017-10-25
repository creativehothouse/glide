package com.bumptech.glide.load.resource.apng;

import android.graphics.drawable.AnimationDrawable;
import java.util.ArrayList;
import java.util.List;
import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;

/**
 * Created by edwin on 3/30/17.
 */

public class ApngDrawable extends AnimationDrawable {

  private final List<PngFrameControl> pngFrameControlList = new ArrayList<>();
  private final PngHeader header;
  private int numFrames;
  private int numPlays;

  public ApngDrawable(PngHeader header) {
    this.header = header;
  }

  public void setNumFrames(int numFrames) {
    this.numFrames = numFrames;
  }

  public void setNumPlays(int numPlays) {
    this.numPlays = numPlays;
  }

  public void addControll(PngFrameControl control) {
    pngFrameControlList.add(control);
  }

  public List<PngFrameControl> getPngFrameControlList() {
    return pngFrameControlList;
  }

  public PngHeader getHeader() {
    return header;
  }

  public int getNumFrames() {
    return numFrames;
  }

  public int getNumPlays() {
    return numPlays;
  }
}
