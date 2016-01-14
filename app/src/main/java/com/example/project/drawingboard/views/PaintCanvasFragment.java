package com.example.project.drawingboard.views;

import com.example.project.drawingboard.R;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment wrapping {@link PaintCanvas}.
 * This is just a convenience fragment to be used in UI and can be easily extended.
 */
public class PaintCanvasFragment extends Fragment {

    private static final String LOG_TAG = PaintCanvasFragment.class.getSimpleName();

    private PaintCanvas mCanvas;

    public PaintCanvasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_drawing_board, container, false);
        mCanvas = (PaintCanvas) root.findViewById(R.id.paintCanvas);
        return root;
    }

    /**
     * Clears the drawing on the canvas.
     */
    public void clearCanvas() {
        if (mCanvas != null) {
            mCanvas.clearCanvas();
        }
    }

    /**
     * @return Returns true if the canvas is dirty (can be saved), false otherwise
     */
    public boolean canSave() {
        return mCanvas.canSave();
    }

    /**
     * Returns the bitmap buffer from the underlying canvas being used for the painting.
     */
    public Bitmap getBitmap() {
        return mCanvas.getBitmap();
    }

    /**
     * Sets a color to for subsequent drawing calls.
     *
     * @param newColor is the color in which the next drawing will take place (if any).
     */
    public void setDrawingColor(int newColor) {
        if (mCanvas != null) {
            mCanvas.setDrawingColor(newColor);
        }
    }

}
