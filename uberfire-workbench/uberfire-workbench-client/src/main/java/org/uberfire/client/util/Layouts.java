/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;

import static org.uberfire.plugin.PluginUtil.toInteger;

public class Layouts {

    public static final int DEFAULT_CHILD_SIZE = 100;

    /**
     * Sets the CSS on the given widget so it automatically fills the available space, rather than being sized based on
     * the amount of space required by its contents. This tends to be useful when building a UI that always fills the
     * available space on the screen, as most desktop application windows do.
     * <p>
     * To achieve this, the element is given relative positioning with top and left set to 0px and width and height set
     * to 100%. This makes the widget fill its nearest ancestor which has relative or absolute positioning. This
     * technique is compatible with GWT's LayoutPanel system. Note that, like LayoutPanels, this only works if the host
     * page is in standards mode (has a {@code <!DOCTYPE html>} header).
     * @param w the widget that should always fill its available space, rather than being sized to fit its contents.
     */
    public static void setToFillParent(Widget w) {
        Element e = w.getElement();
        Style s = e.getStyle();
        s.setPosition(Position.RELATIVE);
        s.setTop(0.0,
                 Unit.PX);
        s.setLeft(0.0,
                  Unit.PX);
        s.setWidth(100.0,
                   Unit.PCT);
        s.setHeight(100.0,
                    Unit.PCT);
        s.setOutlineStyle(Style.OutlineStyle.NONE);
    }

    /**
     * Returns the current width or height of the given panel definition.
     * @param position determines which dimension (width or height) to return.
     * @param definition the definition to get the size information from.
     * @return the with if position is EAST or WEST; the height if position is NORTH or SOUTH. If no size is provided by the PanelDefinition the DEFAULT_CHILD_SIZE is used.
     */
    public static int widthOrHeight(CompassPosition position,
                                    PanelDefinition definition) {
        switch (position) {
            case NORTH:
            case SOUTH:
                return heightOrDefault(definition);
            case EAST:
            case WEST:
                return widthOrDefault(definition);
            default:
                throw new IllegalArgumentException("Position " + position + " has no horizontal or vertial aspect.");
        }
    }

    public static int heightOrDefault(PanelDefinition def) {
        Integer height = toInteger(def.getHeightAsInt());
        return height == null ? DEFAULT_CHILD_SIZE : height;
    }

    public static int widthOrDefault(PanelDefinition def) {
        Integer width = toInteger(def.getWidthAsInt());
        return width == null ? DEFAULT_CHILD_SIZE : width;
    }
}
