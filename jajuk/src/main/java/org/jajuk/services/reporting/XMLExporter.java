/*
 *  Jajuk
 *  Copyright (C) 2006 The Jajuk Team
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
 *  $Revision: 2164 $
 */

package org.jajuk.services.reporting;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Item;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;

/**
 * This class exports music contents to XML.
 */
public class XMLExporter extends Exporter {

  /** Private Constants */
  private static final String NEWLINE = "\n";

  private static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";

  private BufferedWriter writer;

  /** Do we want to export tracks ?* */
  private boolean showTracks = true;

  /** PUBLIC METHODS */

  public XMLExporter() throws IOException {
    cache = UtilSystem.getConfFileByPath(Const.FILE_REPORTING_CACHE_FILE + "_XML_"
        + System.currentTimeMillis());
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache, false), "UTF-8"));
  }

  /**
   * This method will create a tagging of the specified item
   * 
   * @param item
   *          The item to report (can be an album, a year, an author ,a style, a
   *          directory or a device)
   * @return Returns a string containing the report, or null if an error
   *         occurred.
   */
  public void process(Item item) throws Exception {
    if (item instanceof Album) {
      process((Album) item);
    } else if (item instanceof Author) {
      process((Author) item);
    } else if (item instanceof Style) {
      process((Style) item);
    } else if (item instanceof Year) {
      process((Year) item);
    } else if (item instanceof Directory) {
      process((Directory) item);
    } else if (item instanceof Device) {
      process((Device) item);
    }
  }

  /**
   * This method will create a tagging of the specified album and its tracks.
   * 
   * @param album
   *          The album to tag.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Album album) throws Exception {
    // Make sure we have an album.
    if (album != null) {
      tagAlbum(album, 0);
    }
  }

  /**
   * This method will create a reporting of the specified year and its albums
   * and associated tracks.
   * 
   * @param year
   *          The year to report.
   * @return Returns a string containing the report, or null if an error
   *         occurred.
   */
  public void process(Year year) throws Exception {
    if (year != null) {
      tagYear(year, 0);
    }
  }

  /**
   * This method will create a tagging of the specified author and its albums
   * and associated tracks.
   * 
   * @param author
   *          The author to tag.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Author author) throws Exception {
    if (author != null) {
      tagAuthor(author, 0);
    }
  }

  /**
   * This method will create a tagging of the specified style.
   * 
   * @param style
   *          The style to tag.
   * @return Returns a string containing the tagging, or null is an error
   *         occurred.
   */
  public void process(Style style) throws Exception {
    if (style != null) {
      tagStyle(style, 0);
    }
  }

  /**
   * This method will create a tagging of a directory and all its children files
   * and directories.
   * 
   * @param directory
   *          The directory to start from.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Directory directory) throws Exception {
    if (directory != null) {
      tagDirectory(directory);
    }
  }

  /**
   * This method will create a tagging of a device and all its children files
   * and directories.
   * 
   * @param device
   *          The device to start from.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Device device) throws Exception {
    if (device != null) {
      tagDevice(device);
    }
  }

  /**
   * @see Exporter.processColllection
   */
  @Override
  @SuppressWarnings("unchecked")
  public void processCollection(int type) throws Exception {
    // If we are tagging the physical collection...
    if (type == XMLExporter.PHYSICAL_COLLECTION) {
      // Same effect than selecting all devices
      process((List<Item>) DeviceManager.getInstance().getItems());
    } else if (type == LOGICAL_COLLECTION) {
      // Same effect than selecting all styles
      process((List<Item>) StyleManager.getInstance().getItems());
    }
  }

  /** PRIVATE HELPER METHODS */

  private void exportDirectoryHelper(int level, Directory directory) throws Exception {
    // Get the children
    List<Directory> children = new ArrayList<Directory>(directory.getDirectories());
    writer.write(addTabs(level) + Tag.openTag(Const.XML_DIRECTORY) + NEWLINE);
    String sName = UtilString.formatXML(directory.getName());
    String sID = UtilString.formatXML(directory.getID());
    String sPath = UtilString.formatXML(directory.getAbsolutePath());
    // Tag directory data.
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_PATH, sPath) + NEWLINE);
    // Tag children directories
    for (Directory d : children) {
      exportDirectoryHelper(level + 1, d);
    }
    // Tag children files
    for (org.jajuk.base.File file : directory.getFiles()) {
      tagFile(file, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_DIRECTORY) + NEWLINE);
  }

  private void tagFile(org.jajuk.base.File file, int level) throws Exception {
    String sFileID = file.getID();
    String sName = UtilString.formatXML(file.getName());
    String sPath = UtilString.formatXML(file.getAbsolutePath());
    long lSize = file.getSize();
    writer.write(addTabs(level) + Tag.openTag(Const.XML_FILE) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sFileID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_PATH, sPath) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_SIZE, lSize) + NEWLINE);
    tagTrack(file.getTrack(), level + 1);
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_FILE) + NEWLINE);
  }

  private void tagDirectory(Directory directory) throws Exception {
    // Make sure we have a directory.
    if (directory != null) {
      writer.write(Tag.openTag(Const.XML_DIRECTORY) + NEWLINE);
      String sName = UtilString.formatXML(directory.getName());
      String sPath = UtilString.formatXML(directory.getAbsolutePath());
      String sID = directory.getID();

      // Tag directory data.
      writer.write(addTabs(1) + Tag.tagData(Const.XML_ID, sID) + NEWLINE);
      writer.write(addTabs(1) + Tag.tagData(Const.XML_NAME, sName) + NEWLINE);
      writer.write(addTabs(1) + Tag.tagData(Const.XML_PATH, sPath) + NEWLINE);

      // Tag directory children data.
      for (Directory d : new ArrayList<Directory>(directory.getDirectories())) {
        exportDirectoryHelper(1, d);
      }
      // Tag directory file children data.
      for (org.jajuk.base.File file : directory.getFiles()) {
        tagFile(file, 1);
      }
      writer.write(Tag.closeTag(Const.XML_DIRECTORY) + NEWLINE);
    }
  }

  private void tagDevice(Device device) throws Exception {
    String sID = device.getID();
    writer.write(Tag.openTag(Const.XML_DEVICE) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(Const.XML_ID, sID) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(Const.XML_NAME, UtilString.formatXML(device.getName()))
        + NEWLINE);
    writer.write(addTabs(1)
        + Tag.tagData(Const.XML_TYPE, UtilString.formatXML(device.getDeviceTypeS())) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(Const.XML_URL, UtilString.formatXML(device.getUrl()))
        + NEWLINE);
    Directory dir = DirectoryManager.getInstance().getDirectoryForIO(device.getFio());
    // check void devices
    if (dir != null) {
      // Tag children directories of device.
      for (Directory directory : new ArrayList<Directory>(dir.getDirectories())) {
        exportDirectoryHelper(1, directory);
      }
      // Tag children files of device.
      for (org.jajuk.base.File file : DirectoryManager.getInstance().getDirectoryForIO(
          device.getFio()).getFiles()) {
        tagFile(file, 1);
      }
    }
    writer.write(Tag.closeTag(Const.XML_DEVICE) + NEWLINE);
  }

  private void tagTrack(Track track, int level) throws Exception {
    String sTrackID = track.getID();
    String sTrackName = UtilString.formatXML(track.getName());
    String sTrackStyle = UtilString.formatXML(track.getStyle().getName2());
    String sTrackAuthor = UtilString.formatXML(track.getAuthor().getName2());
    String sTrackAlbum = UtilString.formatXML(track.getAlbum().getName2());
    long lTrackLength = track.getDuration();
    long lTrackRate = track.getRate();
    String sTrackComment = UtilString.formatXML(track.getComment());
    long lTrackOrder = track.getOrder();
    writer.write(addTabs(level) + Tag.openTag(Const.XML_TRACK) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sTrackID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_NAME, sTrackName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_STYLE, sTrackStyle) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_AUTHOR, sTrackAuthor) + NEWLINE);
    writer.write(addTabs(level + 1)
        + Tag.tagData(Const.XML_TRACK_LENGTH, UtilString.formatTimeBySec(lTrackLength)) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_RATE, lTrackRate) + NEWLINE);
    writer
        .write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_COMMENT, sTrackComment) + NEWLINE);
    writer.write(addTabs(level + 1)
        + Tag.tagData(Const.XML_TRACK_ORDER, UtilString.padNumber(lTrackOrder, 2)) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_ALBUM, sTrackAlbum) + NEWLINE);
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_TRACK) + NEWLINE);
  }

  private void tagAlbum(Album album, int level) throws Exception {
    String sAlbumID = album.getID();
    String sAlbumName = UtilString.formatXML(album.getName2());
    String sStyleName = "";
    String sAuthorName = "";
    String sYear = "";
    List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
    if (tracks.size() > 0) {
      sStyleName = UtilString.formatXML(tracks.iterator().next().getStyle().getName2());
      sAuthorName = UtilString.formatXML(tracks.iterator().next().getAuthor().getName2());
      sYear = tracks.iterator().next().getYear().getName2();
    }
    writer.write(addTabs(level) + Tag.openTag(Const.XML_ALBUM) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sAlbumID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sAlbumName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_AUTHOR, sAuthorName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_STYLE, sStyleName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_YEAR, sYear) + NEWLINE);
    // For full collection, we don't show detailed tracks for performance
    // reasons
    if (showTracks) {
      for (Track track : tracks) {
        tagTrack(track, level + 1);
      }
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_ALBUM) + NEWLINE);
  }

  private void tagAuthor(Author author, int level) throws Exception {
    String sAuthorID = author.getID();
    String sAuthorName = UtilString.formatXML(author.getName2());
    writer.write(addTabs(level) + Tag.openTag(Const.XML_AUTHOR) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sAuthorID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sAuthorName) + NEWLINE);
    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(author);
    // Collections.sort(albums);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_AUTHOR) + NEWLINE);
  }

  private void tagYear(Year year, int level) throws Exception {
    String sYearID = year.getID();
    String sYearName = year.getName();
    writer.write(addTabs(level) + Tag.openTag(Const.XML_YEAR) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sYearID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sYearName) + NEWLINE);
    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(year);
    // Collections.sort(albums);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_YEAR) + NEWLINE);
  }

  private void tagStyle(Style style, int level) throws Exception {
    String sStyleID = style.getID();
    String sStyleName = UtilString.formatXML(style.getName2());
    writer.write(addTabs(level) + Tag.openTag(Const.XML_STYLE) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sStyleID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sStyleName) + NEWLINE);
    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(style);
    // Collections.sort(albums);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    List<Author> authors = AuthorManager.getInstance().getAssociatedAuthors(style);
    // Collections.sort(authors);
    for (Author author : authors) {
      tagAuthor(author, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_STYLE) + NEWLINE);
  }

  private String addTabs(int num) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < num) {
      sb.append('\t');
      i++;
    }
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.reporting.Exporter#process(java.util.List)
   */
  @Override
  public void process(List<Item> collection) throws Exception {
    try {
      writer.write(XML_HEADER + NEWLINE + Tag.openTag(Const.XML_COLLECTION) + NEWLINE);
      // Sort the collection thanks a tree set (we can't use Collections.sort()
      // here due to generics)
      TreeSet<Item> ts = new TreeSet<Item>();
      for (Item item : collection) {
        ts.add(item);
      }
      for (Item item : ts) {
        process(item);
      }
      // Add I18N nodes
      writer.write(Tag.openTag("i18n"));
      int i = 1;
      while (Messages.contains("ReportAction." + i)) {
        writer.write('\t' + Tag.tagData("ReportAction." + i, Messages
            .getString("ReportAction." + i)));
        i++;
      }
      writer.write('\t' + Tag.tagData("ReportAction.name", Messages.getString("Property_name")));
      writer
          .write('\t' + Tag.tagData("ReportAction.author", Messages.getString("Property_author")));
      writer.write('\t' + Tag.tagData("ReportAction.style", Messages.getString("Property_style")));
      writer.write('\t' + Tag.tagData("ReportAction.order", Messages.getString("Property_track")));
      writer.write('\t' + Tag.tagData("ReportAction.track", Messages.getString("Item_Track")));
      writer.write('\t' + Tag.tagData("ReportAction.album", Messages.getString("Property_album")));
      writer
          .write('\t' + Tag.tagData("ReportAction.length", Messages.getString("Property_length")));
      writer.write('\t' + Tag.tagData("ReportAction.year", Messages.getString("Property_year")));
      writer.write('\t' + Tag.tagData("ReportAction.rate", Messages.getString("Property_rate")));
      writer.write('\t' + Tag.tagData("ReportAction.url", Messages.getString("Property_url")));
      writer.write('\t' + Tag.tagData("ReportAction.type", Messages.getString("Property_type")));
      writer.write('\t' + Tag.tagData("ReportAction.comment", Messages
          .getString("Property_comment")));
      writer.write(Tag.closeTag("i18n"));
      writer.write(Tag.closeTag(Const.XML_COLLECTION));
    } finally {
      writer.flush();
      writer.close();
    }

  }

  protected void setShowTracks(boolean showTracks) {
    this.showTracks = showTracks;
  }

}

/**
 * This class will create taggings. It will create either open tags, closed
 * tags, or full tagging with data.
 */
final class Tag {
  /**
   * private constructor to avoid instantiating utility class
   */
  private Tag() {
  }

  public static String openTag(String tagname) {
    return "<" + tagname + ">";
  }

  public static String closeTag(String tagname) {
    return "</" + tagname + ">";
  }

  public static String tagData(String tagname, String data) {
    return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
  }

  public static String tagData(String tagname, long data) {
    return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
  }

  public static String tagData(String tagname, int data) {
    return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
  }

  public static String tagData(String tagname, double data) {
    return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
  }
}
