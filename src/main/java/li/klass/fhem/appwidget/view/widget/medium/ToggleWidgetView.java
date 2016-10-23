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

package li.klass.fhem.appwidget.view.widget.medium;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.hook.ButtonHook;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class ToggleWidgetView extends DeviceAppWidgetView {
    @Inject
    DeviceHookProvider deviceHookProvider;

    @Inject
    OnOffBehavior onOffBehavior;

    @Override
    public int getWidgetName() {
        return R.string.widget_toggle;
    }

    @Override
    protected int getContentView() {
        return R.layout.appwidget_toggle;
    }

    @Override
    protected void fillWidgetView(Context context, RemoteViews view, FhemDevice<?> device, WidgetConfiguration widgetConfiguration) {
        boolean isOn = onOffBehavior.isOn(device);

        Intent actionIntent;

        ButtonHook hook = deviceHookProvider.buttonHookFor(device);

        if (hook == ButtonHook.NORMAL || hook == ButtonHook.ON_OFF_DEVICE) {
            actionIntent = new Intent(Actions.DEVICE_WIDGET_TOGGLE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                    .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId.orNull());
        } else {
            actionIntent = new Intent(Actions.DEVICE_SET_STATE)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                    .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId.orNull());


            switch (hook) {
                case ON_DEVICE:
                    isOn = true;
                    actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, deviceHookProvider.getOnStateName(device));
                    break;
                case OFF_DEVICE:
                    isOn = false;
                    actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, deviceHookProvider.getOffStateName(device));
                    break;
            }
        }
        actionIntent.setClass(context, DeviceIntentService.class);

        if (isOn) {
            view.setViewVisibility(R.id.toggleOff, View.GONE);
            view.setViewVisibility(R.id.toggleOn, View.VISIBLE);
            view.setTextViewText(R.id.toggleOn, device.getEventMapStateFor(deviceHookProvider.getOnStateName(device)));
        } else {
            view.setViewVisibility(R.id.toggleOff, View.VISIBLE);
            view.setViewVisibility(R.id.toggleOn, View.GONE);
            view.setTextViewText(R.id.toggleOff, device.getEventMapStateFor(deviceHookProvider.getOffStateName(device)));
        }

        PendingIntent pendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.toggleOff, pendingIntent);
        view.setOnClickPendingIntent(R.id.toggleOn, pendingIntent);

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context);
    }

    @Override
    public boolean supports(FhemDevice<?> device) {
        return (device instanceof ToggleableDevice) && ((ToggleableDevice) device).supportsToggle();
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
