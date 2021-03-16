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
package org.uberfire.client.workbench.panels;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.uberfire.client.workbench.part.WorkbenchPartPresenter;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;

@Dependent
public class WorkbenchPanelPresenterImpl implements WorkbenchPanelPresenter {

    private final WorkbenchPanelView view;
    private PanelDefinition definition;

    @Inject
    public WorkbenchPanelPresenterImpl(final WorkbenchPanelView view) {
        this.view = view;
    }

    @PostConstruct
    void init() {
        getPanelView().init(this);
    }

    @Override
    public PanelDefinition getDefinition() {
        return definition;
    }

    @Override
    public void setDefinition(final PanelDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void addPart(final WorkbenchPartPresenter part) {
        if (definition.getPlace() == null) {
            definition.setPlace(part.getPlace());
        }
        getPanelView().addPart(part.getPartView());
    }

    @Override
    public void removePlace(final PlaceRequest place) {
        view.removePlace(place);
        definition.setPlace(null);
    }

    @Override
    public WorkbenchPanelView getPanelView() {
        return view;
    }

    @Override
    public void onResize(final int width,
                         final int height) {
        if (width != 0) {
            getDefinition().setWidth(width);
        }

        if (height != 0) {
            getDefinition().setHeight(height);
        }
    }
}
