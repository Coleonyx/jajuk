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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.helpers.ItemMoveManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

public class PasteAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  PasteAction() {
    super(Messages.getString("ActionMove.0"), IconLoader.ICON_PASTE, "ctrl V", true, false);
    setShortDescription(Messages.getString("ActionMove.0"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    final List<Item> alSelected = (List<Item>) source.getClientProperty(DETAIL_SELECTION);
    final List<Item> moveItems = ItemMoveManager.getInstance().getAll();
    final ItemMoveManager.MoveActions moveAction = ItemMoveManager.getInstance().getAction();

    final List<File> alFiles = new ArrayList<File>(alSelected.size());
    final List<Directory> alDirs = new ArrayList<Directory>(alSelected.size());

    new Thread() {
      @Override
      public void run() {
        UtilGUI.waiting();
        for (Item item : moveItems) {
          if (item instanceof File) {
            alFiles.add((File) item);
          } else if (item instanceof Track) {
            alFiles.addAll(((Track) item).getFiles());
          } else if (item instanceof Album || item instanceof Author || item instanceof Style) {
            for (Track atrack : TrackManager.getInstance().getAssociatedTracks(item)) {
              alFiles.addAll(atrack.getFiles());
            }
          } else if (item instanceof Directory) {
            alDirs.add((Directory) item);
          }
        }

        Item item = alSelected.get(0);
        Directory destDir;
        java.io.File dir;
        if (item instanceof Directory) {
          dir = new java.io.File(((Directory) item).getAbsolutePath());
          destDir = (Directory) item;
        } else if (item instanceof Device) {
          dir = new java.io.File(((Device) item).getRootDirectory().getAbsolutePath());
          destDir = ((Device) item).getRootDirectory();
        } else {
          dir = ((File) item).getDirectory().getFio();
          destDir = ((File) item).getDirectory();
        }

        boolean overwriteAll = false;

        if (moveAction == ItemMoveManager.MoveActions.CUT) {
          for (File f : alFiles) {
            if (!overwriteAll) {
              java.io.File newFile = new java.io.File(dir.getAbsolutePath() + "/" + f.getName());
              if (newFile.exists()) {
                int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
                    + " : \n\n" + f.getName(), Messages.YES_NO_ALL_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                if (iResu != JOptionPane.YES_OPTION) {
                  UtilGUI.stopWaiting();
                  return;
                }
                if (iResu == Messages.ALL_OPTION) {
                  overwriteAll = true;
                }
              }
            }
            try {
              UtilSystem.copyToDir(f.getIO(), dir);
              UtilSystem.deleteFile(f.getIO());
              FileManager.getInstance().changeFileDirectory(f, destDir);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
            }
          }
          for (Directory d : alDirs) {
            try {
              java.io.File src = new java.io.File(d.getAbsolutePath());
              java.io.File dst = new java.io.File(dir.getAbsolutePath() + "/" + d.getName());
              UtilSystem.copyRecursively(src, dst);
              UtilSystem.deleteDir(src);
              DirectoryManager.getInstance().removeDirectory(d.getID());
              destDir.refresh(false,null);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
            }
          }
          try {
            destDir.refresh(false,null);
          } catch (JajukException e) {
            Log.error(e);
            Messages.showErrorMessage(e.getCode());
          }
        } else if (moveAction == ItemMoveManager.MoveActions.COPY) {
          Log.debug("Inside Copy");
          for (File f : alFiles) {
            if (!overwriteAll) {
              java.io.File newFile = new java.io.File(dir.getAbsolutePath() + "/" + f.getName());
              if (newFile.exists()) {
                int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
                    + " : \n\n" + f.getName(), Messages.YES_NO_ALL_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                if (iResu != JOptionPane.YES_OPTION) {
                  UtilGUI.stopWaiting();
                  return;
                }
                if (iResu == Messages.ALL_OPTION) {
                  overwriteAll = true;
                }
              }
            }
            try {
              UtilSystem.copyToDir(f.getIO(), dir);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
            }
          }
          for (Directory d : alDirs) {
            try {
              java.io.File src = new java.io.File(d.getAbsolutePath());
              java.io.File dst = new java.io.File(dir.getAbsolutePath() + "/" + d.getName());
              UtilSystem.copyRecursively(src, dst);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
            }
          }
          try {
            destDir.refresh(false,null);
          } catch (JajukException e) {
            Log.error(e);
            Messages.showErrorMessage(e.getCode());
          }
        }
        ObservationManager.notify(new Event(JajukEvents.EVENT_DEVICE_REFRESH));
        UtilGUI.stopWaiting();
      }
    }.start();
  }
}
