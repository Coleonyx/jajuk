/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 2920 $$
 */
package org.jajuk.ui.action;

import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

public class FileMoveAction extends ActionBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	FileMoveAction() {
		super(Messages.getString("FilesTreeView.5"), IconLoader.ICON_SAVE, true);
		setShortDescription(Messages.getString("FilesTreeView.7"));
	}

	public void perform(ActionEvent e) {
		JComponent source = (JComponent) e.getSource();
		final ArrayList<Item> alSelected = (ArrayList<Item>) source
        	.getClientProperty(DETAIL_NEW);
		final ArrayList<Item> moveItems = (ArrayList<Item>) source
    		.getClientProperty(DETAIL_OLD);
		final String moveAction = ((ArrayList<String>) source.getClientProperty(DETAIL_SELECTION)).get(0);
		
		new Thread(){
			public void run(){
				Item item = alSelected.get(0);
				Directory destDir;
				java.io.File dir;
				if (item instanceof Directory){
					dir = new java.io.File(((Directory) item).getAbsolutePath());
					destDir = (Directory) item; 
				}
				else{
					dir = ((File) item).getIO().getParentFile();
					destDir = ((File) item).getDirectory();
				}
				if (moveAction == "Cut"){
					Log.debug("Inside Cut");
					for (Item t : moveItems){
						if (t instanceof File){
							try{
								Util.copyToDir(((File) t).getIO(), dir);
								Util.deleteFile(((File) t).getIO());
								FileManager.getInstance().changeFileDirectory((File) t, destDir);
							} catch(Exception ioe) {
								Log.error(131, ioe);
								Messages.showErrorMessage(131);
							}
						}else{
							try{
								java.io.File src = new java.io.File(((Directory) t).getAbsolutePath());
								java.io.File dst = new java.io.File(dir.getAbsolutePath() + "/" + ((Directory) t).getName());
								Util.copyRecursively(src, dst);
								Util.deleteDir(src);
								DirectoryManager.getInstance().removeDirectory(((Directory) t).getID());
								registerDirectory(destDir);
							} catch(Exception ioe) {
								Log.error(131, ioe);
								Messages.showErrorMessage(131);
							}
						}
					}
				} else if (moveAction == "Copy"){
					Log.debug("Inside Copy");
					for (Item t : moveItems){
						if (t instanceof File){
							try{
								Util.copyToDir(((File) t).getIO(), dir);
							} catch(Exception ioe) {
								Log.error(131, ioe);
								Messages.showErrorMessage(131);
							}
						}else{
							try{
								Directory d = (Directory) t;
								java.io.File src = new java.io.File(((Directory) t).getAbsolutePath());
								java.io.File dst = new java.io.File(dir.getAbsolutePath() + "/" + d.getName());
								Util.copyRecursively(src, dst);
							} catch(Exception ioe) {
								Log.error(131, ioe);
								Messages.showErrorMessage(131);
							}
						}
					}
					registerDirectory(destDir);
				}
				ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
			}
		}.start();
		return;
	}
	
	public void registerDirectory(Directory d) {
		java.io.File dirList[] = d.getFio().listFiles(
				new JajukFileFilter(JajukFileFilter.DirectoryFilter
						.getInstance()));
		if (dirList != null && dirList.length != 0) {
			for (java.io.File f : dirList) {
				Directory dir = DirectoryManager.getInstance()
						.registerDirectory(f.getName(), d, d.getDevice());
				registerDirectory(dir);
			}
		} else {
			d.scan(true, null);
		}
	}
}
