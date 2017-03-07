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

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.heating.ComfortTempDevice;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.EcoTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.WindowOpenTempDevice;
import li.klass.fhem.service.Command;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.NotificationService;
import li.klass.fhem.service.device.AtService;
import li.klass.fhem.service.device.DeviceService;
import li.klass.fhem.service.device.DimmableDeviceService;
import li.klass.fhem.service.device.GCMSendDeviceService;
import li.klass.fhem.service.device.GenericDeviceService;
import li.klass.fhem.service.device.HeatingService;
import li.klass.fhem.service.device.ToggleableService;
import li.klass.fhem.service.graph.GraphEntry;
import li.klass.fhem.service.graph.GraphService;
import li.klass.fhem.service.graph.gplot.GPlotSeries;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.room.FavoritesService;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.StateToSet;

import static li.klass.fhem.constants.Actions.DEVICE_DELETE;
import static li.klass.fhem.constants.Actions.DEVICE_DIM;
import static li.klass.fhem.constants.Actions.DEVICE_GRAPH;
import static li.klass.fhem.constants.Actions.DEVICE_MOVE_ROOM;
import static li.klass.fhem.constants.Actions.DEVICE_RENAME;
import static li.klass.fhem.constants.Actions.DEVICE_RESET_WEEK_PROFILE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_ALIAS;
import static li.klass.fhem.constants.Actions.DEVICE_SET_COMFORT_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_DESIRED_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_ECO_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_MODE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_SUB_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_SUB_STATES;
import static li.klass.fhem.constants.Actions.DEVICE_SET_WEEK_PROFILE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_WINDOW_OPEN_TEMPERATURE;
import static li.klass.fhem.constants.Actions.DEVICE_TIMER_MODIFY;
import static li.klass.fhem.constants.Actions.DEVICE_TIMER_NEW;
import static li.klass.fhem.constants.Actions.DEVICE_TOGGLE_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_WIDGET_TOGGLE;
import static li.klass.fhem.constants.Actions.GCM_ADD_SELF;
import static li.klass.fhem.constants.Actions.GCM_REMOVE_ID;
import static li.klass.fhem.constants.Actions.RESEND_LAST_FAILED_COMMAND;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_DIM_PROGRESS;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_DEFINITION;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_GRAPH_ENTRY_MAP;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_MODE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NEW_ALIAS;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NEW_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NEW_ROOM;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_TEMPERATURE;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.END_DATE;
import static li.klass.fhem.constants.BundleExtraKeys.GCM_REGISTRATION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.START_DATE;
import static li.klass.fhem.constants.BundleExtraKeys.STATES;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_VALUE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_HOUR;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_IS_ACTIVE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_MINUTE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_REPETITION;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_SECOND;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_STATE_APPENDIX;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TYPE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMES_TO_SEND;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.DONE;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.ERROR;
import static li.klass.fhem.service.intent.ConvenientIntentService.STATE.SUCCESS;
import static li.klass.fhem.service.room.RoomListService.RemoteUpdateRequired.REQUIRED;

public class DeviceIntentService extends ConvenientIntentService {

    @Inject
    RoomListService roomListService;
    @Inject
    HeatingService heatingService;
    @Inject
    DeviceService deviceService;
    @Inject
    FavoritesService favoritesService;
    @Inject
    GenericDeviceService genericDeviceService;
    @Inject
    GCMSendDeviceService gcmSendDeviceService;
    @Inject
    CommandExecutionService commandExecutionService;
    @Inject
    AtService atService;
    @Inject
    DimmableDeviceService dimmableDeviceService;
    @Inject
    ToggleableService toggleableService;
    @Inject
    GraphService graphService;
    @Inject
    NotificationService notificationService;

    private static final Logger LOG = LoggerFactory.getLogger(DeviceIntentService.class);

    public DeviceIntentService() {
        super(DeviceIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {

        if (roomListService.updateRoomDeviceListIfRequired(intent, updatePeriod, this) == REQUIRED) {
            return DONE;
        }

        String deviceName = intent.getStringExtra(DEVICE_NAME);
        Optional<String> connectionId = Optional.fromNullable(intent.getStringExtra(CONNECTION_ID));

        Optional<FhemDevice> deviceOptional = roomListService.getDeviceForName(deviceName, connectionId, this);
        if (!deviceOptional.isPresent()) {
            LOG.info("handleIntent() - cannot find device for {}", deviceName);
        }
        FhemDevice device = deviceOptional.orNull();

        Log.d(DeviceIntentService.class.getName(), intent.getAction());
        String action = intent.getAction();

        STATE result = STATE.SUCCESS;
        if (DEVICE_GRAPH.equals(action)) {
            result = graphIntent(intent, device, resultReceiver);
        } else if (DEVICE_TOGGLE_STATE.equals(action)) {
            result = toggleIntent(device, connectionId);
        } else if (DEVICE_SET_STATE.equals(action)) {
            result = setStateIntent(intent, device, connectionId);
        } else if (DEVICE_DIM.equals(action)) {
            result = dimIntent(intent, device);
        } else if (DEVICE_SET_MODE.equals(action)) {
            if (device instanceof HeatingDevice) {
                Enum mode = (Enum) intent.getSerializableExtra(DEVICE_MODE);
                HeatingDevice heatingDevice = (HeatingDevice) device;

                heatingService.setMode(heatingDevice, mode, this);
            }

        } else if (DEVICE_SET_WEEK_PROFILE.equals(action)) {
            if (!(device instanceof HeatingDevice)) return ERROR;
            heatingService.setWeekProfileFor((HeatingDevice) device, this);

        } else if (DEVICE_SET_WINDOW_OPEN_TEMPERATURE.equals(action)) {
            if (!(device instanceof WindowOpenTempDevice)) return SUCCESS;

            double temperature = intent.getDoubleExtra(DEVICE_TEMPERATURE, -1);
            heatingService.setWindowOpenTemp((WindowOpenTempDevice) device, temperature, this);

        } else if (DEVICE_SET_DESIRED_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                heatingService.setDesiredTemperature((DesiredTempDevice) device, temperature, this);
            }

        } else if (DEVICE_SET_COMFORT_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                heatingService.setComfortTemperature((ComfortTempDevice) device, temperature, this);
            }

        } else if (DEVICE_SET_ECO_TEMPERATURE.equals(action)) {
            double temperature = intent.getDoubleExtra(DEVICE_TEMPERATURE, -1);
            if (device instanceof DesiredTempDevice) {
                heatingService.setEcoTemperature((EcoTempDevice) device, temperature, this);
            }

        } else if (DEVICE_RESET_WEEK_PROFILE.equals(action)) {
            if (!(device instanceof HeatingDevice)) return ERROR;
            heatingService.resetWeekProfile((HeatingDevice) device);
        } else if (DEVICE_RENAME.equals(action)) {
            String newName = intent.getStringExtra(DEVICE_NEW_NAME);
            deviceService.renameDevice(device, newName, this);
            notificationService.rename(deviceName, newName, this);
            favoritesService.removeFavorite(getBaseContext(), deviceName);
            favoritesService.addFavorite(getBaseContext(), newName);

        } else if (DEVICE_DELETE.equals(action)) {
            deviceService.deleteDevice(device, this);

        } else if (DEVICE_MOVE_ROOM.equals(action)) {
            String newRoom = intent.getStringExtra(DEVICE_NEW_ROOM);
            deviceService.moveDevice(device, newRoom, this);

        } else if (DEVICE_SET_ALIAS.equals(action)) {
            String newAlias = intent.getStringExtra(DEVICE_NEW_ALIAS);
            deviceService.setAlias(device, newAlias, this);

        } else if (DEVICE_WIDGET_TOGGLE.equals(action)) {
            result = toggleIntent(device, connectionId);

        } else if (DEVICE_TIMER_MODIFY.equals(action)) {
            processTimerIntent(intent, true);

        } else if (DEVICE_TIMER_NEW.equals(action)) {
            processTimerIntent(intent, false);

        } else if (DEVICE_SET_SUB_STATE.equals(action)) {
            String name = intent.getStringExtra(STATE_NAME);
            String value = intent.getStringExtra(STATE_VALUE);

            genericDeviceService.setSubState(device, name, value, connectionId, this, true);

        } else if (DEVICE_SET_SUB_STATES.equals(action)) {
            @SuppressWarnings("unchecked")
            List<StateToSet> statesToSet = (List<StateToSet>) intent.getSerializableExtra(STATES);

            genericDeviceService.setSubStates(device, statesToSet, connectionId, this);

        } else if (GCM_ADD_SELF.equals(action)) {
            gcmSendDeviceService.addSelf((GCMSendDevice) device, this);

        } else if (GCM_REMOVE_ID.equals(action)) {
            String registrationId = intent.getStringExtra(GCM_REGISTRATION_ID);
            gcmSendDeviceService.removeRegistrationId((GCMSendDevice) device, registrationId, this);

        } else if (RESEND_LAST_FAILED_COMMAND.equals(action)) {

            Command lastFailedCommand = commandExecutionService.getLastFailedCommand();
            if (lastFailedCommand != null && "xmllist".equalsIgnoreCase(lastFailedCommand.getCommand())) {
                sendBroadcast(new Intent(Actions.DO_UPDATE)
                        .putExtra(DO_REFRESH, true));
            } else {
                commandExecutionService.resendLastFailedCommand(this);
            }
        }

        return result;
    }

    /**
     * Find out graph data for a given device and notify the result receiver with the read graph data
     *
     * @param intent         received intent
     * @param device         device to read the graph data
     * @param resultReceiver receiver to notify on result
     * @return success?
     */
    private STATE graphIntent(Intent intent, FhemDevice device, ResultReceiver resultReceiver) {
        SvgGraphDefinition graphDefinition = (SvgGraphDefinition) intent.getSerializableExtra(DEVICE_GRAPH_DEFINITION);
        DateTime startDate = (DateTime) intent.getSerializableExtra(START_DATE);
        DateTime endDate = (DateTime) intent.getSerializableExtra(END_DATE);
        Optional<String> connectionId = Optional.fromNullable(intent.getStringExtra(BundleExtraKeys.CONNECTION_ID));

        HashMap<GPlotSeries, List<GraphEntry>> graphData = graphService.getGraphData(device, connectionId, graphDefinition, startDate, endDate, this);
        sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, DEVICE_GRAPH_ENTRY_MAP, graphData);
        return STATE.DONE;
    }

    /**
     * Toggle a device and notify the result receiver
     *
     * @param device device to toggle
     * @param connectionId
     * @return success?
     */
    private STATE toggleIntent(FhemDevice device, Optional<String> connectionId) {
        if (device instanceof ToggleableDevice && ((ToggleableDevice) device).supportsToggle()) {
            toggleableService.toggleState((ToggleableDevice) device, connectionId, this);
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    /**
     * Set the state of a device and notify the result receiver
     *
     * @param intent       received intent
     * @param device       device to set the state on
     * @param connectionId
     * @return success ?
     */
    private STATE setStateIntent(Intent intent, FhemDevice device, Optional<String> connectionId) {
        String targetState = intent.getStringExtra(DEVICE_TARGET_STATE);
        int timesToSend = intent.getIntExtra(TIMES_TO_SEND, 1);

        for (int i = 0; i < timesToSend; i++) {
            genericDeviceService.setState(device, targetState, connectionId, this);
        }

        return STATE.SUCCESS;
    }

    /**
     * Dim a device and notify the result receiver
     *
     * @param intent received intent
     * @param device device to dim
     * @return success?
     */
    private STATE dimIntent(Intent intent, FhemDevice device) {
        float dimProgress = intent.getFloatExtra(DEVICE_DIM_PROGRESS, -1);
        if (device instanceof DimmableDevice) {
            dimmableDeviceService.dim((DimmableDevice) device, dimProgress, this);
            return STATE.SUCCESS;
        }
        return STATE.ERROR;
    }

    @SuppressWarnings("ConstantConditions")
    private STATE processTimerIntent(Intent intent, boolean isModify) {
        Bundle extras = intent.getExtras();

        assert extras != null;

        String targetDeviceName = extras.getString(TIMER_TARGET_DEVICE_NAME);
        String targetState = extras.getString(TIMER_TARGET_STATE);
        int hour = extras.getInt(TIMER_HOUR, 0);
        int minute = extras.getInt(TIMER_MINUTE, 0);
        int second = extras.getInt(TIMER_SECOND, 0);
        String repetition = extras.getString(TIMER_REPETITION);
        String type = extras.getString(TIMER_TYPE);
        String stateAppendix = extras.getString(TIMER_TARGET_STATE_APPENDIX);
        String timerName = extras.getString(DEVICE_NAME);
        boolean isActive = extras.getBoolean(TIMER_IS_ACTIVE);

        if (isModify) {
            atService.modify(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix, isActive, this);
        } else {
            atService.createNew(timerName, hour, minute, second, repetition, type, targetDeviceName, targetState, stateAppendix, isActive, this);
        }

        return SUCCESS;
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
