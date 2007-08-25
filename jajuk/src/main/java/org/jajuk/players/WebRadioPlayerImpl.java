/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision: 2523 $
 */
package org.jajuk.players;

import org.jajuk.base.WebRadio;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Jajuk web radio player implementation based on Mplayer
 */
public class WebRadioPlayerImpl extends AbstractMPlayerImpl {

	/** Current reader thread */
	volatile ReaderThread reader;

	/**
	 * Reader : read information from mplayer like position
	 */
	private class ReaderThread extends Thread {
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line = null;
				for (; (line = in.readLine()) != null;) {
					if (line.matches(".*ANS_TIME_POSITION.*")) {
						StringTokenizer st = new StringTokenizer(line, "=");
						st.nextToken();
					} else if (line.matches("Exiting.*End.*")) {
						bEOF = true;
						bOpening = false;
					}
					// Opening ?
					else if (line.matches(".*Starting playback.*")) {
						bOpening = false;
					}
				}
				// can reach this point at the end of file
				in.close();
				return;
			} catch (Exception e) {
				// A stop causes a steam close exception, so ignore it
				if (!e.getMessage().matches(".*Stream closed")) {
					Log.error(e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.players.IPlayerImpl#play(org.jajuk.base.File, float, long,
	 *      float)
	 */
	@SuppressWarnings("unchecked")
	public void play(WebRadio radio, float fVolume) throws Exception {
		this.fVolume = fVolume;
		this.bOpening = true;
		this.bEOF = false;
		// Start
		ProcessBuilder pb = new ProcessBuilder(buildCommand(radio.getUrl().toExternalForm()));
		Log.debug("Using this Mplayer command: " + pb.command());
		// Set all environment variables format: var1=xxx var2=yyy
		try {
			Map<String, String> env = pb.environment();
			StringTokenizer st = new StringTokenizer(ConfigurationManager
					.getProperty(CONF_ENV_VARIABLES), " ");
			while (st.hasMoreTokens()) {
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
				env.put(st2.nextToken(), st2.nextToken());
			}
			// If needed, set proxy settings in format:
			// http_proxy=http://username:password@proxy.example.org:8080
			if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
				String sLogin = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN).trim();
				String sHost = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME).trim();
				int port = ConfigurationManager.getInt(CONF_NETWORK_PROXY_PORT);
				// Non anonymous proxy
				if (!Util.isVoid(sLogin)) {
					String sPwd = Util.rot13(ConfigurationManager
							.getProperty(CONF_NETWORK_PROXY_PWD));
					String sProxyConf = "http://" + sLogin + ':' + sPwd + '@' + sHost + ':' + port;
					env.put("http_proxy", sProxyConf);
					Log.debug("Using these proxy settings: " + sProxyConf);
				}
				// Anonymous proxy
				else {
					String sProxyConf = "http://" + sHost + ':' + port;
					env.put("http_proxy", sProxyConf);
					Log.debug("Using these proxy settings: " + sProxyConf);
				}
			}
		} catch (Exception e) {
			Log.error(e);
		}
		proc = pb.start();
		reader = new ReaderThread();
		reader.start();
		// if opening, wait, 30 secs max
		int i = 0;
		while (bOpening && i < 30) {
			try {
				Thread.sleep(1000);
				i++;
			} catch (InterruptedException e) {
				Log.error(e);
			}
		}
		// If end of file already reached, it means that file cannot be read
		if (bEOF) {
			throw new JajukException(7);
		}
		// Get track length
		sendCommand("get_time_length");
		setVolume(fVolume);
	}

	

}