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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Optional;

import java.util.Map;

import li.klass.fhem.R;
import li.klass.fhem.domain.EventMap;
import li.klass.fhem.domain.core.FhemDevice;

public abstract class ToggleActionRow {

    public static final int LAYOUT_DETAIL = R.layout.device_detail_togglebuttonrow;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_togglebuttonrow;
    private TableRow tableRow;
    private TextView descriptionView;
    private ToggleButton toggleButton;

    public ToggleActionRow(LayoutInflater inflater, int layout) {
        tableRow = (TableRow) inflater.inflate(layout, null);
        assert tableRow != null;
        descriptionView = (TextView) tableRow.findViewById(R.id.description);
        toggleButton = (ToggleButton) tableRow.findViewById(R.id.toggleButton);
    }

    public TableRow getView() {
        return tableRow;
    }

    public TableRow createRow(Context context, FhemDevice device, String description) {
        fillWith(context, device, description);
        return tableRow;
    }

    public void fillWith(Context context, FhemDevice device, String description) {
        descriptionView.setText(description);
        toggleButton.setOnClickListener(createListener(context, device, toggleButton));
        setToogleButtonText(device, toggleButton, context);
        toggleButton.setChecked(isOn(device));
    }

    private ToggleButton.OnClickListener createListener(final Context context, final FhemDevice device, final ToggleButton button) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(context, device, button.isChecked());
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected void setToogleButtonText(FhemDevice device, ToggleButton toggleButton, Context context) {
        EventMap eventMap = device.getEventMap();

        Optional<String> onStateText = getOnStateText(eventMap);
        if (onStateText.isPresent()) {
            toggleButton.setTextOn(onStateText.get());
        } else {
            toggleButton.setTextOn(context.getString(R.string.on));
        }

        Optional<String> offStateText = getOffStateText(eventMap);
        if (offStateText.isPresent()) {
            toggleButton.setTextOff(offStateText.get());
        } else {
            toggleButton.setTextOff(context.getString(R.string.off));
        }
    }

    protected Optional<String> getOnStateText(EventMap eventMap) {
        return Optional.fromNullable(eventMap.getValueFor("on"));
    }

    protected Optional<String> getOffStateText(EventMap eventMap) {
        return Optional.fromNullable(eventMap.getValueFor("off"));
    }

    protected abstract boolean isOn(FhemDevice device);

    protected abstract void onButtonClick(final Context context, FhemDevice device, boolean isChecked);
}
