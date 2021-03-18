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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.resources.WorkbenchResources;
import org.uberfire.client.util.CSSLocatorsUtils;
import org.uberfire.client.util.JSFunctions;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.workbench.docks.UberfireDocksContainer;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.ActivityResourceType;

@EntryPoint
public class WorkbenchEntryPoint {

    @Inject
    private UberfireDocksContainer uberfireDocksContainer;
    @Inject
    private ActivityManager activityManager;

    private final DockLayoutPanel rootContainer = new DockLayoutPanel(Unit.PX);

    private final Map<PlaceRequest, ScrollPanel> dockPanels = new HashMap<>();
    private final Map<PlaceRequest, HasWidgets> placeCustomWidgetMap = new HashMap<>();

    @AfterInitialization
    private void afterInit() {
        WorkbenchResources.INSTANCE.CSS().ensureInjected();

        uberfireDocksContainer.setup(rootContainer,
                                     () -> Scheduler.get().scheduleDeferred(this::onResize));

        Layouts.setToFillParent(rootContainer);
        RootLayoutPanel.get().add(rootContainer);
        bootstrapRootPanel();

        Window.addResizeHandler(event -> resizeTo(event.getWidth(),
                                                  event.getHeight()));
        Scheduler.get().scheduleDeferred(this::onResize);

        JSFunctions.notifyJSReady();
    }

    public void onResize() {
        resizeTo(Window.getClientWidth(),
                 Window.getClientHeight());
    }

    private void resizeTo(int width,
                          int height) {
        rootContainer.setPixelSize(width,
                                   height);
        rootContainer.onResize();
    }

    private void bootstrapRootPanel() {
        final DefaultPlaceRequest editorPlace = new DefaultPlaceRequest(activityManager.getEditorActivity().getIdentifier());
        final Activity editorActivity = activityManager.getActivity(editorPlace);
        if (!editorActivity.isType(ActivityResourceType.EDITOR.name())) {
            return;
        }

        final SimpleLayoutPanel panel = new SimpleLayoutPanel();
        panel.getElement().addClassName(CSSLocatorsUtils.buildLocator("qe", "static-workbench-panel-view"));

        final ScrollPanel sp = new ScrollPanel();
        sp.setWidget(editorActivity.getWidget());
        sp.getElement().getFirstChildElement().setClassName("uf-scroll-panel");

        panel.setWidget(sp);
        Layouts.setToFillParent(panel);

        rootContainer.add(panel);
        activityManager.openActivity(editorActivity.getIdentifier());
        onResize();
    }

    public void openDock(PlaceRequest place,
                         HasWidgets container) {
        final Activity dockActivity = activityManager.getActivity(place);
        if (place == null || !dockActivity.isType(ActivityResourceType.DOCK.name())) {
            return;
        }

        final ScrollPanel panel = new ScrollPanel();
        panel.setWidget(dockActivity.getWidget());
        panel.addAttachHandler(new CleanupHandler(place));
        Layouts.setToFillParent(panel);
        container.add(panel);
        activityManager.openActivity(dockActivity.getIdentifier());
        placeCustomWidgetMap.put(place,
                                 container);
        dockPanels.put(place,
                       panel);
    }

    private final class CleanupHandler implements AttachEvent.Handler {

        private final PlaceRequest place;
        private boolean detaching;

        private CleanupHandler(PlaceRequest place) {
            this.place = place;
        }

        @Override
        public void onAttachOrDetach(AttachEvent event) {
            if (event.isAttached() || detaching || place == null || !dockPanels.containsKey(place)) {
                return;
            }
            detaching = true;
            Scheduler.get().scheduleFinally(() -> {
                try {
                    final Activity activity = activityManager.getActivity(place);
                    activityManager.closeActivity(activity.getIdentifier());

                    final ScrollPanel panelToRemove = dockPanels.remove(place);
                    if (panelToRemove != null) {
                        panelToRemove.clear();

                        final HasWidgets customContainer = placeCustomWidgetMap.remove(place);
                        if (customContainer != null) {
                            customContainer.remove(panelToRemove.asWidget());
                        }
                    }
                    activityManager.destroyActivity(activity);
                } finally {
                    detaching = false;
                }
            });
        }
    }
}
