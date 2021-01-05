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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.util.Layouts;
import org.uberfire.client.workbench.docks.UberfireDocksContainer;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchDragAndDropManager;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchPickupDragController;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.PerspectiveDefinition;

/**
 * The default layout implementation.
 */
@ApplicationScoped
public class WorkbenchLayoutImpl implements WorkbenchLayout {

    public static final String UF_MAXIMIZED_PANEL = "uf-maximized-panel";

    public static final String UF_ROOT_CSS_CLASS = "uf-workbench-layout";
    /**
     * Holder for style information that was modified in order to maximize a panel.
     */

    private static final int MAXIMIZED_PANEL_Z_INDEX = 100;

    /**
     * Dock Layout panel: in center root perspective and also (if available) with east west south docks
     */
    private final DockLayoutPanel rootContainer = new DockLayoutPanel(Unit.PX);
    /**
     * The panel within which the current perspective's root view resides. This panel lasts the lifetime of the app; it's
     * cleared and repopulated with the new perspective's root view each time
     * {@link org.uberfire.client.workbench.PanelManager#setPerspective(PerspectiveDefinition)} gets called.
     */
    private final SimpleLayoutPanel perspectiveRootContainer = new SimpleLayoutPanel();
    private final Map<Widget, OriginalStyleInfo> maximizedWidgetOriginalStyles = new HashMap<Widget, OriginalStyleInfo>();
    private SyncBeanManager iocManager;
    /**
     * Top-level widget of the whole workbench layout. This panel contains the nested container panels for headers,
     * footers, and the current perspective. During a normal startup of UberFire, this panel would be added directly to
     * the RootLayoutPanel.
     */
    private HeaderPanel root;
    private WorkbenchDragAndDropManager dndManager;
    /**
     * An abstraction for DockLayoutPanel used by Uberfire Docks.
     */
    private UberfireDocksContainer uberfireDocksContainer;
    /**
     * We read the drag boundary panel out of this, and sandwich it between the root panel and the perspective container panel.
     */
    private WorkbenchPickupDragController dragController;

    public WorkbenchLayoutImpl() {

    }

    @Inject
    public WorkbenchLayoutImpl(SyncBeanManager iocManager,
                               HeaderPanel root,
                               WorkbenchDragAndDropManager dndManager,
                               UberfireDocksContainer uberfireDocksContainer,
                               WorkbenchPickupDragController dragController) {

        this.iocManager = iocManager;
        this.root = root;
        this.dndManager = dndManager;
        this.uberfireDocksContainer = uberfireDocksContainer;
        this.dragController = dragController;
    }

    @PostConstruct
    private void init() {
        perspectiveRootContainer.ensureDebugId("perspectiveRootContainer");
        dragController.getBoundaryPanel().ensureDebugId("workbenchDragBoundary");
        root.addStyleName(UF_ROOT_CSS_CLASS);
    }

    @Override
    public HeaderPanel getRoot() {
        return root;
    }

    @Override
    public HasWidgets getPerspectiveContainer() {
        return perspectiveRootContainer;
    }

    @Override
    public void onBootstrap() {
        dndManager.unregisterDropControllers();

        AbsolutePanel dragBoundary = dragController.getBoundaryPanel();
        dragBoundary.add(perspectiveRootContainer);

        setupDocksContainer();
        rootContainer.add(dragBoundary);

        Layouts.setToFillParent(perspectiveRootContainer);
        Layouts.setToFillParent(dragBoundary);
        Layouts.setToFillParent(rootContainer);

        root.setContentWidget(rootContainer);
    }

    private void setupDocksContainer() {
        uberfireDocksContainer.setup(rootContainer,
                                     () -> Scheduler.get().scheduleDeferred(() -> onResize()));
    }

    @Override
    public void onResize() {
        resizeTo(Window.getClientWidth(),
                 Window.getClientHeight());
    }

    @Override
    public void resizeTo(int width,
                         int height) {
        root.setPixelSize(width,
                          height);

        // The dragBoundary can't be a LayoutPanel, so it doesn't support ProvidesResize/RequiresResize.
        // We start the cascade of onResize() calls at its immediate child.
        perspectiveRootContainer.onResize();

        new Timer() {
            @Override
            public void run() {
                updateMaximizedPanelSizes();
            }
        }.schedule(5);
    }

    private void updateMaximizedPanelSizes() {
        for (Widget w : maximizedWidgetOriginalStyles.keySet()) {
            Style style = w.getElement().getStyle();
            style.setTop(perspectiveRootContainer.getAbsoluteTop(),
                         Unit.PX);
            style.setLeft(perspectiveRootContainer.getAbsoluteLeft(),
                          Unit.PX);
            style.setWidth(perspectiveRootContainer.getOffsetWidth(),
                           Unit.PX);
            style.setHeight(perspectiveRootContainer.getOffsetHeight(),
                            Unit.PX);

            if (w instanceof RequiresResize) {
                ((RequiresResize) w).onResize();
            }
        }
    }

    protected static abstract class AbstractResizeAnimation extends Animation {

        protected final Style style;
        protected final Widget w;
        protected final Map<Widget, OriginalStyleInfo> maximizedWidgetOriginalStyles;
        protected final Command onCompleteCallback;

        public AbstractResizeAnimation(final Widget w,
                                       final Map<Widget, OriginalStyleInfo> maximizedWidgetOriginalStyles,
                                       final Command onCompleteCallback) {
            this.w = w;
            this.maximizedWidgetOriginalStyles = maximizedWidgetOriginalStyles;
            this.onCompleteCallback = onCompleteCallback;
            style = w.getElement().getStyle();
        }

        @Override
        protected void onUpdate(double progress) {
            final double width = newTarget(w.getElement().getClientWidth(),
                                           getTargetWidth(),
                                           progress);
            style.setWidth(width,
                           Unit.PX);
            final double height = newTarget(w.getElement().getClientHeight(),
                                            getTargetHeight(),
                                            progress);
            style.setHeight(height,
                            Unit.PX);
            final double top = newTarget(w.getAbsoluteTop(),
                                         getTargetTop(),
                                         progress);
            style.setTop(top,
                         Unit.PX);
            final double left = newTarget(w.getAbsoluteLeft(),
                                          getTargetLeft(),
                                          progress);
            style.setLeft(left,
                          Unit.PX);
        }

        public abstract int getTargetWidth();

        public abstract int getTargetHeight();

        public abstract int getTargetTop();

        public abstract int getTargetLeft();

        public void run() {
            super.run(1000);
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            if (onCompleteCallback != null) {
                onCompleteCallback.execute();
            }
        }

        private double newTarget(int current,
                                 int target,
                                 double progress) {
            return Math.round(current + ((target - current) * progress));
        }

        public void onResize() {
            if (w instanceof RequiresResize) {
                ((RequiresResize) w).onResize();
            }
        }
    }

    protected static class ExpandAnimation extends AbstractResizeAnimation {

        protected final SimpleLayoutPanel perspectiveRootContainer;

        public ExpandAnimation(
                final Widget w,
                final Map<Widget, OriginalStyleInfo> maximizedWidgetOriginalStyles,
                final SimpleLayoutPanel perspectiveRootContainer,
                final Command onCompleteCallback) {
            super(w,
                  maximizedWidgetOriginalStyles,
                  onCompleteCallback);
            this.perspectiveRootContainer = perspectiveRootContainer;
        }

        @Override
        protected void onStart() {
            maximizedWidgetOriginalStyles.put(w,
                                              new OriginalStyleInfo(w));
            style.setZIndex(MAXIMIZED_PANEL_Z_INDEX);
            style.setHeight(w.getElement().getClientHeight(),
                            Unit.PX);
            style.setWidth(w.getElement().getClientWidth(),
                           Unit.PX);
            style.setTop(w.getAbsoluteTop(),
                         Unit.PX);
            style.setLeft(w.getAbsoluteLeft(),
                          Unit.PX);
            style.setPosition(Position.FIXED);
        }

        @Override
        public int getTargetWidth() {
            return perspectiveRootContainer.getOffsetWidth();
        }

        @Override
        public int getTargetHeight() {
            return perspectiveRootContainer.getOffsetHeight();
        }

        @Override
        public int getTargetTop() {
            return perspectiveRootContainer.getAbsoluteTop();
        }

        @Override
        public int getTargetLeft() {
            return perspectiveRootContainer.getAbsoluteLeft();
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            onResize();
        }
    }

    protected static class CollapseAnimation extends AbstractResizeAnimation {

        private final OriginalStyleInfo originalStyleInfo;

        public CollapseAnimation(final Widget w,
                                 final Map<Widget, OriginalStyleInfo> maximizedWidgetOriginalStyles,
                                 final Command onCompleteCallback) {
            super(w,
                  maximizedWidgetOriginalStyles,
                  onCompleteCallback);
            originalStyleInfo = maximizedWidgetOriginalStyles.remove(w);
        }

        @Override
        public int getTargetWidth() {
            return originalStyleInfo.getClientWidth();
        }

        @Override
        public int getTargetHeight() {
            return originalStyleInfo.getClientHeight();
        }

        @Override
        public int getTargetTop() {
            return originalStyleInfo.getAbsoluteTop();
        }

        @Override
        public int getTargetLeft() {
            return originalStyleInfo.getAbsoluteLeft();
        }

        @Override
        protected void onComplete() {
            originalStyleInfo.restore(w);
            onResize();
            if (onCompleteCallback != null) {
                onCompleteCallback.execute();
            }
        }
    }

    /**
     * Holder for style information that was modified in order to maximize a panel.
     */
    protected static class OriginalStyleInfo {

        private String position;
        private String top;
        private String left;
        private String width;
        private String height;
        private String zIndex;
        private int absoluteTop;
        private int absoluteLeft;
        private int clientHeight;
        private int clientWidth;

        public OriginalStyleInfo(final Widget w) {
            absoluteLeft = w.getAbsoluteLeft();
            absoluteTop = w.getAbsoluteTop();
            clientHeight = w.getElement().getClientHeight();
            clientWidth = w.getElement().getClientWidth();

            final Style style = w.getElement().getStyle();
            position = style.getPosition();
            top = style.getTop();
            left = style.getLeft();
            width = style.getWidth();
            height = style.getHeight();
            zIndex = style.getZIndex();
        }

        /**
         * Restores to {@code w} all style values to those most recently set on this instance.
         * @param w the widget to restore styles on.
         */
        public void restore(final Widget w) {
            final Style style = w.getElement().getStyle();
            style.setProperty("position",
                              position);
            style.setProperty("top",
                              top);
            style.setProperty("left",
                              left);
            style.setProperty("width",
                              width);
            style.setProperty("height",
                              height);
            style.setProperty("zIndex",
                              zIndex);
        }

        public int getAbsoluteTop() {
            return absoluteTop;
        }

        public int getAbsoluteLeft() {
            return absoluteLeft;
        }

        public int getClientHeight() {
            return clientHeight;
        }

        public int getClientWidth() {
            return clientWidth;
        }
    }
}
