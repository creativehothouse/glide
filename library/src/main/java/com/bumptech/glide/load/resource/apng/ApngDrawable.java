package com.bumptech.glide.load.resource.apng;

import android.graphics.drawable.AnimationDrawable;

import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edwin on 3/30/17.
 */

public class ApngDrawable extends AnimationDrawable {

    final List<PngFrameControl> pngFrameControlList = new ArrayList<PngFrameControl>();
    final Header header2 = new Header();
    PngHeader header;
    int numFrames;
    int numPlays;

    public ApngDrawable() {
        //for decoder v2
    }

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

    class Header {
        public int width;
        public int height;
        public byte bitDepth;
        public byte colourType;
        public byte compressionMethod;
        public byte filterMethod;
        public byte interlaceMethod;
    }

}
