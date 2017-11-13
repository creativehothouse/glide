package com.bumptech.glide.request.target;

import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.apng.ApngDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;

/**
 * A {@link Target} that can display an {@link android.graphics.drawable.AnimationDrawable} in
 * an {@link android.widget.ImageView}.
 *
 * @author Ridho Hadi Satrio <ridho@creativehothouse.com>
 */
public class ApngDrawableImageViewTarget extends ImageViewTarget<ApngDrawable> {

    private final Boolean oneShot;
    private ApngDrawable resource;

    /**
     * Constructor for an {@link Target} that can display an
     * {@link GlideDrawable} in an {@link android.widget.ImageView}.
     *
     * @param view The view to display the drawable in.
     */
    public ApngDrawableImageViewTarget(ImageView view) {
        this(view, Boolean.FALSE);
    }

    /**
     * Constructor for an {@link Target} that can display an
     * {@link GlideDrawable} in an {@link android.widget.ImageView}.
     *
     * @param view    The view to display the drawable in.
     * @param oneShot Whether the animation should be played once or repeated indefinitely.
     *                See {@link AnimationDrawable#setOneShot(boolean)}.
     */
    public ApngDrawableImageViewTarget(ImageView view, Boolean oneShot) {
        super(view);
        this.oneShot = oneShot;
    }

    /**
     * Sets the drawable on the view using
     * {@link android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
     *
     * @param resource The {@link android.graphics.drawable.Drawable} to display in the view.
     */
    @Override
    protected void setResource(ApngDrawable resource) {
        view.setImageDrawable(this.resource = resource);
        this.resource.setOneShot(oneShot);
        this.resource.start();
    }

    @Override
    public void getSize(SizeReadyCallback cb) {
        cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.resource != null) {
            this.resource.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.resource != null) {
            this.resource.stop();
        }
    }
}
