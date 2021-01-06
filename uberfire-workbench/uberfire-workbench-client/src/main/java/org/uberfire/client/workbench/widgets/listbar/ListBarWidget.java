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
package org.uberfire.client.workbench.widgets.listbar;

import org.uberfire.client.workbench.panels.MultiPartWidget;
import org.uberfire.client.workbench.panels.impl.AbstractSimpleWorkbenchPanelView;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelView;

/**
 * API contract for the header widget of panel views that extend {@link AbstractSimpleWorkbenchPanelView} and
 * {@link MultiListWorkbenchPanelView}. Each application needs exactly one implementation of this class at compile time
 * (usually this will come from the view module). The implementing type must be a Dependent-scoped CDI bean.
 */
public interface ListBarWidget extends MultiPartWidget {

    /**
     * Enable support to close parts.
     */
    void enableClosePart();

    /**
     * Disable support to close parts.
     */
    void disableClosePart();
}