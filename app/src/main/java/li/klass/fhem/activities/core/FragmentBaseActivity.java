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

package li.klass.fhem.activities.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.RepairedDrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.base.Optional;

import java.util.Timer;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.activities.DuplicateInstallActivity;
import li.klass.fhem.billing.BillingService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.license.LicenseManager;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.constants.Actions.BACK;
import static li.klass.fhem.constants.Actions.CONNECTIONS_CHANGED;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.DO_UPDATE;
import static li.klass.fhem.constants.Actions.REDRAW;
import static li.klass.fhem.constants.Actions.RELOAD;
import static li.klass.fhem.constants.Actions.SHOW_ALERT;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_TOAST;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.HAS_FAVORITES;
import static li.klass.fhem.constants.PreferenceKeys.AUTO_UPDATE_TIME_IN_ACTIVITY;
import static li.klass.fhem.constants.PreferenceKeys.STARTUP_VIEW;
import static li.klass.fhem.constants.PreferenceKeys.UPDATE_ON_APPLICATION_START;
import static li.klass.fhem.fragments.FragmentType.ALL_DEVICES;
import static li.klass.fhem.fragments.FragmentType.FAVORITES;
import static li.klass.fhem.fragments.FragmentType.getFragmentFor;

public abstract class FragmentBaseActivity extends SherlockFragmentActivity implements Updateable {

    public static final String TAG = FragmentBaseActivity.class.getName();
    public static final String NAVIGATION_TAG = "NAVIGATION_TAG";
    public static final String CONTENT_TAG = "CONTENT_TAG";

    private ApplicationProperties applicationProperties = ApplicationProperties.INSTANCE;
    private Receiver broadcastReceiver;

    protected Menu optionsMenu;

    private Timer timer;

    private RepairedDrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private boolean saveInstanceStateCalled;
    private AvailableConnectionDataAdapter availableConnectionDataAdapter;

    protected FragmentBaseActivity() {
    }

    private class Receiver extends BroadcastReceiver {

        private final IntentFilter intentFilter;

        private Receiver() {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Actions.SHOW_FRAGMENT);
            intentFilter.addAction(Actions.DISMISS_UPDATING_DIALOG);
            intentFilter.addAction(Actions.DO_UPDATE);
            intentFilter.addAction(SHOW_EXECUTING_DIALOG);
            intentFilter.addAction(DISMISS_EXECUTING_DIALOG);
            intentFilter.addAction(SHOW_TOAST);
            intentFilter.addAction(SHOW_ALERT);
            intentFilter.addAction(DO_UPDATE);
            intentFilter.addAction(BACK);
            intentFilter.addAction(RELOAD);
            intentFilter.addAction(CONNECTIONS_CHANGED);
            intentFilter.addAction(REDRAW);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (! saveInstanceStateCalled) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String action = intent.getAction();
                            if (action == null) return;

                            if (Actions.SHOW_FRAGMENT.equals(action)) {
                                Bundle bundle = intent.getExtras();
                                if (bundle == null)
                                    throw new IllegalArgumentException("need a content fragment");
                                FragmentType fragmentType;
                                if (bundle.containsKey(FRAGMENT)) {
                                    fragmentType = (FragmentType) bundle.getSerializable(FRAGMENT);
                                } else {
                                    String fragmentName = bundle.getString(FRAGMENT_NAME);
                                    fragmentType = getFragmentFor(fragmentName);
                                }
                                switchToFragment(fragmentType, intent.getExtras());
                            } else if (action.equals(Actions.DISMISS_UPDATING_DIALOG)) {
                                setShowRefreshProgressIcon(false);
                            } else if (intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false) && action.equals(Actions.DO_UPDATE)) {
                                setShowRefreshProgressIcon(true);
                            } else if (action.equals(SHOW_EXECUTING_DIALOG)) {
                                setShowRefreshProgressIcon(true);
                            } else if (action.equals(DISMISS_EXECUTING_DIALOG)) {
                                setShowRefreshProgressIcon(false);
                            } else if (action.equals(SHOW_TOAST)) {
                                String content = intent.getStringExtra(BundleExtraKeys.CONTENT);
                                if (content == null) {
                                    content = getString(intent.getIntExtra(BundleExtraKeys.STRING_ID, 0));
                                }
                                Toast.makeText(FragmentBaseActivity.this, content, Toast.LENGTH_SHORT).show();
                            } else if (action.equals(SHOW_ALERT)) {
                                DialogUtil.showAlertDialog(FragmentBaseActivity.this,
                                        intent.getIntExtra(BundleExtraKeys.ALERT_TITLE_ID, R.string.blank),
                                        intent.getIntExtra(BundleExtraKeys.ALERT_CONTENT_ID, R.string.blank));
                            } else if (action.equals(BACK)) {
                                onBackPressed();
                            } else if (CONNECTIONS_CHANGED.equals(action)) {
                                if (availableConnectionDataAdapter != null) {
                                    availableConnectionDataAdapter.doLoad();
                                }
                            } else if (REDRAW.equals(action)) {
                                redrawContent();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "exception occurred while receiving broadcast", e);
                        }
                    }
                });
            }
        }

        public IntentFilter getIntentFilter() {
            return intentFilter;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            Log.e(TAG, "error while creating activity", e);
        }

        if (AndFHEMApplication.INSTANCE.isAndFHEMAlreadyInstalled()) {
            startActivity(new Intent(this, DuplicateInstallActivity.class));
            return;
        }

        saveInstanceStateCalled = false;

        setContentView(R.layout.main_view);
        if (findViewById(R.id.tabletIndicator) != null) {
            AndFHEMApplication.INSTANCE.setIsTablet(true);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        handleConnectionSpinner(actionBar);

        actionBar.setDisplayHomeAsUpEnabled(true);

        broadcastReceiver = new Receiver();
        registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());

        initDrawerLayout();

        BillingService.INSTANCE.start(new BillingService.SetupFinishedListener() {
            @Override
            public void onSetupFinished() {
                Log.i(TAG, "Billing initialized, creating initial fragment");
                if (savedInstanceState == null && ! saveInstanceStateCalled) {
                    handleStartupFragment();
                }
            }
        });
    }

    private void handleStartupFragment() {
        String startupView = ApplicationProperties.INSTANCE.getStringSharedPreference(STARTUP_VIEW,
                FragmentType.FAVORITES.name());
        FragmentType preferencesStartupFragment = FragmentType.forEnumName(startupView);
        Log.d(TAG, "startup view is " + preferencesStartupFragment);

        Bundle startupBundle = new Bundle();

        FragmentType fragmentType = FragmentType.ALL_DEVICES;
        if (preferencesStartupFragment != null) {
            if (preferencesStartupFragment == FAVORITES) {
                Log.i(TAG, "startup with " + FAVORITES);
                Intent intent = new Intent(Actions.FAVORITES_PRESENT);
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);

                        if (resultCode != ResultCodes.SUCCESS || saveInstanceStateCalled) {
                            Log.e(TAG, "favorites response was " + resultData + " (" + resultCode + ")");
                        } else {
                            handleHasFavoritesResponse(resultCode, resultData);
                        }
                    }
                });
                startService(intent);
                fragmentType = null;
            } else {
                fragmentType = preferencesStartupFragment;
            }
        }

        if (fragmentType != null) {
            Log.i(TAG, "startup with " + fragmentType + " (extras: " + startupBundle + ")");
            switchToFragment(fragmentType, startupBundle);
        }
    }

    private void handleConnectionSpinner(ActionBar actionBar) {
        availableConnectionDataAdapter = new AvailableConnectionDataAdapter(this, actionBar);
        actionBar.setListNavigationCallbacks(availableConnectionDataAdapter, availableConnectionDataAdapter);
        availableConnectionDataAdapter.doLoad();
    }

    private void handleHasFavoritesResponse(int resultCode, Bundle resultData) {
        FragmentType toSwitchTo = ALL_DEVICES;
        if (resultCode == ResultCodes.SUCCESS && resultData.containsKey(HAS_FAVORITES)) {
            boolean hasFavorites = resultData.getBoolean(HAS_FAVORITES, false);
            if (hasFavorites && ! saveInstanceStateCalled) {
                toSwitchTo = FAVORITES;
                Log.d(TAG, "found favorites, switching");
            } else {
                Log.d(TAG, "no favorites found");
            }
        }
        switchToFragment(toSwitchTo, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveInstanceStateCalled = true;
        super.onSaveInstanceState(outState);
    }

    private void initDrawerLayout() {
        mDrawerLayout = (RepairedDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new NavigationDrawerAdapter(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentType fragmentType = (FragmentType) view.getTag();

                if (fragmentType == FragmentType.TIMER_OVERVIEW && Build.VERSION.SDK_INT < 11) {
                    String text = String.format(getString(R.string.feature_requires_android_version), 3);
                    DialogUtil.showAlertDialog(FragmentBaseActivity.this, R.string.android_version, text);
                    return;
                }

                switchToFragment(fragmentType, new Bundle());
                mDrawerLayout.closeDrawers();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawerOpen, R.string.drawerClose) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (actionBarDrawerToggle != null) actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
        updateNavigationVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "resuming " + getClass().getName());

        saveInstanceStateCalled = false;

        if (broadcastReceiver != null) {
            registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter());
        }

        if (availableConnectionDataAdapter != null) {
            availableConnectionDataAdapter.doLoad();
        }

        handleTimerUpdates();

        updateNavigationVisibility();

        handleOpenIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void handleOpenIntent() {
        final Optional<FragmentType> intentFragment = getFragmentTypeFromStartupIntent();
        if (intentFragment.isPresent()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchToFragment(intentFragment.get(), getIntent().getExtras());
                    setIntent(null);
                }
            }, 500);
        }
    }

    private Optional<FragmentType> getFragmentTypeFromStartupIntent() {
        Optional<FragmentType> toReturn = Optional.absent();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(FRAGMENT)) {
                toReturn = Optional.of((FragmentType) intent.getSerializableExtra(BundleExtraKeys.FRAGMENT));
            } else if (intent.hasExtra(FRAGMENT_NAME)) {
                String fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT_NAME);
                toReturn = Optional.of(FragmentType.valueOf(fragmentName));
            }
        }
        return toReturn;
    }

    private void handleTimerUpdates() {
        // We post this delayed, as otherwise we will block the application startup (causing
        // ugly ANRs).
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                boolean updateOnApplicationStart = applicationProperties.getBooleanSharedPreference(UPDATE_ON_APPLICATION_START, false);
                int updateInterval = Integer.valueOf(applicationProperties.getStringSharedPreference(AUTO_UPDATE_TIME_IN_ACTIVITY, "-1"));

                if (timer == null && (updateOnApplicationStart || updateInterval != -1)) {
                    timer = new Timer();
                }

                if (updateInterval != -1) {
                    int initialDelay = updateOnApplicationStart ? updateInterval : 0;
                    timer.scheduleAtFixedRate(new UpdateTimerTask(FragmentBaseActivity.this), initialDelay, updateInterval);
                    Log.i(TAG, "scheduling update every " + (updateInterval / 1000 / 60) + "min");
                }

                if (updateOnApplicationStart) {
                    Log.i(TAG, "update on application start started");
                    timer.schedule(new UpdateTimerTask(FragmentBaseActivity.this), 0);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        RoomListService.INSTANCE.storeDeviceListMap();

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "receiver was not registered, ignore ...");
        }

        setShowRefreshProgressIcon(false);
    }

    @Override
    public void onBackPressed() {
        // We pop fragments as long as:
        // - there are more fragments within the back stack
        // - the popped fragment type is not equals to the current fragment type

        boolean doFinish = false;

        BaseFragment contentFragment = getContentFragment();
        if (contentFragment == null) {
            finish();
            return;
        }
        FragmentType contentFragmentType = getFragmentFor(contentFragment.getClass());
        while (true) {
            if (! getSupportFragmentManager().popBackStackImmediate()) {
                doFinish = true;
                break;
            }

            BaseFragment current = getContentFragment();
            if (current == null) {
                doFinish = true;
                break;
            }

            FragmentType currentFragmentType = getFragmentFor(current.getClass());
            if (currentFragmentType != contentFragmentType) {
                Log.i(TAG, "backpress => switched to " + currentFragmentType);
                break;
            }
        }

        if (doFinish) {
            finish();
            BillingService.INSTANCE.stop();
            Log.i(TAG, "cannot find more fragments on backstack => exiting");
        } else {
            updateNavigationVisibility();
        }
    }

    private void redrawContent() {

        BaseFragment contentFragment = getContentFragment();
        if (contentFragment != null) contentFragment.invalidate();

        BaseFragment navigationFragment = getNavigationFragment();
        if (navigationFragment != null) navigationFragment.invalidate();
    }

    private BaseFragment getContentFragment() {
        return (BaseFragment) getSupportFragmentManager().findFragmentByTag(CONTENT_TAG);
    }

    private void switchToFragment(FragmentType fragmentType, Bundle data) {
        if (! saveInstanceStateCalled) {
            if (data == null) data = new Bundle();

            Log.i(TAG, "switch to " + fragmentType.name() + " with " + data.toString());
            if (fragmentType.isTopLevelFragment()) {
                clearBackStack();
            }

            BaseFragment contentFragment = createContentFragment(fragmentType, data);
            BaseFragment navigationFragment = createNavigationFragment(fragmentType, data);

            setContent(navigationFragment, contentFragment);
        }
    }

    private BaseFragment createContentFragment(FragmentType fragmentType, Bundle data) {
        if (fragmentType == null) {
            sendBroadcast(new Intent(Actions.RELOAD));
            return null;
        }
        try {
            Class<? extends BaseFragment> fragmentClass = fragmentType.getContentClass();
            return createFragmentForClass(data, fragmentClass);
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate fragment", e);
            return null;
        }
    }

    private BaseFragment createNavigationFragment(FragmentType fragmentType, Bundle data) {
        View navigationView = findViewById(R.id.navigation);
        if (navigationView == null) {
            return null;
        }

        try {
            Class<? extends BaseFragment> navigationClass = fragmentType.getNavigationClass();
            if (navigationClass == null) {
                navigationView.setVisibility(View.GONE);
                return null;
            }
            navigationView.setVisibility(View.VISIBLE);
            BaseFragment fragment = createFragmentForClass(data, navigationClass);
            fragment.setNavigation(true);
            return fragment;
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate fragment", e);
            return null;
        }
    }

    private void setContent(BaseFragment navigationFragment, BaseFragment contentFragment) {
        if (saveInstanceStateCalled) return;

        boolean hasNavigation = hasNavigation(navigationFragment, contentFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null) {
            Log.e(TAG, "fragment manager is null in #setContent");
            return;
        }

        FragmentTransaction transaction = fragmentManager
                .beginTransaction()
                .addToBackStack(contentFragment.getClass().getName())
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        if (hasNavigation) {
            transaction.replace(R.id.navigation, navigationFragment, NAVIGATION_TAG);
        }
        transaction.replace(R.id.content, contentFragment, CONTENT_TAG);

        transaction.commit();

        updateNavigationVisibility(navigationFragment, contentFragment);
    }

    private BaseFragment getNavigationFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager == null) return null;
        return (BaseFragment) supportFragmentManager
                .findFragmentByTag(NAVIGATION_TAG);
    }

    private boolean updateNavigationVisibility() {
        BaseFragment navigationFragment = getNavigationFragment();
        BaseFragment contentFragment = getContentFragment();

        return updateNavigationVisibility(navigationFragment, contentFragment);
    }

    private boolean hasNavigation(BaseFragment navigationFragment, BaseFragment contentFragment) {
        FragmentType fragmentType = getFragmentFor(contentFragment.getClass());
        View navigationView = findViewById(R.id.navigation);
        return navigationView != null && !(navigationFragment == null || fragmentType.getNavigationClass() == null);
    }

    private boolean updateNavigationVisibility(BaseFragment navigationFragment, BaseFragment contentFragment) {
        if (contentFragment == null) return false;

        FragmentType fragmentType = getFragmentFor(contentFragment.getClass());

        boolean hasNavigation = hasNavigation(navigationFragment, contentFragment);
        View navigationView = findViewById(R.id.navigation);
        if (navigationView != null) {
            if (navigationFragment == null || fragmentType.getNavigationClass() == null) {
                navigationView.setVisibility(View.GONE);
            } else {
                navigationView.setVisibility(View.VISIBLE);
            }
        }
        return hasNavigation;
    }

    private BaseFragment createFragmentForClass(Bundle data, Class<? extends BaseFragment> fragmentClass) throws Exception {
        if (fragmentClass == null) return null;

        BaseFragment fragment = fragmentClass.newInstance();
        fragment.setArguments(data);
        return fragment;
    }

    private void clearBackStack() {
        int entryCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < entryCount; i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, com.actionbarsherlock.view.Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        if (LicenseManager.INSTANCE.isPremiumApk()) {
            menu.removeItem(R.id.menu_premium);
        }
        this.optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void setShowRefreshProgressIcon(boolean show) {
        if (optionsMenu == null) return;
        optionsMenu.findItem(R.id.menu_refresh).setVisible(!show);
        optionsMenu.findItem(R.id.menu_refresh_progress).setVisible(show);
    }
}