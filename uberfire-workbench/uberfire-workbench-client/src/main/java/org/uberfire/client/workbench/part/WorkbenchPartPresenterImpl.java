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

package org.uberfire.client.workbench.part;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.mvp.PlaceRequest;

@Dependent
public class WorkbenchPartPresenterImpl implements WorkbenchPartPresenter {

    private final WorkbenchPartView view;

    private PlaceRequest place;

    @Inject
    public WorkbenchPartPresenterImpl(final WorkbenchPartView view) {
        this.view = view;
    }

    @PostConstruct
    void init() {
        view.init(this);
    }

    @Override
    public PlaceRequest getPlace() {
        return place;
    }

    @Override
    public void setPlace(final PlaceRequest place) {
        this.place = place;
    }

    @Override
    public WorkbenchPartView getPartView() {
        return view;
    }

    @Override
    public void setWrappedWidget(final IsWidget widget) {
        this.view.setWrappedWidget(widget);
    }
}
