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

package li.klass.fhem.service.device;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMRegistrar;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.service.AbstractService;
import li.klass.fhem.service.Command;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.ArrayUtil;

import static com.google.common.base.Strings.isNullOrEmpty;
import static li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID;
import static li.klass.fhem.constants.PreferenceKeys.GCM_REGISTRATION_ID;

@Singleton
public class GCMSendDeviceService extends AbstractService {
    private static final String ATTR_REG_IDS_COMMAND = "attr %s regIds %s";

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    public GCMSendDeviceService() {
    }

    public void addSelf(GCMSendDevice device, Context context) {

        String registrationId = applicationProperties.getStringSharedPreference(GCM_REGISTRATION_ID, null, context);
        if (isNullOrEmpty(registrationId)) {
            showToast(R.string.gcmRegistrationNotActive, context);
            return;
        }

        if (ArrayUtil.contains(device.getRegIds(), registrationId)) {
            showToast(R.string.gcmAlreadyRegistered, context);
            return;
        }

        String[] newRegIds = ArrayUtil.addToArray(device.getRegIds(), registrationId);
        setRegIdsAttributeFor(device, newRegIds, context);

        Intent intent = new Intent(Actions.DO_UPDATE);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, true);
        context.sendBroadcast(intent);

        showToast(R.string.gcmSuccessfullyRegistered, context);
    }

    private void setRegIdsAttributeFor(GCMSendDevice device, String[] newRegIds, Context context) {
        String regIdsAttribute = ArrayUtil.join(newRegIds, "|");

        commandExecutionService.executeSync(new Command(String.format(ATTR_REG_IDS_COMMAND, device.getName(), regIdsAttribute)), context);
        device.setRegIds(regIdsAttribute);
    }

    public void removeRegistrationId(GCMSendDevice device, String registrationId, Context context) {
        if (!ArrayUtil.contains(device.getRegIds(), registrationId)) {
            return;
        }

        String[] newRegIds = ArrayUtil.removeFromArray(device.getRegIds(), registrationId);
        setRegIdsAttributeFor(device, newRegIds, context);
    }

    public boolean isDeviceRegistered(GCMSendDevice device, Context context) {
        String registrationId = applicationProperties.getStringSharedPreference(GCM_REGISTRATION_ID, null, context);

        return (registrationId != null && ArrayUtil.contains(device.getRegIds(), registrationId));
    }

    public void registerWithGCM(Context context) {
        String projectId = applicationProperties.getStringSharedPreference(GCM_PROJECT_ID, null, context);
        registerWithGCMInternal(context, projectId);
    }

    private void registerWithGCMInternal(Context context, String projectId) {
        if (isNullOrEmpty(projectId)) return;

        if (!GCMRegistrar.isRegisteredOnServer(context)) {
            GCMRegistrar.checkDevice(context);
            GCMRegistrar.checkManifest(context);

            GCMRegistrar.setRegisterOnServerLifespan(context, 1000L * 60 * 60 * 24 * 30);
            GCMRegistrar.register(context, projectId);
        }
    }

    public void registerWithGCM(Context context, String projectId) {
        applicationProperties.setSharedPreference(GCM_REGISTRATION_ID, null, context);
        GCMRegistrar.unregister(context);
        registerWithGCMInternal(context, projectId);
    }
}
