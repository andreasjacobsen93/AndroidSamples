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
package org.exobel.routerkeygen;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

import org.exobel.routerkeygen.algorithms.WiFiNetwork;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.widget.Toast;

public class WifiScanReceiver extends BroadcastReceiver {
	final private OnScanListener[] scanListeners;
	final private WifiManager wifi;
	private KeygenMatcherTask task;

	public interface OnScanListener {

		public void onScanFinished(WiFiNetwork[] networks);
	}

	public WifiScanReceiver(WifiManager wifi, OnScanListener... scanListener) {
		this.scanListeners = scanListener;
		this.wifi = wifi;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onReceive(Context context, Intent intent) {
		if (intent == null
				|| !intent.getAction().equals(
						WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
			return;
		if (scanListeners == null)
			return;
		if (wifi == null)
			return;
		final List<ScanResult> results = wifi.getScanResults();
		/*
		 * He have had reports of this returning null instead of empty
		 */
		if (results == null)
			return;

		try {
			// Single scan
			context.unregisterReceiver(this);
		} catch (Exception e) {
		}
		if (task == null || task.getStatus() == Status.FINISHED) {
			task = new KeygenMatcherTask(results, context);
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
				task.execute();
			} else {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}

	private class KeygenMatcherTask extends
			AsyncTask<Void, Void, WiFiNetwork[]> {
		private final List<ScanResult> results;
		private final Context context;
		private boolean misbuiltAPK = false;

		public KeygenMatcherTask(List<ScanResult> results, Context context) {
			this.results = results;
			this.context = context;
		}

		@Override
		protected void onPostExecute(WiFiNetwork[] networks) {
			if (misbuiltAPK)
				Toast.makeText(context, R.string.err_misbuilt_apk,
						Toast.LENGTH_SHORT).show();
			for (OnScanListener scanListener : scanListeners)
				scanListener.onScanFinished(networks);
		}

		@Override
		protected WiFiNetwork[] doInBackground(Void... params) {

			final Set<WiFiNetwork> set = new TreeSet<WiFiNetwork>();
			for (int i = 0; i < results.size() - 1; ++i)
				for (int j = i + 1; j < results.size(); ++j)
					if (results.get(i).SSID.equals(results.get(j).SSID))
						results.remove(j--);
			for (ScanResult result : results) {
				try {
					ZipInputStream magicInfo = new ZipInputStream(context
							.getResources().openRawResource(R.raw.magic_info));
					set.add(new WiFiNetwork(result, magicInfo));
					magicInfo.close();
				} catch (LinkageError e) {
					misbuiltAPK = true;
				} catch (NotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			final WiFiNetwork[] networks = new WiFiNetwork[set.size()];
			final Iterator<WiFiNetwork> it = set.iterator();
			int i = 0;
			while (it.hasNext())
				networks[i++] = it.next();
			return networks;
		}
	}
}
