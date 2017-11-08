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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import li.klass.fhem.R;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.settings.SettingsKeys.SHOW_SET_VALUE_BUTTONS;
import static li.klass.fhem.util.NumberUtil.isDecimalNumber;

public abstract class SeekBarActionRowFullWidthAndButton extends SeekBarActionRowFullWidth {

    public static final int LAYOUT_DETAIL = R.layout.device_detail_seekbarrow_with_button;
    protected Context context;

    public SeekBarActionRowFullWidthAndButton(Context context, float initialProgress, float step, float minimumProgress, float maximumProgress, TableRow updateRow) {
        super(initialProgress, minimumProgress, step, maximumProgress, LAYOUT_DETAIL, updateRow);
        this.context = context;
    }

    @Override
    public TableRow createRow(LayoutInflater inflater, XmlListDevice device, int layoutSpan) {
        TableRow row = super.createRow(inflater, device, 1);
        applySetButtonIfRequired(device, row);

        TableLayout layout = (TableLayout) row.findViewById(R.id.seekBarLayout);
        if (layout != null && layoutSpan != 1) {
            TableRow.LayoutParams layoutParams = (TableRow.LayoutParams) layout.getLayoutParams();
            layoutParams.span = layoutSpan;
            layout.setLayoutParams(layoutParams);
        }

        return row;
    }

    private void applySetButtonIfRequired(final XmlListDevice device, TableRow row) {
        Button button = (Button) row.findViewById(R.id.button);
        if (button == null) return;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = context.getString(R.string.set_value);

                DialogUtil.showInputBox(context, title, initialProgress + "", new DialogUtil.InputDialogListener() {
                    @Override
                    public void onClick(String text) {
                        if (isDecimalNumber(text)) {
                            onButtonSetValue(device, Float.parseFloat(text));
                        } else {
                            DialogUtil.showAlertDialog(context, R.string.error, R.string.invalidInput);
                        }
                    }
                });
            }
        });
        if (!showButton()) {
            button.setVisibility(View.GONE);
        }
    }

    public abstract void onButtonSetValue(XmlListDevice device, float value);

    protected boolean showButton() {
        return getApplicationProperties().getBooleanSharedPreference(SHOW_SET_VALUE_BUTTONS, false, context);
    }

    protected abstract ApplicationProperties getApplicationProperties();

    @Override
    public TableRow createRow(final LayoutInflater inflater, final XmlListDevice device) {
        TableRow row = super.createRow(inflater, device);
        applySetButtonIfRequired(device, row);

        return row;
    }
}
