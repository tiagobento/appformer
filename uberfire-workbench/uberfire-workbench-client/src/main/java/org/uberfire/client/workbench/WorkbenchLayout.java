/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.client.workbench;

import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.resources.WorkbenchResources;
import org.uberfire.client.util.JSFunctions;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.workbench.docks.UberfireDocksContainer;

@EntryPoint
public class WorkbenchLayout {

    private final UberfireDocksContainer uberfireDocksContainer;
    private final PlaceManager placeManager;
    private final DockLayoutPanel rootContainer = new DockLayoutPanel(Unit.PX);
    private Widget currentContent;

    @Inject
    public WorkbenchLayout(final UberfireDocksContainer uberfireDocksContainer,
                           final PlaceManager placeManager) {
        this.uberfireDocksContainer = uberfireDocksContainer;
        this.placeManager = placeManager;
    }

    @AfterInitialization
    private void afterInit() {
        WorkbenchResources.INSTANCE.CSS().ensureInjected();

        uberfireDocksContainer.setup(rootContainer,
                                     () -> Scheduler.get().scheduleDeferred(this::onResize));
        Layouts.setToFillParent(rootContainer);

        RootLayoutPanel.get().add(rootContainer);
        placeManager.bootstrapRootPanel();

        // Resizing the Window should resize everything
        Window.addResizeHandler(event -> resizeTo(event.getWidth(),
                                                  event.getHeight()));

        // Defer the initial resize call until widgets are rendered and sizes are available
        Scheduler.get().scheduleDeferred(this::onResize);

        JSFunctions.notifyJSReady();
    }

    public void setContent(Widget content) {
        if (currentContent != null) {
            rootContainer.remove(currentContent);
        }
        rootContainer.add(content);
        Layouts.setToFillParent(content);
    }

    public void onResize() {
        resizeTo(Window.getClientWidth(),
                 Window.getClientHeight());
    }

    private void resizeTo(int width,
                          int height) {
        rootContainer.setPixelSize(width,
                                   height);

        // The dragBoundary can't be a LayoutPanel, so it doesn't support ProvidesResize/RequiresResize.
        // We start the cascade of onResize() calls at its immediate child.
        rootContainer.onResize();
    }
}
