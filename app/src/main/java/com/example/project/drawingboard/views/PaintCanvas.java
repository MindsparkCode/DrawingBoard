package com.example.project.drawingboard.views;

import com.example.project.drawingboard.BuildConfig;
import com.example.project.drawingboard.models.DrawingPathCacheStore;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ritwaj.ratan on 1/10/2016.
 */

/**
 * A view that translates user touches to a colored path.
 * This view maintains an offscreen buffer to handle changes in view size and can be further
 * extended for undo/redo as it tracts each drawing path.
 */
public class PaintCanvas extends View {

    public static final String LOG_TAG = PaintCanvas.class.getSimpleName();

    private Context mHostContext;

    // represents the (temporary) disconnected paths that user draws between a
    // touch down and a touch up.
    private Path mDisconnectedPath = new Path();

    // Holds the paint style and color information.
    private Paint mPaintConfig = new Paint();

    // Default assumptions for paint configuration.
    private int mCurrentPaintColor = Color.WHITE;
    private final float STROKE_WIDTH = 5f;

    // UndRedoCacheManager
    private DrawingPathCacheStore mCacheManager;
    private boolean mIsDirty = false;
    private float mX, mY;

    // Threshold under which we do not consider translating user events to the canvas.
    private static final float TOUCH_TOLERANCE = 4;

    public PaintCanvas(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public PaintCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public PaintCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        mDisconnectedPath = new Path();
        mPaintConfig = new Paint();
        applyDefaultConfigurations();
        // applyCustomStyledAttributes(context, attrs, defStyleAttr);

        // initialize the UndoRedoManager
        // we can safely assume that context here is a Activity, true for any View
        android.app.FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
        mCacheManager = (DrawingPathCacheStore)
                fragmentManager.findFragmentByTag(DrawingPathCacheStore.LOG_TAG);
        if (mCacheManager == null) {
            mCacheManager = DrawingPathCacheStore.newInstance();
            fragmentManager.beginTransaction().add(mCacheManager, DrawingPathCacheStore.LOG_TAG)
                    .commit();
        }
    }

    /**
     * Sets a color to for subsequent drawing calls.
     *
     * @param newColor is the color in which the next drawing will take place (if any).
     */
    public void setDrawingColor(int newColor) {
        if (mPaintConfig != null && mPaintConfig.getColor() != newColor) {
            mCurrentPaintColor = newColor;
            mPaintConfig.setColor(mCurrentPaintColor);
        }
    }

    /**
     * Applies the default assumptions for color and style that this view uses for drawing.
     * Drawing color can be overwritten by the client using
     * {@link PaintCanvas#setDrawingColor(int)}
     */
    private void applyDefaultConfigurations() {
        mPaintConfig.setColor(Color.BLACK);

        mPaintConfig.setStrokeWidth(STROKE_WIDTH);
        mPaintConfig.setStyle(Paint.Style.STROKE);

        // smoothen out the edges and path joins.
        mPaintConfig.setAntiAlias(true);
        mPaintConfig.setStrokeCap(Paint.Cap.ROUND);
        mPaintConfig.setStrokeJoin(Paint.Join.ROUND);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCacheManager.setCanvasSize(w, h);

        if (BuildConfig.DEBUG) {
            // Log.d(LOG_TAG, "onSizeChanged()");
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // All the drawing on the view should take place from here as this is the actual canvas
        // which backs up the view.
        // Ref: http://developer.android.com/guide/topics/graphics/2d-graphics.html#draw-with-canvas
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mCacheManager.getBitmap(), 0, 0, mPaintConfig);
        canvas.drawPath(mDisconnectedPath, mPaintConfig);

        // save off whatever we draw on screen, to the cache.
        mCacheManager.commitToCache(mDisconnectedPath, mPaintConfig);


    }

    private void touchStart(float x, float y) {
        mDisconnectedPath.reset();
        mDisconnectedPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // Keeps extending the path with normalized curve to the path since last TOUCH_DOWN
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mDisconnectedPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mDisconnectedPath.lineTo(mX, mY);
        // clear the path, to pickup only the delta.
        mDisconnectedPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        if (!mIsDirty) {
            mIsDirty = true;
        }
        return true;
    }

    public boolean canSave() {
        return mIsDirty;
    }

    /**
     * @return an immutable copy of the current draw buffer being used internally by PaintCanvas.
     */
    public Bitmap getBitmap() {
        setDrawingCacheEnabled(true);
        return Bitmap.createBitmap(getDrawingCache());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // We use this as a signal to commit our buffer to the cache and to clean up
        // strong references.
        // mCacheManager = null;
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable restoreStatesFrom) {
        super.onRestoreInstanceState(restoreStatesFrom);
    }

    /**
     * Clears any drawing present on this view, including offscreen caches.
     */
    public void clearCanvas() {
        mDisconnectedPath.reset();
        mCacheManager.resetCache();
        invalidate();
    }

}
