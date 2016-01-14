package com.example.project.drawingboard;

import com.example.project.drawingboard.views.ColorPickerFragment;
import com.example.project.drawingboard.views.PaintCanvasFragment;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.EnumSet;

public class DrawingBoardActivity extends AppCompatActivity implements ColorPickerFragment
        .OnColorPickerListener {

    public enum ActionType {
        NONE, SAVE, ERASE, QUIT
    }


    public static final String LOG_TAG = DrawingBoardActivity.class.getSimpleName();

    //TODO: If we don't want to store any data, could we just do with a bitmask instead?
    private Bundle mScreenStates;

    private PaintCanvasFragment mCanvasFragment;
    private static final String KEY_SAVE_DIALOG = "key_save_dialog";
    private static final String KEY_SCREEN_STATES = "key_screen_states";
    private static final String KEY_LAST_SELECTED_COLOR = "key_last_selected_color";

    private int mLastSelectedColor = Color.BLACK;
    private EnumSet<ActionType> pendingActions = EnumSet.noneOf(ActionType.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScreenStates = (savedInstanceState != null)
                ? savedInstanceState.getBundle(KEY_SCREEN_STATES)
                : new Bundle();

        setContentView(R.layout.activity_drawing_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCanvasFragment = (PaintCanvasFragment) getFragmentManager()
                .findFragmentById(R.id.canvasFragment);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorPicker();
            }
        });

        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drawing_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_erase_drawing) {
            pendingActions.add(ActionType.ERASE);
            showSaveDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // save if needed and finish();
        if (!mCanvasFragment.canSave()) {
            finish();
        } else {
            pendingActions.add(ActionType.QUIT);
            showSaveDialog();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mScreenStates.putInt(KEY_LAST_SELECTED_COLOR, mLastSelectedColor);
        outState.putBundle(KEY_SCREEN_STATES, mScreenStates);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mScreenStates = savedInstanceState.getBundle(KEY_SCREEN_STATES);
        if (mScreenStates == null) {
            Log.e(LOG_TAG, "Could not restore screen states");
            return;
        }
        if (mScreenStates.getBoolean(KEY_SAVE_DIALOG, false)) {
            showSaveDialog();
            ;
        }

        mLastSelectedColor = mScreenStates.getInt(KEY_LAST_SELECTED_COLOR);
        // DialogFragments are managed by the framework across configuration changes, so we do
        // not need to explicitly manage it, however, the order of onRestore..() calls between
        // the dialog fragment and the activity is not guaranteed, so we need to force restore
        // the color to the canvas manually, to avoid timing issues.
        onColorChanged(mLastSelectedColor);
    }

    private void showColorPicker() {

        ColorPickerFragment colorPickerFragment =
                ColorPickerFragment.showDialog(getSupportFragmentManager(), mLastSelectedColor);
        colorPickerFragment.setColorPickerListener(this);
    }

    @Override
    public void onColorChanged(int newColor) {
        mLastSelectedColor = newColor;
        mCanvasFragment.setDrawingColor(newColor);
    }

    @Override
    public void onColorPickerDismissed() {
        // do nothing.
    }

    private void showSaveDialog() {

        final boolean isSaveAndErase = pendingActions.contains(ActionType.ERASE);
        // Overload the UI based on user actions.
        new AlertDialog.Builder(this)
                .setTitle(isSaveAndErase
                        ? R.string.dialog_erase_title
                        : R.string.dialog_save_title)
                .setMessage(isSaveAndErase
                        ? R.string.dialog_save_drawing_msg
                        : R.string.dialog_erase_drawing_msg)
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Snackbar.make(mCanvasFragment.getView(), R.string.status_saving, Snackbar.LENGTH_LONG)
                                .setAction(R.string.status_saving, null).show();
                        performSave();
                    }
                })
                .setNegativeButton(isSaveAndErase ? R.string.action_no : android.R.string.no, new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isSaveAndErase) {
                                    performErase();
                                }
                            }
                        })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mScreenStates.putBoolean(KEY_SAVE_DIALOG, false);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        mScreenStates.putBoolean(KEY_SAVE_DIALOG, true);
    }

    private void performSave() {

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "Spawned a new task to save drawing.");
        }

        new SaveDrawingTask(mCanvasFragment.getBitmap(), this).execute();

        if (pendingActions.contains(ActionType.ERASE)) {
            performErase();
        }

        if (pendingActions.contains(ActionType.QUIT)) {
            finish();
            pendingActions.remove(ActionType.ERASE);
        }
    }

    private void performErase() {
        mCanvasFragment.clearCanvas();
        pendingActions.remove(ActionType.ERASE);
    }

}
