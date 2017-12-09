/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import li.klass.fhem.util.NumberUtil;

import static android.widget.NumberPicker.Formatter;

@SuppressLint("NewApi")
public class FallbackTimePicker extends LinearLayout {
    public interface OnValueChangedListener {
        void onValueChanged(int hours, int minutes);
    }

    private NumberPicker hourPicker;
    private NumberPicker minutePicker;

    private OnValueChangedListener listener;

    public static final Formatter TWO_DIGIT_FORMATTER = new Formatter() {
        @Override
        public String format(int i) {
            return NumberUtil.toTwoDecimalDigits(i);
        }
    };

    @SuppressWarnings("unused")
    public FallbackTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("unused")
    public FallbackTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressWarnings("unused")
    public FallbackTimePicker(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (listener != null) {
                    listener.onValueChanged(getHours(), getMinutes());
                }
            }
        };

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

        LayoutParams numberPickerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        numberPickerLayoutParams.gravity = Gravity.CENTER;
        numberPickerLayoutParams.leftMargin = 15;
        numberPickerLayoutParams.rightMargin = 15;

        hourPicker = new NumberPicker(context);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        initPicker(hourPicker, valueChangeListener, numberPickerLayoutParams);
        addView(hourPicker);

        minutePicker = new NumberPicker(context);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        initPicker(minutePicker, valueChangeListener, numberPickerLayoutParams);
        addView(minutePicker);

    }

    private void initPicker(NumberPicker picker, NumberPicker.OnValueChangeListener valueChangeListener, LayoutParams numberPickerLayoutParams) {
        picker.setFormatter(TWO_DIGIT_FORMATTER);
        picker.setLayoutParams(numberPickerLayoutParams);
        picker.setOnValueChangedListener(valueChangeListener);
    }

    public int getMinutes() {
        return minutePicker.getValue();
    }

    public void setMinutes(int Minutes) {
        minutePicker.setValue(Minutes);
    }

    public int getHours() {
        return hourPicker.getValue();
    }

    public void setHours(int Hours) {
        hourPicker.setValue(Hours);
    }

    @SuppressWarnings("unused")
    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.listener = listener;
    }
}
