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
package org.uberfire.client.mvp;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.workbench.WorkbenchPanel;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.ActivityResourceType;

@ApplicationScoped
public class PlaceManagerImpl implements PlaceManager {

    private final Map<PlaceRequest, WorkbenchPanel> placePanelMap = new HashMap<>();
    private final Map<PlaceRequest, HasWidgets> placeCustomWidgetMap = new HashMap<>();

    @Inject
    private ActivityManager activityManager;
    @Inject
    private Instance<WorkbenchPanel> workbenchPanelInstance;

    @Override
    public void openDock(PlaceRequest place,
                         HasWidgets addTo) {
        final Activity dockActivity = activityManager.getActivity(place);
        if (place == null || !dockActivity.isType(ActivityResourceType.DOCK.name())) {
            return;
        }

        final WorkbenchPanel newPanel = workbenchPanelInstance.get();
        Widget panelViewWidget = newPanel.asWidget();
        panelViewWidget.addAttachHandler(new CustomPanelCleanupHandler(place));

        addTo.add(panelViewWidget);
        newPanel.init(dockActivity.getWidget());
        activityManager.openActivity(dockActivity.getIdentifier());
        placeCustomWidgetMap.put(place,
                                 addTo);
        placePanelMap.put(place,
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
            if (event.isAttached() || detaching || place == null || !placePanelMap.containsKey(place)) {
                return;
            }
            detaching = true;
            Scheduler.get().scheduleFinally(() -> {
                try {
                    closeDock(place);
                } finally {
                    detaching = false;
                }
            });
        }

        private void closeDock(final PlaceRequest place) {
            final Activity activity = activityManager.getActivity(place);
            activityManager.closeActivity(activity.getIdentifier());

            final WorkbenchPanel panelToRemove = placePanelMap.remove(place);
            if (panelToRemove != null) {
                panelToRemove.clear();

                final HasWidgets customContainer = placeCustomWidgetMap.remove(place);
                if (customContainer != null) {
                    customContainer.remove(panelToRemove.asWidget());
                }
                activityManager.destroyBean(panelToRemove);
            }
            activityManager.destroyActivity(activity);
        }
    }
}
