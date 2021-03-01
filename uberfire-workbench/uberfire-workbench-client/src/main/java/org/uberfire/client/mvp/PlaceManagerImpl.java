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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

import com.google.gwt.user.client.ui.HasWidgets;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.api.SharedSingleton;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.client.workbench.WorkbenchLayout;
import org.uberfire.client.workbench.panels.WorkbenchPanelPresenterImpl;
import org.uberfire.mvp.BiParameterizedCommand;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.ActivityResourceType;
import org.uberfire.workbench.model.CustomPanelDefinition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PartDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;

import static org.uberfire.plugin.PluginUtil.toInteger;

@SharedSingleton
@EnabledByProperty(value = "uberfire.plugin.mode.active", negated = true)
public class PlaceManagerImpl implements PlaceManager {

    private final Map<PlaceRequest, Activity> existingWorkbenchActivities = new HashMap<>();
    private final Map<PlaceRequest, PartDefinition> visibleWorkbenchParts = new HashMap<>();
    private final Map<PlaceRequest, CustomPanelDefinition> customPanels = new HashMap<>();

    @Inject
    private ActivityManager activityManager;
    @Inject
    private PanelManager panelManager;
    @Inject
    private WorkbenchLayout workbenchLayout;
    @Inject
    private SyncBeanManager iocManager;

    @Override
    public void bootstrapRootPanel() {
        final BiParameterizedCommand<PanelDefinition, PartDefinition> command = (panelDef, partDef) -> {
            panelManager.setRoot(panelDef);
            goToEditor(partDef, panelDef);
            workbenchLayout.onResize();
        };

        final PanelDefinitionImpl rootPanel = new PanelDefinitionImpl(WorkbenchPanelPresenterImpl.class.getName());
        final PartDefinitionImpl editorPart = new PartDefinitionImpl(resolveEditorPlaceRequest());
        rootPanel.setRoot(true);
        rootPanel.addPart(editorPart);
        command.execute(rootPanel, editorPart);
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
        if (place == null || place.equals(PlaceRequest.NOWHERE)) {
            return;
        }

        final Predicate<CustomPanelDefinition> filter = p -> p.getHasWidgetsContainer().isPresent()
                && p.getHasWidgetsContainer().get().equals(addTo);

        new HashSet<>(customPanels.values()).stream()
                .filter(filter)
                .flatMap(p -> p.getParts().stream())
                .forEach(part -> closePlace(part.getPlace()));

        final CustomPanelDefinition customPanel = panelManager.addCustomPanel(addTo);
        customPanels.put(place,
                         customPanel);

        final Activity activity = resolveActivity(place);

        if (activity.isType(ActivityResourceType.DOCK.name())) {
            if (visibleWorkbenchParts.containsKey(place)) {
                return;
            }

            launchActivity(place,
                           activity,
                           new PartDefinitionImpl(place),
                           customPanel);
        }
    }

    private void goToEditor(final PartDefinition part,
                            final PanelDefinition panel) {
        final PlaceRequest place = part.getPlace();
        if (place == null) {
            return;
        }

        final Activity resolved = resolveActivity(place);

        if (!resolved.isType(ActivityResourceType.EDITOR.name())) {
            throw new IllegalArgumentException("Only EditorActivity can be launched in a specific targetPanel.");
        }

        launchActivity(place,
                       resolved,
                       part,
                       panel);
    }

    private Activity resolveActivity(final PlaceRequest place) {
        final Activity existingDestination = resolveExistingParts(place);

        if (existingDestination != null) {
            return existingDestination;
        }

        final Set<Activity> activities = activityManager.getActivities(place);

        if (activities.size() != 1) {
            throw new RuntimeException("There shouldn't be more than one activity associated with a place request.");
        }

        Activity resolvedActivity = activities.iterator().next();
        existingWorkbenchActivities.put(place,
                                        resolvedActivity);
        return resolvedActivity;
    }

    private Activity resolveExistingParts(final PlaceRequest place) {
        final Activity activity = getActivity(place);

        if (activity != null) {
            return activity;
        }

        return null;
    }

    private Activity getActivity(final PlaceRequest place) {
        if (place == null) {
            return null;
        }
        return existingWorkbenchActivities.get(place);
    }

    @Override
    public void closePlace(final PlaceRequest placeToClose) {
        if (placeToClose == null) {
            return;
        }
        closePlace(placeToClose,
                   null);
    }

    private void launchActivity(final PlaceRequest place,
                                final Activity activity,
                                final PartDefinition part,
                                final PanelDefinition panel) {
        if (visibleWorkbenchParts.containsKey(place)) {
            return;
        }

        visibleWorkbenchParts.put(place,
                                  part);

        panelManager.addWorkbenchPart(place,
                                      part,
                                      panel,
                                      activity.getWidget(),
                                      toInteger(panel.getWidthAsInt()),
                                      toInteger(panel.getHeightAsInt()));

        try {
            activity.onOpen();
        } catch (Exception ex) {
            closePlace(place);
        }
    }

    @Override
    public void closePlace(final PlaceRequest place,
                           final Command onAfterClose) {

        final Activity existingActivity = existingWorkbenchActivities.get(place);
        if (existingActivity == null) {
            return;
        }

        final Command closeCommand = getCloseCommand(place,
                                                     onAfterClose);

        final BiParameterizedCommand<Command, PlaceRequest> closeChain = (chain, placeRequest) -> chain.execute();
        closeChain.execute(closeCommand,
                           place);
    }

    private Command getCloseCommand(final PlaceRequest place,
                                    final Command onAfterClose) {
        return () -> {

            final Activity activity = existingWorkbenchActivities.get(place);
            if (activity == null) {
                return;
            }

            activity.onClose();

            panelManager.removePartForPlace(place);
            existingWorkbenchActivities.remove(place);
            visibleWorkbenchParts.remove(place);
            activityManager.destroyActivity(activity);

            // currently, we force all custom panels as Static panels, so they can only ever contain the one part we put in them.
            // we are responsible for cleaning them up when their place closes.
            PanelDefinition customPanelDef = customPanels.remove(place);
            if (customPanelDef != null) {
                panelManager.removeWorkbenchPanel(customPanelDef);
            }

            if (onAfterClose != null) {
                onAfterClose.execute();
            }
        };
    }
}
