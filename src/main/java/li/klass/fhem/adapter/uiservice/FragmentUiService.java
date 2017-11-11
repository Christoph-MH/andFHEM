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

package li.klass.fhem.adapter.uiservice;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;
import li.klass.fhem.ui.FragmentType;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.HEATING_CONFIGURATION;

@Singleton
public class FragmentUiService {

    @Inject
    public FragmentUiService() {
    }

    public void showIntervalWeekProfileFor(XmlListDevice device, String connectionId, Context context, HeatingConfiguration heatingConfiguration) {
        context.sendBroadcast(new Intent(Actions.SHOW_FRAGMENT)
                .putExtra(FRAGMENT, FragmentType.INTERVAL_WEEK_PROFILE)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(HEATING_CONFIGURATION, heatingConfiguration));
    }
}
