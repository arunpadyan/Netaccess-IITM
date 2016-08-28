package me.arunpadiyan.netaccess.Objects;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import me.arunpadiyan.netaccess.R;

/**
 * Created by Arun Padiyan on 27-08-2016.
 */

public class CircleView extends View {
    public float sweepAngle = 0.0f;
    Paint paint;
    int color= R.color.logo_green;
    int minX, minY, maxX, maxY;

// start at 0 degrees

    public CircleView(Context context) {
        super(context);
        init();

    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();

    }

    public void setColor(int color){
        this.color = color;
    }


    @Override
    protected void onDraw(Canvas canvas) {
       // canvas.drawColor(ContextCompat.getColor(getContext(), R.color.logo_red));
        minX = getPaddingLeft();
        maxX = getWidth() - getPaddingLeft() - getPaddingRight();
        minY = getPaddingTop();
        maxY = getHeight() - getPaddingTop() - getPaddingBottom();
        arcorange(canvas, minX, maxX, minY, maxY);
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
    private void init(){
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(),color));
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

    }
    protected void arcorange(Canvas canvas, int xmin, int xmax, int ymin, int ymax){
        final RectF oval = new RectF();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(ContextCompat.getColor(getContext(),color));
        canvas.clipRect(xmin, ymin, xmax, ymax, Region.Op.REPLACE);
        oval.set(xmin+10, ymin+10, xmax-10, ymax-10);
        canvas.drawArc(oval, 0, sweepAngle, false, paint);

    }


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