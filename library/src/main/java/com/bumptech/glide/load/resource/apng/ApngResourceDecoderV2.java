package com.bumptech.glide.load.resource.apng;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import net.ellerton.japng.PngConstants;
import net.ellerton.japng.android.api.PngViewBuilder;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * An {@link ResourceDecoder} that decodes
 * {@link GifDrawable} from {@link InputStream} data.
 */
public class ApngResourceDecoderV2 implements ResourceDecoder<InputStream, ApngDrawable> {

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

    final PngViewBuilder apngViewBuilder;
    final Resources resources;

    public ApngResourceDecoderV2(Context context) {
        this.apngViewBuilder = new PngViewBuilder(context);
        this.resources = context.getResources();
    }

    @Override
    public Resource<ApngDrawable> decode(InputStream source, int width, int height)
            throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(source));
        byte[] pngSign = new byte[8];
        if (dataInputStream.read(pngSign, 0, 8) < 0 || !Arrays.equals(PNG_SIGNATURE, pngSign)) {
            throw new RuntimeException("PNG Signature not valid");
        }
        ApngMetaData apngMetaData = new ApngMetaData();
        int dataLenght;
        int dataCode;
        do {
            dataLenght = dataInputStream.readInt();
            dataCode = dataInputStream.readInt();
        } while (!readChunk(apngMetaData, dataInputStream, dataCode, dataLenght));

        return new ApngDrawableResource(transformToApngDrawable(apngMetaData));
    }

    private boolean readChunk(ApngMetaData apngMetaData, DataInputStream dataInputStream,
            int dataCode, int dataLenght) throws IOException {
        String code = new String(ByteBuffer.allocate(4).putInt(dataCode).array());
        Log.d("edwin", "APNG CODE | " + code);
        switch (dataCode) {
            case IHDR_VALUE:
                ApngMetaData.ImageHeader imageHeader = new ApngMetaData.ImageHeader();
                imageHeader.width = dataInputStream.readInt();
                imageHeader.height = dataInputStream.readInt();
                imageHeader.bitDepth = dataInputStream.readByte();
                imageHeader.colourType = dataInputStream.readByte();
                imageHeader.compressionMethod = dataInputStream.readByte();
                imageHeader.filterMethod = dataInputStream.readByte();
                imageHeader.interlaceMethod = dataInputStream.readByte();
                apngMetaData.imageHeader = imageHeader;
                break;
            case acTL_VALUE:
                ApngMetaData.AnimationControl animationControl = new ApngMetaData.AnimationControl();
                animationControl.numFrames = dataInputStream.readInt();
                animationControl.numPlays = dataInputStream.readInt();
                apngMetaData.animationControl = animationControl;
                break;
            case fcTL_VALUE:
                ApngMetaData.FrameControl frameControl = new ApngMetaData.FrameControl();
                frameControl.sequenceNumber = dataInputStream.readInt();
                frameControl.width = dataInputStream.readInt();
                frameControl.height = dataInputStream.readInt();
                frameControl.xOffset = dataInputStream.readInt();
                frameControl.yOffset = dataInputStream.readInt();
                frameControl.delayNumerator = dataInputStream.readShort();
                frameControl.delayDenominator = dataInputStream.readShort();
                if (frameControl.delayDenominator == 0) {
                    frameControl.delayDenominator = 100;
                }
                frameControl.disposeOp = dataInputStream.readByte();
                frameControl.blendOp = dataInputStream.readByte();
                apngMetaData.frameControlSparseArray.put(frameControl.sequenceNumber, frameControl);
                break;
            case IDAT_VALUE: {
                ApngMetaData.ImageDefault imageDefault = new ApngMetaData.ImageDefault();
                byte[] image = new byte[dataLenght];
                if (dataInputStream.read(image, 0, dataLenght) > 0) {
                    imageDefault.image = image;
                    apngMetaData.imageDefault = imageDefault;
                }
            }
            break;
            case fdAT_VALUE: {
                ApngMetaData.FrameData frameData = new ApngMetaData.FrameData();
                frameData.sequenceNumber = dataInputStream.readInt();
                int imageLength = dataLenght - 4;
                byte[] image = new byte[imageLength];
                if (dataInputStream.read(image, 0, imageLength) > 0) {
                    frameData.image = image;
                    apngMetaData.frameDataSparseArray.put(frameData.sequenceNumber, frameData);
                }
            }
            break;
            default:
                dataInputStream.skip(dataLenght);
                break;
        }
        int chunkChecksum = dataInputStream.readInt();
        //TODO do check sum

        return dataCode == PngConstants.IEND_VALUE;
    }

    private ApngDrawable transformToApngDrawable(ApngMetaData apngMetaData) {
        ApngDrawable apngDrawable = new ApngDrawable();
        Bitmap canvasBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(canvasBitmap);
        for (int i = 0; i < apngMetaData.frameDataSparseArray.size(); ++i) {
            int keyFrameData = apngMetaData.frameDataSparseArray.keyAt(i);
            int keyFrameControl = apngMetaData.frameControlSparseArray.keyAt(i + 1);
            ApngMetaData.FrameData frameData = apngMetaData.frameDataSparseArray.get(keyFrameData);
            ApngMetaData.FrameControl frameControl =
                    apngMetaData.frameControlSparseArray.get(keyFrameControl);
            ApngMetaData.ImageHeader imageHeader = apngMetaData.imageHeader;
            Bitmap frameBitmap = createFrameBitmap(imageHeader, frameData, frameControl);

            canvas.drawBitmap(frameBitmap, frameControl.xOffset, frameControl.yOffset, null);
            BitmapDrawable bitmapDrawable =
                    new BitmapDrawable(resources, canvasBitmap.copy(Bitmap.Config.ARGB_8888, false));

            int delay = frameControl.delayDenominator == 1000 ? frameControl.delayNumerator
                    : (int) ((float) frameControl.delayNumerator * (1000 / frameControl.delayDenominator));
            apngDrawable.addFrame(bitmapDrawable, delay);
        }
        return apngDrawable;
    }

    private Bitmap createFrameBitmap(ApngMetaData.ImageHeader imageHeader,
            ApngMetaData.FrameData frameData, ApngMetaData.FrameControl frameControl) {
        int[] pixel = new int[imageHeader.width * imageHeader.height];

        //TODO read framedata and parse it to pixel array

        int offset = 0;
        int stride = imageHeader.width;
        return Bitmap.createBitmap(pixel, offset, stride, imageHeader.width, imageHeader.height,
                Bitmap.Config.ARGB_8888);
    }

    @Override
    public String getId() {
        return "";
    }
}
