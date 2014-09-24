/*
 * Copyright 2012 Rui Araújo, Luís Fonseca
 *
 * This file is part of Router Keygen.
 *
 * Router Keygen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Router Keygen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Router Keygen.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.exobel.routerkeygen.ui;

import org.exobel.routerkeygen.R;
import org.exobel.routerkeygen.WifiScanReceiver.OnScanListener;
import org.exobel.routerkeygen.algorithms.WiFiNetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

@SuppressWarnings("deprecation")
public class NetworksListFragment extends SherlockFragment implements
		OnScanListener, OnItemClickListener, MessagePublisher {

	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private static final String NETWORKS_FOUND = "network_found";

	private OnItemSelectionListener mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private ListView listview;
	private WifiListAdapter wifiListAdapter;
	private View noNetworksMessage;

	private WiFiNetwork[] networksFound;

	public interface OnItemSelectionListener {

		public void onItemSelected(WiFiNetwork id);

		public void onItemSelected(String mac);
	}

	private static OnItemSelectionListener sDummyCallbacks = new OnItemSelectionListener() {
		public void onItemSelected(WiFiNetwork id) {
		}

		public void onItemSelected(String mac) {
		}
	};

	public NetworksListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		RelativeLayout root = (RelativeLayout) inflater.inflate(
				R.layout.fragment_networks_list, container, false);
		listview = (ListView) root.findViewById(R.id.networks_list);
		wifiListAdapter = new WifiListAdapter(getActivity());
		listview.setAdapter(wifiListAdapter);
		noNetworksMessage = root.findViewById(R.id.message_group);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(NETWORKS_FOUND)) {
				Parcelable[] storedNetworksFound = savedInstanceState
						.getParcelableArray(NETWORKS_FOUND);
				networksFound = new WiFiNetwork[storedNetworksFound.length];
				for (int i = 0; i < storedNetworksFound.length; ++i)
					networksFound[i] = (WiFiNetwork) storedNetworksFound[i];
				onScanFinished(networksFound);
			}
			if (savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
				setActivatedPosition(savedInstanceState
						.getInt(STATE_ACTIVATED_POSITION));
			}
		}
		registerForContextMenu(listview);
		listview.setOnItemClickListener(this);
		return root;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof OnItemSelectionListener)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (OnItemSelectionListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (networksFound != null)
			outState.putParcelableArray(NETWORKS_FOUND, networksFound);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		listview.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
				: ListView.CHOICE_MODE_NONE);
	}

	public void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			listview.setItemChecked(mActivatedPosition, false);
		} else {
			listview.setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	private static final String MENU_VALUE = "menu_value";

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		if (networksFound == null || wifiListAdapter.getCount() <= info.position)
			return;
		final WiFiNetwork wiFiNetwork = wifiListAdapter.getItem(info.position).wifiNetwork;
		if (wiFiNetwork == null) // the list is unstable and it can happen
			return;

		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.networks_context_menu, menu);
		// We are copying the values right away as the networks list is
		// unstable.
		((MenuItem) menu.findItem(R.id.copy_ssid)).setIntent(new Intent()
				.putExtra(MENU_VALUE, wiFiNetwork.getSsidName()));
		((MenuItem) menu.findItem(R.id.copy_mac)).setIntent(new Intent()
				.putExtra(MENU_VALUE, wiFiNetwork.getMacAddress()));
		((MenuItem) menu.findItem(R.id.use_mac)).setIntent(new Intent()
				.putExtra(MENU_VALUE, wiFiNetwork.getMacAddress()));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String value = item.getIntent().getStringExtra(MENU_VALUE);
		switch (item.getItemId()) {
		case R.id.copy_ssid: {
			ClipboardManager clipboard = (ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(value);
			Toast.makeText(getActivity(),
					getString(R.string.msg_copied, value), Toast.LENGTH_SHORT)
					.show();
			return true;
		}
		case R.id.copy_mac: {
			ClipboardManager clipboard = (ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(value);
			Toast.makeText(getActivity(),
					getString(R.string.msg_copied, value), Toast.LENGTH_SHORT)
					.show();
			return true;
		}
		case R.id.use_mac:
			mCallbacks.onItemSelected(value);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void setMessage(int message) {
		noNetworksMessage.findViewById(R.id.loading_spinner).setVisibility(
				View.GONE);
		listview.setVisibility(View.GONE);
		TextView messageView = (TextView) noNetworksMessage
				.findViewById(R.id.message);
		messageView.setVisibility(View.VISIBLE);
		messageView.setText(message);
		noNetworksMessage.setVisibility(View.VISIBLE);
	}

	public void onScanFinished(WiFiNetwork[] networks) {
		networksFound = networks;
		if (getActivity() == null)
			return;
		if (networks.length > 0) {
			noNetworksMessage.setVisibility(View.GONE);
			wifiListAdapter.updateNetworks(networks);
			listview.setVisibility(View.VISIBLE);
		} else {
			noNetworksMessage.findViewById(R.id.loading_spinner).setVisibility(
					View.GONE);
			listview.setVisibility(View.GONE);
			listview.setVisibility(View.GONE);
			TextView messageView = (TextView) noNetworksMessage
					.findViewById(R.id.message);
			messageView.setVisibility(View.VISIBLE);
			messageView.setText(R.string.msg_nowifidetected);
			noNetworksMessage.setVisibility(View.VISIBLE);
		}
	}

	public void onItemClick(AdapterView<?> list, View view, int position,
			long id) {
		if (networksFound != null && wifiListAdapter.getCount() > position) {
			final WiFiNetwork wifiNetwork = wifiListAdapter.getItem(position).wifiNetwork;
			if (wifiNetwork != null) // the list is unstable and it can happen
				mCallbacks.onItemSelected(wifiNetwork);
		}
	}

}
