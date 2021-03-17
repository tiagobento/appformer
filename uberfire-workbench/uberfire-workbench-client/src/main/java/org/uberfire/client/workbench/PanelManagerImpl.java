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

package org.uberfire.client.workbench;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;

/**
 * Standard implementation of {@link PanelManager}.
 */
@ApplicationScoped
public class PanelManagerImpl implements PanelManager {

    protected final Map<PlaceRequest, WorkbenchPanel> mapPlaceToPanel = new HashMap<>();
    protected final Map<PlaceRequest, HasWidgets> customPanels = new HashMap<>();

    protected SyncBeanManager iocManager;
    protected PlaceManager placeManager;
    private final WorkbenchLayout workbenchLayout;
    private final Instance<WorkbenchPanel> workbenchPanelInstance;

    @Inject
    public PanelManagerImpl(SyncBeanManager iocManager,
                            PlaceManager placeManager,
                            WorkbenchLayout workbenchLayout,
                            Instance<WorkbenchPanel> workbenchPanelInstance) {
        this.iocManager = iocManager;
        this.placeManager = placeManager;
        this.workbenchLayout = workbenchLayout;
        this.workbenchPanelInstance = workbenchPanelInstance;
    }

    @Override
    public void setRoot(PlaceRequest place) {
        final WorkbenchPanel newPanel = workbenchPanelInstance.get();
        mapPlaceToPanel.put(place, newPanel);
        workbenchLayout.setContent(newPanel.asWidget());
    }

    @Override
    public void addWorkbenchPart(final PlaceRequest place,
                                 final IsWidget widget) {
        final WorkbenchPanel panel = mapPlaceToPanel.get(place);
        if (panel == null) {
            throw new IllegalArgumentException("Target panel is not part of the layout");
        }
        panel.init(widget);
        mapPlaceToPanel.put(place, panel);
    }

    @Override
    public void removePanelForPlace(final PlaceRequest toRemove,
                                    final boolean isDock) {
        if (toRemove == null) {
            return;
        }

        final WorkbenchPanel removedPanel = mapPlaceToPanel.remove(toRemove);
        removedPanel.clear();

        if (isDock) {
            removeWorkbenchPanelFromParent(toRemove,
                                           removedPanel);
        }
        iocManager.destroyBean(removedPanel);
    }

    private void removeWorkbenchPanelFromParent(final PlaceRequest toRemove,
                                                final WorkbenchPanel panelToRemove) {
        HasWidgets customContainer = customPanels.remove(toRemove);
        if (customContainer != null) {
            customContainer.remove(panelToRemove.asWidget());
        }
    }

    @Override
    public void addCustomPanel(final PlaceRequest place,
                               final HasWidgets container) {
        final WorkbenchPanel newPanel = workbenchPanelInstance.get();
        Widget panelViewWidget = newPanel.asWidget();
        panelViewWidget.addAttachHandler(new CustomPanelCleanupHandler(place));

        container.add(panelViewWidget);
        customPanels.put(place,
                         container);

        mapPlaceToPanel.put(place,
                            newPanel);
    }

    private final class CustomPanelCleanupHandler implements AttachEvent.Handler {
        private final PlaceRequest place;
        private boolean detaching;

        private CustomPanelCleanupHandler(PlaceRequest place) {
            this.place = place;
        }

        @Override
        public void onAttachOrDetach(AttachEvent event) {
            if (event.isAttached() || detaching || !mapPlaceToPanel.containsKey(place)) {
                return;
            }
            detaching = true;
            Scheduler.get().scheduleFinally(() -> {
                try {
                    placeManager.closePlace(place, null);
                } finally {
                    detaching = false;
                }
            });
        }
    }
}
