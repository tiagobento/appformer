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
import org.uberfire.client.workbench.panels.WorkbenchPanelPresenter;
import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * Standard implementation of {@link PanelManager}.
 */
@ApplicationScoped
public class PanelManagerImpl implements PanelManager {

    protected final Map<PlaceRequest, WorkbenchPartPresenter> mapPlaceToPresenter = new HashMap<>();
    protected final Map<PanelDefinition, WorkbenchPanelPresenter> mapPanelDefinitionToPresenter = new HashMap<>();
    protected final Map<PanelDefinition, HasWidgets> customPanels = new HashMap<>();
    private boolean isRootSet;

    protected SyncBeanManager iocManager;
    protected PlaceManager placeManager;
    private final WorkbenchLayout workbenchLayout;
    private final Instance<WorkbenchPartPresenter> partPresenterInstances;
    private final Instance<WorkbenchPanelPresenter> panelPresenterInstances;

    @Inject
    public PanelManagerImpl(SyncBeanManager iocManager,
                            PlaceManager placeManager,
                            WorkbenchLayout workbenchLayout,
                            Instance<WorkbenchPartPresenter> partPresenterInstances,
                            Instance<WorkbenchPanelPresenter> panelPresenterInstances) {
        this.iocManager = iocManager;
        this.placeManager = placeManager;
        this.workbenchLayout = workbenchLayout;
        this.partPresenterInstances = partPresenterInstances;
        this.panelPresenterInstances = panelPresenterInstances;
    }

    @Override
    public void setRoot(PanelDefinition root) {
        checkNotNull("root",
                     root);

        if (isRootSet) {
            throw new RuntimeException("Cannot set root more than once.");
        }

        final WorkbenchPanelPresenter newPresenter = panelPresenterInstances.get();
        newPresenter.setDefinition(root);
        mapPanelDefinitionToPresenter.put(root, newPresenter);
        workbenchLayout.setContent(newPresenter.getPanelView().asWidget());
        isRootSet = true;
    }

    @Override
    public void addWorkbenchPart(final PlaceRequest place,
                                 final PanelDefinition panelDef,
                                 final IsWidget widget) {
        checkNotNull("panel",
                     panelDef);

        final WorkbenchPanelPresenter panelPresenter = mapPanelDefinitionToPresenter.get(panelDef);
        if (panelPresenter == null) {
            throw new IllegalArgumentException("Target panel is not part of the layout");
        }

        WorkbenchPartPresenter part =
                mapPlaceToPresenter.computeIfAbsent(place, pr -> {
                    WorkbenchPartPresenter p = partPresenterInstances.get();
                    p.setPlace(pr);
                    p.setWrappedWidget(widget);
                    return p;
                });

        panelPresenter.addPart(part);
    }

    @Override
    public boolean removePartForPlace(PlaceRequest toRemove) {
        if (toRemove != null) {
            removePlace(toRemove);
            return true;
        }
        return false;
    }

    @Override
    public void removeWorkbenchPanel(final PanelDefinition toRemove) throws IllegalStateException {
        if (toRemove.isRoot()) {
            throw new IllegalArgumentException("The root panel cannot be removed. To replace it, call setRoot()");
        }
        if (toRemove.getPlace() != null) {
            throw new IllegalStateException("Panel still contains place: " + toRemove.getPlace());
        }

        final WorkbenchPanelPresenter presenterToRemove = mapPanelDefinitionToPresenter.remove(toRemove);
        if (presenterToRemove == null) {
            throw new IllegalArgumentException("Couldn't find panel to remove: " + toRemove);
        }

        removeWorkbenchPanelFromParent(toRemove,
                                       presenterToRemove);

        iocManager.destroyBean(presenterToRemove);
    }

    private void removeWorkbenchPanelFromParent(final PanelDefinition toRemove,
                                                final WorkbenchPanelPresenter presenterToRemove) {
        HasWidgets customContainer = customPanels.remove(toRemove);
        if (customContainer != null) {
            customContainer.remove(presenterToRemove.getPanelView().asWidget());
        }
    }

    protected void removePlace(final PlaceRequest place) {
        for (Map.Entry<PanelDefinition, WorkbenchPanelPresenter> e : mapPanelDefinitionToPresenter.entrySet()) {
            final WorkbenchPanelPresenter panelPresenter = e.getValue();
            if (panelPresenter.getDefinition().getPlace().equals(place)) {
                panelPresenter.removePlace(place);
                break;
            }
        }

        WorkbenchPartPresenter deadPartPresenter = mapPlaceToPresenter.remove(place);
        iocManager.destroyBean(deadPartPresenter);
    }

    @Override
    public PanelDefinition addCustomPanel(final HasWidgets container) {
        final PanelDefinitionImpl panelDef = new PanelDefinitionImpl();
        final WorkbenchPanelPresenter panelPresenter = panelPresenterInstances.get();
        panelPresenter.setDefinition(panelDef);
        Widget panelViewWidget = panelPresenter.getPanelView().asWidget();
        panelViewWidget.addAttachHandler(new CustomPanelCleanupHandler(panelPresenter));

        container.add(panelViewWidget);
        customPanels.put(panelDef,
                         container);

        mapPanelDefinitionToPresenter.put(panelDef,
                                          panelPresenter);
        return panelDef;
    }

    private final class CustomPanelCleanupHandler implements AttachEvent.Handler {

        private final WorkbenchPanelPresenter panelPresenter;
        private boolean detaching;

        private CustomPanelCleanupHandler(WorkbenchPanelPresenter panelPresenter) {
            this.panelPresenter = panelPresenter;
        }

        @Override
        public void onAttachOrDetach(AttachEvent event) {
            if (event.isAttached()) {
                return;
            }
            if (!detaching && mapPanelDefinitionToPresenter.containsKey(panelPresenter.getDefinition())) {
                detaching = true;
                Scheduler.get().scheduleFinally(() -> {
                    try {
                        placeManager.closePlace(panelPresenter.getDefinition().getPlace(), null);
                        // in many cases, the panel will have cleaned itself up when we closed its last part in the loop above.
                        // for other custom panel use cases, the panel may still be open. we can do the cleanup here.
                        if (mapPanelDefinitionToPresenter.containsKey(panelPresenter.getDefinition())) {
                            removeWorkbenchPanel(panelPresenter.getDefinition());
                        }
                    } finally {
                        detaching = false;
                    }
                });
            }
        }
    }
}
