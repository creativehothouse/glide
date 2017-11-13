package com.bumptech.glide.load.resource.apng;

import android.util.SparseArray;

/**
 * Created by edwin on 3/31/17.
 */

public class ApngMetaData {

    ImageHeader imageHeader;
    ImageDefault imageDefault;
    AnimationControl animationControl;
    SparseArray<FrameControl> frameControlSparseArray = new SparseArray<FrameControl>();
    SparseArray<FrameData> frameDataSparseArray = new SparseArray<FrameData>();

    static class ImageHeader {
        public int width;
        public int height;
        public byte bitDepth;
        public byte colourType;
        public byte compressionMethod;
        public byte filterMethod;
        public byte interlaceMethod;
    }

    static class ImageDefault {
        public byte[] image;
    }

    static class AnimationControl {
        public int numFrames;
        public int numPlays;
    }

    static class FrameControl {
        public int sequenceNumber;
        public int width;
        public int height;
        public int xOffset;
        public int yOffset;
        public short delayNumerator;
        public short delayDenominator;
        public byte disposeOp;
        public byte blendOp;
    }

    static class FrameData {
        public int sequenceNumber;
        public byte[] image;
    }
}
