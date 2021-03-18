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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.workbench.WorkbenchLayout;
import org.uberfire.client.workbench.WorkbenchPanel;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.ActivityResourceType;

@ApplicationScoped
public class PlaceManagerImpl implements PlaceManager {

    private final Map<PlaceRequest, Activity> existingWorkbenchActivities = new HashMap<>();
    private final List<PlaceRequest> dockPlaces = new ArrayList<>();
    private final Map<PlaceRequest, WorkbenchPanel> mapPlaceToPanel = new HashMap<>();
    private final Map<PlaceRequest, HasWidgets> customPanels = new HashMap<>();

    @Inject
    private ActivityManager activityManager;
    @Inject
    private WorkbenchLayout workbenchLayout;
    @Inject
    private SyncBeanManager iocManager;
    @Inject
    private Instance<WorkbenchPanel> workbenchPanelInstance;

    @Override
    public void bootstrapRootPanel() {
        final ParameterizedCommand<PlaceRequest> command = editorPlace -> {
            final WorkbenchPanel panel = workbenchPanelInstance.get();
            workbenchLayout.setContent(panel.asWidget());

            final Activity editorActivity = resolveActivity(editorPlace, ActivityResourceType.EDITOR);
            if (editorActivity == null) {
                return;
            }

            launchActivity(panel,
                           editorPlace,
                           editorActivity);

            workbenchLayout.onResize();
        };
        command.execute(resolveEditorPlaceRequest());
    }

    private DefaultPlaceRequest resolveEditorPlaceRequest() {
        final Collection<SyncBeanDef<EditorActivity>> editors = iocManager.lookupBeans(EditorActivity.class);

        if (editors.size() != 1) {
            throw new RuntimeException("There must be exactly one instance of EditorActivity.");
        }

        return new DefaultPlaceRequest(editors.iterator().next().getInstance().getIdentifier());
    }

    @Override
    public void goToDock(PlaceRequest place,
                         HasWidgets addTo) {
        final Activity dockActivity = resolveActivity(place, ActivityResourceType.DOCK);
        if (place == null || dockActivity == null) {
            return;
        }

        final WorkbenchPanel panel = addDockPanel(place, addTo);
        dockPlaces.add(place);
        launchActivity(panel,
                       place,
                       dockActivity);
    }

    private Activity resolveActivity(final PlaceRequest place,
                                     final ActivityResourceType type) {
        final Set<Activity> activities = activityManager.getActivities(place);
        if (activities.size() != 1) {
            throw new RuntimeException("There shouldn't be more than one activity associated with a place request.");
        }

        final Activity resolvedActivity = activities.iterator().next();
        if (!resolvedActivity.isType(type.name())) {
            return null;
        }

        existingWorkbenchActivities.put(place,
                                        resolvedActivity);
        return resolvedActivity;
    }

    private void launchActivity(final WorkbenchPanel panel,
                                final PlaceRequest place,
                                final Activity activity) {
        panel.init(activity.getWidget());
        try {
            activity.onOpen();
        } catch (Exception ex) {
            closePlace(place, null);
        }
    }

    @Override
    public void closePlace(final PlaceRequest place,
                           final Command onAfterClose) {
        if (place == null) {
            return;
        }
        final Activity activity = existingWorkbenchActivities.get(place);
        if (activity == null) {
            return;
        }

        activity.onClose();

        removePanelForPlace(place, dockPlaces.remove(place));
        existingWorkbenchActivities.remove(place);
        activityManager.destroyActivity(activity);

        if (onAfterClose != null) {
            onAfterClose.execute();
        }
    }

    private void removePanelForPlace(final PlaceRequest toRemove,
                                     final boolean isDock) {
        if (toRemove == null) {
            return;
        }

        final WorkbenchPanel panelToRemove = mapPlaceToPanel.remove(toRemove);
        if (panelToRemove == null) {
            return;
        }

        panelToRemove.clear();

        if (isDock) {
            HasWidgets customContainer = customPanels.remove(toRemove);
            if (customContainer != null) {
                customContainer.remove(panelToRemove.asWidget());
            }
        }

        iocManager.destroyBean(panelToRemove);
    }

    private WorkbenchPanel addDockPanel(final PlaceRequest place,
                                        final HasWidgets container) {
        final WorkbenchPanel newPanel = workbenchPanelInstance.get();
        Widget panelViewWidget = newPanel.asWidget();
        panelViewWidget.addAttachHandler(new CustomPanelCleanupHandler(place));

        container.add(panelViewWidget);
        customPanels.put(place,
                         container);

        mapPlaceToPanel.put(place,
                            newPanel);
        return newPanel;
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
                    closePlace(place, null);
                } finally {
                    detaching = false;
                }
            });
        }
    }
}
