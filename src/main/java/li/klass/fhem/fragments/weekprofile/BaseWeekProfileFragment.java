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

package li.klass.fhem.fragments.weekprofile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import li.klass.fhem.R;
import li.klass.fhem.adapter.weekprofile.BaseWeekProfileAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.util.StateToSet;
import li.klass.fhem.widget.NestedListView;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.HEATING_CONFIGURATION;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;

public abstract class BaseWeekProfileFragment<H extends BaseHeatingInterval> extends BaseFragment {

    private String deviceName;
    private HeatingConfiguration heatingConfiguration;
    private WeekProfile weekProfile;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWeekProfileFragment.class);

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        checkArgument(args.containsKey(DEVICE_NAME));
        checkArgument(args.containsKey(HEATING_CONFIGURATION));

        deviceName = args.getString(DEVICE_NAME);
        heatingConfiguration = (HeatingConfiguration) args.getSerializable(HEATING_CONFIGURATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        beforeCreateView();

        View view = inflater.inflate(R.layout.weekprofile, container, false);

        Button saveButton = (Button) view.findViewById(R.id.save);
        update(false);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View view) {
                ArrayList<StateToSet> commands = newArrayList(weekProfile.getStatesToSet(deviceName));
                getActivity().startService(new Intent(Actions.DEVICE_SET_SUB_STATES)
                        .setClass(getActivity(), DeviceIntentService.class)
                        .putExtra(DEVICE_NAME, deviceName)
                        .putExtra(BundleExtraKeys.STATES, commands)
                        .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode != ResultCodes.SUCCESS) return;
                                backToDevice();
                                update(true);
                            }
                        }));
            }
        });

        Button resetButton = (Button) view.findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(false);
            }
        });

        NestedListView nestedListView = (NestedListView) view.findViewById(R.id.weekprofile);
        BaseWeekProfileAdapter adapter = getAdapter();
        adapter.registerWeekProfileChangedListener(new BaseWeekProfileAdapter.WeekProfileChangedListener() {
            @Override
            public void onWeekProfileChanged(WeekProfile weekProfile) {
                LOGGER.info("onWeekProfileChanged() - {}", weekProfile.toString());
                updateChangeButtonsHolderVisibility(weekProfile);
            }
        });


        nestedListView.setAdapter(adapter);

        return view;
    }

    private void backToDevice() {
        DialogUtil.showAlertDialog(getActivity(), R.string.doneTitle, R.string.heatingConfigurationSaveNotification, new DialogUtil.AlertOnClickListener() {
            @Override
            public void onClick() {
                back();
            }
        });
    }

    @Override
    public void update(boolean doUpdate) {

        getActivity().startService(new Intent(Actions.GET_DEVICE_FOR_NAME)
                .setClass(getActivity(), RoomListIntentService.class)
                .putExtra(DO_REFRESH, doUpdate)
                .putExtra(DEVICE_NAME, deviceName)
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.SUCCESS && getView() != null) {
                            FhemDevice device = (FhemDevice) resultData.getSerializable(BundleExtraKeys.DEVICE);
                            if (device == null) {
                                return;
                            }

                            weekProfile = new WeekProfile<>(heatingConfiguration);
                            weekProfile.fillWith(device.getXmlListDevice());

                            updateChangeButtonsHolderVisibility(weekProfile);
                        }
                    }
                }));
    }

    @SuppressWarnings("unchecked")
    private void updateChangeButtonsHolderVisibility(WeekProfile weekProfile) {
        View view = getView();
        if (view == null) return;

        View holder = view.findViewById(R.id.changeValueButtonHolder);
        if (holder == null) return;

        updateAdapterWith(weekProfile);

        if (weekProfile.getChangedDayProfiles().size() > 0) {
            holder.setVisibility(View.VISIBLE);
        } else {
            holder.setVisibility(View.GONE);
        }
    }

    protected abstract void updateAdapterWith(WeekProfile<H, ?, ? extends FhemDevice> weekProfile);

    protected abstract BaseWeekProfileAdapter getAdapter();

    protected void beforeCreateView() {
    }

    @Override
    protected boolean mayUpdateFromBroadcast() {
        return false;
    }
}
