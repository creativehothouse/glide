package com.bumptech.glide.load.resource.apng;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * An {@link ResourceEncoder} that can write
 * {@link GifDrawable} to cache.
 */
public class ApngResourceEncoder implements ResourceEncoder<ApngDrawable> {
    private static final byte[] PNG_SIGNATURE = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
    private static final int IHDR_VALUE = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R';
    private static final int PLTE_VALUE = 'P' << 24 | 'L' << 16 | 'T' << 8 | 'E';
    private static final int IDAT_VALUE = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T';
    private static final int IEND_VALUE = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D';
    private static final int gAMA_VALUE = 'g' << 24 | 'A' << 16 | 'M' << 8 | 'A';
    private static final int bKGD_VALUE = 'b' << 24 | 'K' << 16 | 'G' << 8 | 'D';
    private static final int tRNS_VALUE = 't' << 24 | 'R' << 16 | 'N' << 8 | 'S';
    private static final int acTL_VALUE = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int fcTL_VALUE = 'f' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int fdAT_VALUE = 'f' << 24 | 'd' << 16 | 'A' << 8 | 'T';

    @Override
    public boolean encode(Resource<ApngDrawable> resource, OutputStream os) {
        ApngDrawable drawable = resource.get();

        DataOutputStream dos = new DataOutputStream(os);

        try {
            dos.write(PNG_SIGNATURE.clone());
            dos.write(makeIHDRChunk(drawable.header).array());
            dos.write(makeACTLChunk(drawable.numFrames, drawable.numPlays).array());

            for (int i = 0; i < drawable.numFrames; ++i) {
                Drawable frame = drawable.getFrame(i);
                dos.write(makeFCTL(drawable.pngFrameControlList.get(i), i).array());
                dos.write(makeFDAT((BitmapDrawable) frame, i).array());
            }

            dos.write(makeIENDChunk().array());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getId() {
        return "";
    }

    private ByteBuffer makeIHDRChunk(PngHeader header) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(28);
        bb.putInt(13);
        bb.putInt(IHDR_VALUE);
        bb.putInt(header.width);
        bb.putInt(header.height);
        bb.put(header.bitDepth);
        bb.putInt(header.colourType.code);
        bb.put(header.compressionMethod);
        bb.put(header.filterMethod);
        bb.put(header.interlaceMethod);
        addCRC(bb);
        bb.flip();
        return bb;
    }

    private ByteBuffer makeACTLChunk(int frameCount, int loopCount) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.putInt(8);
        bb.putInt(acTL_VALUE);
        bb.putInt(frameCount);
        bb.putInt(loopCount);
        addCRC(bb);
        bb.flip();
        return bb;
    }

    private ByteBuffer makeFCTL(PngFrameControl pngFrameControl, int sequenceNumber)
            throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(38);
        bb.putInt(26);
        bb.putInt(fcTL_VALUE);
        bb.putInt(sequenceNumber + 1);
        bb.putInt(pngFrameControl.width);
        bb.putInt(pngFrameControl.height);
        bb.putInt(pngFrameControl.xOffset);
        bb.putInt(pngFrameControl.yOffset);
        bb.putShort(pngFrameControl.delayNumerator);
        bb.putShort(pngFrameControl.delayNumerator);
        bb.put(pngFrameControl.disposeOp);
        bb.put(pngFrameControl.blendOp);
        addCRC(bb);
        bb.flip();
        return bb;
    }

    private ByteBuffer makeFDAT(BitmapDrawable drawable, int sequenceNumber) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        drawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        stream.close();

        int lenght = bitmapdata.length + 16;
        ByteBuffer bb = ByteBuffer.allocate(lenght);
        bb.putInt(lenght);
        bb.putInt(fdAT_VALUE);
        bb.putInt(sequenceNumber);
        bb.put(bitmapdata);
        addCRC(bb);
        bb.flip();
        return bb;
    }

    private ByteBuffer makeIENDChunk() {
        final ByteBuffer bb = ByteBuffer.allocate(12);
        bb.putInt(0);
        bb.putInt(IEND_VALUE);
        addCRC(bb);
        bb.flip();
        return bb;
    }

    private void addCRC(ByteBuffer chunkBuffer) {
        if (chunkBuffer.remaining() != 4) {
            throw new IllegalArgumentException();
        }

        int size = chunkBuffer.position() - 4;

        if (size <= 0) throw new IllegalArgumentException();

        chunkBuffer.position(4);
        byte[] bytes = new byte[size];
        chunkBuffer.get(bytes);
        chunkBuffer.putInt(crc(bytes));
    }

    private int crc(byte[] buf) {
        return crc(buf, 0, buf.length);
    }

    private int crc(byte[] buf, int off, int len) {
        CRC32 crc = new CRC32();
        crc.update(buf, off, len);
        return (int) crc.getValue();
    }
}
