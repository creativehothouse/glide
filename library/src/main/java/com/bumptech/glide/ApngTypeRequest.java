package com.bumptech.glide;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.resource.apng.ApngDrawable;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.provider.DataLoadProvider;
import com.bumptech.glide.provider.FixedLoadProvider;

import java.io.InputStream;

/**
 * A class for creating a load request that either loads an {@link ApngDrawable}
 * directly or that adds an {@link ResourceTranscoder} to transcode
 * {@link ApngDrawable} into another resource type.
 *
 * @param <ModelType> The type of model to load the {@link ApngDrawable} or other
 *                    transcoded class from.
 */
public class ApngTypeRequest<ModelType> extends ApngRequestBuilder<ModelType> {
    private final ModelLoader<ModelType, InputStream> streamModelLoader;
    private final RequestManager.OptionsApplier optionsApplier;

    ApngTypeRequest(GenericRequestBuilder<ModelType, ?, ?, ?> other,
            ModelLoader<ModelType, InputStream> streamModelLoader,
            RequestManager.OptionsApplier optionsApplier) {
        super(buildProvider(other.glide, streamModelLoader, ApngDrawable.class, null),
                ApngDrawable.class, other);
        this.streamModelLoader = streamModelLoader;
        this.optionsApplier = optionsApplier;

        // Default to animating.
        crossFade();
    }

    private static <A, R> FixedLoadProvider<A, InputStream, ApngDrawable, R> buildProvider(
            Glide glide, ModelLoader<A, InputStream> streamModelLoader, Class<R> transcodeClass,
            ResourceTranscoder<ApngDrawable, R> transcoder) {
        if (streamModelLoader == null) {
            return null;
        }

        if (transcoder == null) {
            transcoder = glide.buildTranscoder(ApngDrawable.class, transcodeClass);
        }
        DataLoadProvider<InputStream, ApngDrawable> dataLoadProvider =
                glide.buildDataProvider(InputStream.class, ApngDrawable.class);
        return new FixedLoadProvider<A, InputStream, ApngDrawable, R>(streamModelLoader,
                transcoder, dataLoadProvider);
    }

    /**
     * Sets a transcoder to transcode the decoded {@link ApngDrawable} into another
     * resource type.
     *
     * @param transcoder     The transcoder to use.
     * @param transcodeClass The {@link Class} of the resource the
     *                       {@link ApngDrawable} will be transcoded to.
     * @param <R>            The type of the resource the {@link ApngDrawable} will be
     *                       trasncoded to.
     * @return This request builder.
     */
    public <R> GenericRequestBuilder<ModelType, InputStream, ApngDrawable, R> transcode(
            ResourceTranscoder<ApngDrawable, R> transcoder, Class<R> transcodeClass) {
        FixedLoadProvider<ModelType, InputStream, ApngDrawable, R> provider =
                buildProvider(glide, streamModelLoader, transcodeClass, transcoder);
        return optionsApplier.apply(new GenericRequestBuilder<ModelType, InputStream, ApngDrawable, R>(provider, transcodeClass, this));
    }

    public void asSequence() {

    }
}
