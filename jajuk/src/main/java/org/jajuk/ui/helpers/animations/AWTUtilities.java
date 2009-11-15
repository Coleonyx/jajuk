/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
 *  http://jajuk.info
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
 *  $Revision$
 */

package org.jajuk.ui.helpers.animations;

import java.awt.Shape;
import java.awt.Window;
import java.lang.reflect.Method;

/**
 * Wrapper class used to handle cases where JRE AWTUtilities class is not available.
 * 
 * See https://trac.jajuk.info/ticket/1464 for more details
 */
public class AWTUtilities {
  
  /**
   * Checks if is available.
   * 
   * @return true, if is available
   */
  @SuppressWarnings("unchecked")
  public static boolean isAvailable() {
    try {
      @SuppressWarnings("unused")
      Class awtutil = Class.forName("com.sun.awt.AWTUtilities");
      return true;
    } catch (Exception ex) {

    }
    return false;
  }

  /**
   * Sets the window shape.
   * DOCUMENT_ME
   * 
   * @param window DOCUMENT_ME
   * @param shape DOCUMENT_ME
   */
  @SuppressWarnings("unchecked")
  public static void setWindowShape(Window window, Shape shape) {
    try {
      Class awtutil = Class.forName("com.sun.awt.AWTUtilities");
      Method setWindowShape = awtutil.getMethod("setWindowShape", Window.class, Shape.class);
      setWindowShape.invoke(null, window, shape);
    } catch (Exception ex) {

    }
  }

  /**
   * Sets the window opacity.
   * DOCUMENT_ME
   * 
   * @param window DOCUMENT_ME
   * @param alpha DOCUMENT_ME
   */
  @SuppressWarnings("unchecked")
  public static void setWindowOpacity(Window window, float alpha) {
    try {
      Class awtutil = Class.forName("com.sun.awt.AWTUtilities");
      Method setWindowOpaque = awtutil.getMethod("setWindowOpacity", Window.class, float.class);
      setWindowOpaque.invoke(null, window, alpha);
    } catch (Exception ex) {

    }
  }

  /**
   * Instantiates a new aWT utilities.
   */
  private AWTUtilities() {

  }
}
