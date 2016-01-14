package com.example.project.drawingboard.views;

import com.example.project.drawingboard.R;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

/**
 * Created by ritwaj.ratan on 1/10/2016.
 */

/**
 * A Color picker widget which behaves as a dialog.
 */
public class ColorPickerFragment extends DialogFragment {

    /**
     * Provides hooks for listening to events originating from this widget.
     */
    public interface OnColorPickerListener {
        /**
         * Notifies listeners of the newly selected color. This is called only when the new color
         * and old color really differ.
         *
         * @param newColor RGB components of the new color, packed in an integer.
         */
        void onColorChanged(int newColor);

        /**
         * Notifies the listeners that this widget has been dismissed.
         */
        void onColorPickerDismissed();
    }

    public static final String LOG_TAG = ColorPickerFragment.class.getSimpleName();
    public static final String TAG_COLOR_PICKER_DIALOG = "color_picker_dialog";
    private static final String KEY_SELECTED_COLOR = "key_selected_color";

    // represents the current selection on the dialog.
    private int mSelectedColor = Color.BLACK;
    private int mInitialColor = Color.BLACK;
    private OnColorPickerListener mListener;

    public ColorPickerFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of this fragment.
     *
     * @param initialColor Color that the picker should initialize with.
     * @return instance of the color picker.
     */
    public static ColorPickerFragment newInstance(int initialColor) {
        ColorPickerFragment colorPickerFragment = new ColorPickerFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_SELECTED_COLOR, initialColor);
        colorPickerFragment.setArguments(args);

        return colorPickerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedColor = mInitialColor = (savedInstanceState != null)
                ? savedInstanceState.getInt(KEY_SELECTED_COLOR)
                : getArguments().getInt(KEY_SELECTED_COLOR);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_color_picker,
                container, false);

        // configure the views
        View colorSwatch = layout.findViewById(R.id.colorSwatch);
        SeekBar redSeekbar = (SeekBar) layout.findViewById(R.id.redSeekBar);
        SeekBar greenSeekbar = (SeekBar) layout.findViewById(R.id.greenSeekBar);
        SeekBar blueSeekbar = (SeekBar) layout.findViewById(R.id.blueSeekBar);
        Button okAction = (Button) layout.findViewById(R.id.ok_action);
        Button cancelAction = (Button) layout.findViewById(R.id.cancel_action);

        setupColorChangeListener(redSeekbar, colorSwatch);
        setupColorChangeListener(greenSeekbar, colorSwatch);
        setupColorChangeListener(blueSeekbar, colorSwatch);

        if (savedInstanceState != null) {
            // mSelectedColor would have been initialized for us by onCreate()
            setStatesFromSelectedColor(redSeekbar, greenSeekbar, blueSeekbar);
        } else {
            setStatesFromSelectedColor(redSeekbar, greenSeekbar, blueSeekbar);
        }

        colorSwatch.setBackgroundColor(Color.rgb(redSeekbar.getProgress()
                , greenSeekbar.getProgress()
                , blueSeekbar.getProgress()));

        // configure the dialog properties of this fragment
        getDialog().setTitle(R.string.color_picker_title);
        getDialog().setCancelable(true);

        // finally, wire in the hooks for our callbacks.
        setupInteractionListeners(okAction, cancelAction);

        return layout;
    }

    private void setStatesFromSelectedColor(final SeekBar redSeekbar, final SeekBar greenSeekbar,
                                            final SeekBar blueSeekbar) {
        redSeekbar.setProgress(Color.red(mSelectedColor));
        greenSeekbar.setProgress(Color.green(mSelectedColor));
        blueSeekbar.setProgress(Color.blue(mSelectedColor));
    }


    /**
     * Registers a listener for listening to user's selection changes on this widget.
     *
     * @param colorChangedListener An instance of
     *                             {@link OnColorPickerListener}
     */
    public void setColorPickerListener(OnColorPickerListener colorChangedListener) {
        mListener = colorChangedListener;
    }

    /**
     * Shows the color picker
     *
     * @return Returns the reference to the dialog being shown. It is not advisable to cache
     * this reference, holding onto this reference might cause memory leaks.
     */
    public static ColorPickerFragment showDialog(FragmentManager fragmentManager, int initialColor) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment oldPicker = fragmentManager.findFragmentByTag(TAG_COLOR_PICKER_DIALOG);
        if (oldPicker != null) {
            fragmentTransaction.remove(oldPicker);
        }
        fragmentTransaction.addToBackStack(null);
        ColorPickerFragment colorPickerFragment = ColorPickerFragment.newInstance(initialColor);
        colorPickerFragment.show(fragmentTransaction, TAG_COLOR_PICKER_DIALOG);
        return colorPickerFragment;
    }

    private void setupInteractionListeners(final Button okAction, final Button cancelAction) {
        getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mListener != null) {
                    mListener.onColorPickerDismissed();
                }
            }
        });
        okAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onColorChanged(mSelectedColor);
                }
                ColorPickerFragment.this.dismiss();
            }
        });
        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerFragment.this.dismiss();
                if (mListener != null) {
                    // revert back to the original color.
                    mSelectedColor = mInitialColor;
                    mListener.onColorPickerDismissed();
                    mListener.onColorChanged(mSelectedColor);
                }
            }
        });
    }

    /**
     * A common multiplexed listener for all the color seekbars that our UI uses.
     */
    private void setupColorChangeListener(final SeekBar seekbar, final View colorSwatch) {
        if (seekbar == null) return;

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                final int redComponent = Color.red(mSelectedColor);
                final int greenComponent = Color.green(mSelectedColor);
                final int blueComponent = Color.blue(mSelectedColor);
                switch (seekBar.getId()) {
                    case R.id.redSeekBar:
                        mSelectedColor = Color.rgb(progress, greenComponent, blueComponent);
                        break;
                    case R.id.greenSeekBar:
                        mSelectedColor = Color.rgb(redComponent, progress, blueComponent);
                        break;
                    case R.id.blueSeekBar:
                        mSelectedColor = Color.rgb(redComponent, greenComponent, progress);
                        break;
                    default:
                        mSelectedColor = Color.BLACK;
                }
                colorSwatch.setBackgroundColor(mSelectedColor);
                if (mListener != null) {
                    mListener.onColorChanged(mSelectedColor);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
