package com.example.project.drawingboard.models;

import com.example.project.drawingboard.BuildConfig;
import com.example.project.drawingboard.DrawingBoardActivity;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

/**
 * A data fragment which has no knowledge about the UI using it. This fragment is retained
 * in memory across configuration changes to avoid persisting the bitmap, for efficiency purpose.
 *
 * Use the {@link DrawingPathCacheStore#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DrawingPathCacheStore extends Fragment {

    public static final String LOG_TAG = DrawingBoardActivity.class.getSimpleName();

    // actual buffer which holds the pixel matrix that we draw on the canvas.
    private Bitmap mBitmap;

    // This canvas object, just provides us with a convenient way to manipulate the bitmap buffer.
    // It is important to note that, this is NOT the canvas gets drawn on the screen.
    private Canvas mCanvas;

    SparseArray<Stroke> mUndoRedoStack = new SparseArray<Stroke>();
    int mUserActionCount = 0;

    private class Stroke {
        Path mPath;
        Paint mPaintConfig;

        public Stroke(Path pathToCopy, Paint paintConfigUsedByPath) {
            mPath = new Path(pathToCopy);
            mPaintConfig = new Paint(paintConfigUsedByPath);
        }
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DrawingPathCacheStore.
     */
    public static DrawingPathCacheStore newInstance(/**Uri*/) {
        DrawingPathCacheStore fragment = new DrawingPathCacheStore();
        fragment.setRetainInstance(true);

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "Retained fragment cacheManager instantiated");
        }
        return fragment;
    }

    public DrawingPathCacheStore() {
        // Required empty public constructor
    }

    public void setCanvasSize(int width, int height) {


        if (mBitmap == null) { // incorrect, we need to rescale the bitmap
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        else {
            // TODO: Add check for the cache being actually dirty, this is wasteful otherwise.
            // Rescale the old bitmap to fit the new size
        }

    }

    public void commitToCache(Path pathToCommit, Paint currentPaintConfig) {
        mUndoRedoStack.append(mUserActionCount++, new Stroke(pathToCommit, currentPaintConfig));
        mCanvas.drawPath(pathToCommit, currentPaintConfig);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }


    public void resetCache() {
        final int height = mBitmap.getHeight();
        final int width = mBitmap.getWidth();

        // free up the underlying buffer
        mBitmap.recycle();
        mBitmap = null;
        mCanvas = null;

        // initialize a new buffer if the same size.
        setCanvasSize(width, height);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        matrix.postTranslate(source.getHeight(),0);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

}
