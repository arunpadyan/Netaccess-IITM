package me.arunpadiyan.netaccess.Objects;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import me.arunpadiyan.netaccess.R;

/**
 * Created by Arun Padiyan on 27-08-2016.
 */

public class CircleView extends View {
    public float sweepAngle = 0.0f; // start at 0 degrees

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.logo_red));
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        RectF oval = new RectF(canvas.getClipBounds());
        canvas.drawArc(oval, 0, sweepAngle, true, p);
    }

    // ---------------------------------------------------
    // getter and setter method to turn "sweepAngle"
    // into a property for ObjectAnimator to use
    // ---------------------------------------------------
    public float getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(float angle) {
        sweepAngle = angle;
        invalidate();
    }

    // ---------------------------------------------------
    // animation method to be called outside this class
    // ---------------------------------------------------
    public void animateArc(float fromAngle, float toAngle, long duration) {
        sweepAngle = fromAngle;

        invalidate();

        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "sweepAngle", fromAngle, toAngle);
        anim.setDuration(duration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // calling invalidate(); will trigger onDraw() to execute
                invalidate();
            }
        });
        anim.start();
    }
}