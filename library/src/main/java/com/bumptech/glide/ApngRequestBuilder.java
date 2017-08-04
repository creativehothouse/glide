package com.bumptech.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;

import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.apng.ApngDrawable;
import com.bumptech.glide.load.resource.apng.ApngDrawableTransformation;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.provider.LoadProvider;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.DrawableCrossFadeFactory;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;

import java.io.File;
import java.io.InputStream;

/**
 * A class for creating a request to load an animated gif.
 * <p>
 * <p>
 * Warning - It is <em>not</em> safe to use this builder after calling <code>into()</code>, it may
 * be pooled and
 * reused.
 * </p>
 *
 * @param <ModelType> The type of model that will be loaded into the target.
 */
public class ApngRequestBuilder<ModelType>
        extends GenericRequestBuilder<ModelType, InputStream, ApngDrawable, ApngDrawable>
        implements BitmapOptions, DrawableOptions {

    ApngRequestBuilder(LoadProvider<ModelType, InputStream, ApngDrawable, ApngDrawable> loadProvider,
            Class<ApngDrawable> transcodeClass, GenericRequestBuilder<ModelType, ?, ?, ?> other) {
        super(loadProvider, transcodeClass, other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> thumbnail(
            GenericRequestBuilder<?, ?, ?, ApngDrawable> thumbnailRequest) {
        super.thumbnail(thumbnailRequest);
        return this;
    }

    /**
     * Loads and displays the GIF retrieved by the given thumbnail request if it finishes before this
     * request. Best used for loading thumbnail GIFs that are smaller and will be loaded more quickly
     * than the fullsize GIF. There are no guarantees about the order in which the requests will
     * actually
     * finish. However, if the thumb request completes after the full request, the thumb GIF will
     * never
     * replace the full image.
     *
     * @param thumbnailRequest The request to use to load the thumbnail.
     * @return This builder object.
     * @see #thumbnail(float)
     * <p>
     * <p>
     * Note - Any options on the main request will not be passed on to the thumbnail request. For
     * example, if
     * you want an animation to occur when either the full GIF loads or the thumbnail loads,
     * you need to call {@link #animate(int)} on both the thumb and the full request. For a simpler
     * thumbnail
     * option where these options are applied to the humbnail as well, see {@link #thumbnail(float)}.
     * </p>
     * <p>
     * <p>
     * Only the thumbnail call on the main request will be obeyed, recursive calls to this method are
     * ignored.
     * </p>
     */
    public ApngRequestBuilder<ModelType> thumbnail(ApngRequestBuilder<?> thumbnailRequest) {
        super.thumbnail(thumbnailRequest);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> thumbnail(float sizeMultiplier) {
        super.thumbnail(sizeMultiplier);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> sizeMultiplier(float sizeMultiplier) {
        super.sizeMultiplier(sizeMultiplier);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> decoder(
            ResourceDecoder<InputStream, ApngDrawable> decoder) {
        super.decoder(decoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> cacheDecoder(
            ResourceDecoder<File, ApngDrawable> cacheDecoder) {
        super.cacheDecoder(cacheDecoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> encoder(ResourceEncoder<ApngDrawable> encoder) {
        super.encoder(encoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> priority(Priority priority) {
        super.priority(priority);
        return this;
    }

    /**
     * Transforms each frame of the GIF using {@link CenterCrop}.
     *
     * @return This request builder.
     * @see #fitCenter()
     * @see #transformFrame(BitmapTransformation...)
     * @see #transformFrame(Transformation[])
     * @see #transform(Transformation[])
     */
    public ApngRequestBuilder<ModelType> centerCrop() {
        return transformFrame(glide.getBitmapCenterCrop());
    }

    /**
     * Transforms each frame of the GIF using {@link FitCenter}.
     *
     * @return This request builder..
     * @see #centerCrop()
     * @see #transformFrame(BitmapTransformation...)
     * @see #transformFrame(Transformation[])
     * @see #transform(Transformation[])
     */
    public ApngRequestBuilder<ModelType> fitCenter() {
        return transformFrame(glide.getBitmapFitCenter());
    }

    /**
     * Transforms each frame of the GIF using the given transformations.
     *
     * @param bitmapTransformations The transformations to apply in order to each frame.
     * @return This request builder.
     * @see #centerCrop()
     * @see #fitCenter()
     * @see #transformFrame(Transformation[])
     * @see #transform(Transformation[])
     */
    public ApngRequestBuilder<ModelType> transformFrame(
            BitmapTransformation... bitmapTransformations) {
        return transform(toApngTransformations(bitmapTransformations));
    }

    /**
     * Transforms each frame of the GIF using the given transformations.
     *
     * @param bitmapTransformations The transformations to apply in order to each frame.
     * @return This request builder.
     * @see #fitCenter()
     * @see #centerCrop()
     * @see #transformFrame(BitmapTransformation...)
     * @see #transform(Transformation[])
     */
    public ApngRequestBuilder<ModelType> transformFrame(
            Transformation<Bitmap>... bitmapTransformations) {
        return transform(toApngTransformations(bitmapTransformations));
    }

    private ApngDrawableTransformation[] toApngTransformations(
            Transformation<Bitmap>[] bitmapTransformations) {
        ApngDrawableTransformation[] transformations =
                new ApngDrawableTransformation[bitmapTransformations.length];
        for (int i = 0; i < bitmapTransformations.length; i++) {
            transformations[i] =
                    new ApngDrawableTransformation(bitmapTransformations[i], glide.getBitmapPool());
        }
        return transformations;
    }

    /**
     * {@inheritDoc}
     *
     * @see #fitCenter()
     * @see #centerCrop()
     * @see #transformFrame(BitmapTransformation...)
     * @see #transformFrame(Transformation[])
     */
    @Override
    public ApngRequestBuilder<ModelType> transform(
            Transformation<ApngDrawable>... transformations) {
        super.transform(transformations);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> transcoder(
            ResourceTranscoder<ApngDrawable, ApngDrawable> transcoder) {
        super.transcoder(transcoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> crossFade() {
        super.animate(new DrawableCrossFadeFactory<ApngDrawable>());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> crossFade(int duration) {
        super.animate(new DrawableCrossFadeFactory<ApngDrawable>(duration));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public ApngRequestBuilder<ModelType> crossFade(Animation animation,
            int duration) {
        super.animate(new DrawableCrossFadeFactory<ApngDrawable>(animation, duration));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> crossFade(int animationId, int duration) {
        super.animate(new DrawableCrossFadeFactory<ApngDrawable>(context, animationId, duration));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> dontAnimate() {
        super.dontAnimate();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> animate(int animationId) {
        super.animate(animationId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    @Override
    public ApngRequestBuilder<ModelType> animate(Animation animation) {
        super.animate(animation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> animate(ViewPropertyAnimation.Animator animator) {
        super.animate(animator);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> placeholder(int resourceId) {
        super.placeholder(resourceId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> placeholder(Drawable drawable) {
        super.placeholder(drawable);
        return this;
    }

    @Override
    public ApngRequestBuilder<ModelType> fallback(Drawable drawable) {
        super.fallback(drawable);
        return this;
    }

    @Override
    public ApngRequestBuilder<ModelType> fallback(int resourceId) {
        super.fallback(resourceId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> error(int resourceId) {
        super.error(resourceId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> error(Drawable drawable) {
        super.error(drawable);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> listener(
            RequestListener<? super ModelType, ApngDrawable> requestListener) {
        super.listener(requestListener);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> skipMemoryCache(boolean skip) {
        super.skipMemoryCache(skip);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> diskCacheStrategy(DiskCacheStrategy strategy) {
        super.diskCacheStrategy(strategy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> override(int width, int height) {
        super.override(width, height);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> sourceEncoder(Encoder<InputStream> sourceEncoder) {
        super.sourceEncoder(sourceEncoder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApngRequestBuilder<ModelType> dontTransform() {
        super.dontTransform();
        return this;
    }

    @Override
    public ApngRequestBuilder<ModelType> signature(Key signature) {
        super.signature(signature);
        return this;
    }

    @Override
    public ApngRequestBuilder<ModelType> load(ModelType model) {
        super.load(model);
        return this;
    }

    @Override
    public ApngRequestBuilder<ModelType> clone() {
        return (ApngRequestBuilder<ModelType>) super.clone();
    }

    @Override
    void applyFitCenter() {
        fitCenter();
    }

    @Override
    void applyCenterCrop() {
        centerCrop();
    }
}
