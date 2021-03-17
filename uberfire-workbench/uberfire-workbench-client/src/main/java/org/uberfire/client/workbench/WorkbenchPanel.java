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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.util.CSSLocatorsUtils;
import org.uberfire.client.util.Layouts;

@Dependent
public class WorkbenchPanel extends ResizeComposite {

    private final RequiresResizeFocusPanel container = new RequiresResizeFocusPanel();

    @PostConstruct
    void postConstruct() {
        Layouts.setToFillParent(container);
        initWidget(container);
        container.getElement().addClassName(CSSLocatorsUtils.buildLocator("qe", "static-workbench-panel-view"));
    }

    @Override
    public Widget getWidget() {
        return container;
    }

    public void init(final IsWidget widget) {
        final SimpleLayoutPanel panel = new SimpleLayoutPanel();
        final ScrollPanel sp = new ScrollPanel();
        panel.setWidget(sp);
        sp.getElement().getFirstChildElement().setClassName("uf-scroll-panel");
        Layouts.setToFillParent(panel);
        sp.setWidget(widget);
        container.setWidget(panel);
        onResize();
    }

    public void clear() {
        container.clear();
    }
}
