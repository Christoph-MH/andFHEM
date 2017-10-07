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

package li.klass.fhem.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.appwidget.service.AppWidgetUpdateService;
import li.klass.fhem.dagger.ApplicationComponent;

import static li.klass.fhem.constants.Actions.REDRAW_WIDGET;
import static li.klass.fhem.constants.BundleExtraKeys.ALLOW_REMOTE_UPDATES;
import static li.klass.fhem.constants.BundleExtraKeys.APP_WIDGET_ID;

public abstract class AndFHEMAppWidgetProvider extends AppWidgetProvider {

    @Inject
    AppWidgetDataHolder appWidgetDataHolder;

    protected AndFHEMAppWidgetProvider() {
        AndFHEMApplication application = AndFHEMApplication.Companion.getApplication();
        if (application != null) {
            inject(application.getDaggerComponent());
        }
    }

    protected abstract void inject(ApplicationComponent applicationComponent);

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            context.startService(new Intent(REDRAW_WIDGET).setClass(context, AppWidgetUpdateService.class)
                    .putExtra(APP_WIDGET_ID, appWidgetId)
                    .putExtra(ALLOW_REMOTE_UPDATES, true));
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            appWidgetDataHolder.deleteWidget(context, appWidgetId);
        }
    }
}
