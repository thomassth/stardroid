package io.github.marcocipriani01.telescopetouch.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

public class ControlPanelFragment extends Fragment
        implements INDIServerConnectionListener, SearchView.OnQueryTextListener, View.OnClickListener, SearchView.OnCloseListener {

    private static final String KEY_VIEWPAGER_STATE = "DevicesViewPagerState";
    private static Bundle viewPagerBundle;
    private final ArrayList<INDIDevice> devices = new ArrayList<>();
    private final HashMap<Integer, DeviceControlFragment> fragmentsMap = new HashMap<>();
    private ConnectionManager connectionManager;
    private DevicesFragmentAdapter fragmentAdapter;
    private LinearLayout controlLayout;
    private TextView noDevicesText;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private Context context;
    private MenuItem searchMenu;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_control_panel, container, false);
        controlLayout = rootView.findViewById(R.id.indi_control_layout);
        noDevicesText = rootView.findViewById(R.id.no_devices_label);
        viewPager = rootView.findViewById(R.id.indi_control_pager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout = rootView.findViewById(R.id.indi_control_tabs);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(devices.get(position).getName()));
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up INDI connection
        connectionManager = TelescopeTouchApp.getConnectionManager();
        connectionManager.addListener(this);
        // Enumerate existing properties
        if (!connectionManager.isConnected()) {
            noDevices();
        } else {
            List<INDIDevice> list = connectionManager.getConnection().getDevicesAsList();
            if (list.isEmpty()) {
                noDevices();
            } else {
                for (INDIDevice device : list) {
                    if (!devices.contains(device)) newDevice(device);
                }
                viewPager.post(() -> {
                    fragmentAdapter.notifyDataSetChanged();
                    if (viewPagerBundle != null) {
                        int position = viewPagerBundle.getInt(KEY_VIEWPAGER_STATE);
                        viewPager.setCurrentItem(position, false);
                        tabLayout.selectTab(tabLayout.getTabAt(position));
                    }
                });
                devices();
            }
        }
        fragmentAdapter = new DevicesFragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
        tabLayoutMediator.attach();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewPagerBundle = new Bundle();
        viewPagerBundle.putInt(KEY_VIEWPAGER_STATE, viewPager.getCurrentItem());
    }

    @Override
    public void onStop() {
        super.onStop();
        tabLayoutMediator.detach();
        viewPager.setAdapter(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        noDevices();
        connectionManager.removeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        searchMenu = menu.add(R.string.search);
        searchMenu.setIcon(R.drawable.search);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = new SearchView(context);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnSearchClickListener(this);
        searchView.setOnCloseListener(this);
        searchView.setOnQueryTextListener(this);
        searchMenu.setActionView(searchView);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        DeviceControlFragment fragment = fragmentsMap.get(viewPager.getCurrentItem());
        if (fragment != null) fragment.findPref(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private void noDevices() {
        devices.clear();
        noDevicesText.post(() -> noDevicesText.setVisibility(View.VISIBLE));
        controlLayout.post(() -> controlLayout.setVisibility(View.GONE));
        viewPager.post(() -> fragmentAdapter.notifyDataSetChanged());
        new Handler(Looper.getMainLooper()).post(() -> searchMenu.setVisible(false));
    }

    private void devices() {
        noDevicesText.post(() -> noDevicesText.setVisibility(View.GONE));
        controlLayout.post(() -> controlLayout.setVisibility(View.VISIBLE));
        new Handler(Looper.getMainLooper()).post(() -> searchMenu.setVisible(true));
    }

    @Override
    public void connectionLost(INDIServerConnection connection) {
        noDevices();
        // Move to the connection tab
        TelescopeTouchApp.goToConnectionTab();
    }

    @Override
    public void newDevice(INDIServerConnection connection, INDIDevice device) {
        Log.i("ControlPanelFragment", "New device: " + device.getName());
        viewPager.post(() -> fragmentAdapter.notifyItemInserted(newDevice(device)));
        devices();
    }

    private int newDevice(INDIDevice device) {
        devices.add(device);
        return devices.indexOf(device);
    }

    @Override
    public void removeDevice(INDIServerConnection connection, INDIDevice device) {
        Log.d("ControlPanelFragment", "Device removed: " + device.getName());
        int index = devices.indexOf(device);
        if (index != -1) {
            devices.remove(device);
            if (devices.isEmpty()) {
                noDevices();
            } else {
                viewPager.post(() -> fragmentAdapter.notifyItemRemoved(index));
            }
        }
    }

    @Override
    public void newMessage(INDIServerConnection connection, Date timestamp, String message) {

    }

    @Override
    public void onClick(View v) {
        tabLayout.setVisibility(View.GONE);
        viewPager.setUserInputEnabled(false);
    }

    @Override
    public boolean onClose() {
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setUserInputEnabled(true);
        DeviceControlFragment fragment = fragmentsMap.get(viewPager.getCurrentItem());
        if (fragment != null) fragment.stopSearch();
        return false;
    }

    private class DevicesFragmentAdapter extends FragmentStateAdapter {

        public DevicesFragmentAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            DeviceControlFragment fragment = new DeviceControlFragment();
            fragment.setDevice(devices.get(position));
            fragmentsMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }
    }
}