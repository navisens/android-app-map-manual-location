package com.navisens.manuallocationnavisensexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;

class Point2D {
    double x;
    double y;
    Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("{ %.2f, %.2f}",x,y );
    }
}

public class HeadingDialView extends View {
    private float dialAngle = 0.0f;
    private final Drawable headingDialDrawable;
    private final double innerTouchBounds = 60;
    private double outerTouchBounds = 160;
    private double lastFi;

    public HeadingDialView(Context context) {
        this(context, null);
    }
    public HeadingDialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public HeadingDialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headingDialDrawable = context.getResources().getDrawable(R.drawable.heading_dial, null);
        } else {
            headingDialDrawable = context.getResources().getDrawable(R.drawable.heading_dial);
        }
    }

    public interface DialListener {
        void onDial(double number);
    }
    private final List<DialListener> dialListeners = new ArrayList<DialListener>();
    // ...
    public void addDialListener(DialListener listener) {
        Log.v(getClass().getSimpleName(),"Adding listener");
        dialListeners.add(listener);
    }
    public void removeDialListener(DialListener listener) {
        Log.v(getClass().getSimpleName(),"Removing listener");
        dialListeners.remove(listener);
    }

    public void reset() {
        dialAngle = 0.0f;
    }

    private void fireDialListenerEvent(double number) {
        // TODO fire dial event
        for (DialListener listener : dialListeners) {
            listener.onDial(number);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = getMeasuredWidth() / 2;
        int y = getMeasuredWidth() / 2;
        canvas.save();
        headingDialDrawable.setBounds(0, 0, this.getMeasuredWidth(), this.getMeasuredWidth());

        canvas.rotate(dialAngle, x, y);
        headingDialDrawable.draw(canvas);
        canvas.restore();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point2D center = new Point2D(getMeasuredWidth() / 2,getMeasuredHeight() / 2);
        outerTouchBounds = center.x;
        Point2D touch = new Point2D(event.getX(), event.getY());
        Point2D displacementFromCenter = new Point2D(center.x - touch.x, center.y - touch.y);
        // use pythagorus
        double   distanceFromCenter = Math.sqrt(pow(displacementFromCenter.x,2) + pow(displacementFromCenter.y,2));
        double sinfi = displacementFromCenter.y / distanceFromCenter;
        double fi = Math.toDegrees(Math.asin(sinfi));
        if (touch.x > center.x && center.y > touch.y) {
            fi = 180 - fi;
        } else if (touch.x > center.x && touch.y > center.y) {
            fi = 180 - fi;
        } else if (center.x > touch.x && touch.y > center.y) {
            fi += 360;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (distanceFromCenter > innerTouchBounds && distanceFromCenter < outerTouchBounds) {
                    dialAngle += fi - lastFi;
                    dialAngle %= 360;
                    lastFi = fi;
                    final float angle = dialAngle % 360;
                    fireDialListenerEvent(angle);
                    invalidate();
                    return true;
                }
            case MotionEvent.ACTION_DOWN:
                lastFi = fi;
                return true;
            case MotionEvent.ACTION_UP:
//                final float angle = dialAngle % 360;
//                fireDialListenerEvent(angle);
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}

