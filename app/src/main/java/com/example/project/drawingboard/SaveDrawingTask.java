package com.example.project.drawingboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ritwaj.ratan on 1/13/2016.
 */

/**
 * Asynchronously saves the provided bitmap to the external DCIM directory.
 */
public class SaveDrawingTask extends AsyncTask<Void, Void, Uri> {

    private static final String LOG_TAG = SaveDrawingTask.class.getSimpleName();

    private Activity mUiContext;
    private Bitmap mDrawing;

    //TODO: this should ideally be pulled in from user settings (preference)
    private static final File STORAGE_PATH = Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DCIM);

    public SaveDrawingTask(Bitmap drawing, Activity uiContext) {
        mUiContext = uiContext;
        mDrawing = drawing;
    }

    @Override
    protected Uri doInBackground(Void... taskInputParams) {

        Uri savedFileUri = Uri.EMPTY;
        if (!STORAGE_PATH.exists()) {
            Log.e(LOG_TAG, "Cannot find path to pictures gallery");
        }
        try {
            // Save to the default camera (DCIM) album.
            final String newImageFilePath = STORAGE_PATH +
                    File.separator + "drawing_" + System.currentTimeMillis() / 1000 + ".png";
            File userDrawing = new File(newImageFilePath);
            final FileOutputStream outputStream = new FileOutputStream(userDrawing);
            mDrawing.compress(Bitmap.CompressFormat.PNG, 0 /** PNG formats ignore the quality
             parameter anyway*/, outputStream);
            outputStream.flush();
            outputStream.close();

            savedFileUri = Uri.fromFile(userDrawing);

            // force the media content provider to update with this file.
            mUiContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    savedFileUri));

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, " saved the drawing to " + newImageFilePath);
            }


        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(LOG_TAG, "Unable to create the image");
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Could not write to gallery");
        }

        return savedFileUri;
    }

    @Override
    protected void onPostExecute(final Uri savedFileUri) {
        if (mUiContext != null && savedFileUri != null
                && !savedFileUri.getPath().isEmpty()) {
            Toast.makeText((Context) mUiContext, R.string
                    .snackbar_drawing_saved, Toast.LENGTH_LONG);
        }
    }

}
