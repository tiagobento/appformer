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

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.slf4j.Logger;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.resources.WorkbenchResources;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

/**
 * Responsible for bootstrapping the client-side Workbench user interface by coordinating calls to the PanelManager and
 * PlaceManager. Normally this happens automatically with no need for assistance or interference from the application.
 * Thus, applications don't usually need to do anything with the Workbench class directly.
 * <p>
 * <h2>Delaying Workbench Startup</h2>
 * <p>
 * In special cases, applications may wish to delay the startup of the workbench. For example, an application that
 * relies on global variables (also known as singletons or Application Scoped beans) that are initialized based on
 * response data from the server doesn't want UberFire to start initializing its widgets until that server response has
 * come in.
 * <p>
 * To delay startup, add a <i>Startup Blocker</i> before Errai starts calling {@link AfterInitialization} methods. The
 * best place to do this is in the {@link PostConstruct} method of an {@link EntryPoint} bean. You would then remove the
 * startup blocker from within the callback from the server:
 * <p>
 * <pre>
 *   {@code @EntryPoint}
 *   public class MyMutableGlobal() {
 *     {@code @Inject private Workbench workbench;}
 *     {@code @Inject private Caller<MyRemoteService> remoteService;}
 *
 *     // set up by a server call. don't start the app until it's populated!
 *     {@code private MyParams params;}
 *
 *     {@code @PostConstruct}
 *     private void earlyInit() {
 *       workbench.addStartupBlocker(MyMutableGlobal.class);
 *     }
 *
 *     {@code @AfterInitialization}
 *     private void lateInit() {
 *       remoteService.call(new {@code RemoteCallback<MyParams>}{
 *         public void callback(MyParams params) {
 *           MyMutableGlobal.this.params = params;
 *           workbench.removeStartupBlocker(MyMutableGlobal.class);
 *         }
 *       }).fetchParameters();
 *     }
 *   }
 * </pre>
 */
@EntryPoint
@EnabledByProperty(value = "uberfire.plugin.mode.active", negated = true)
public class Workbench {

    @Inject
    LayoutSelection layoutSelection;
    @Inject
    private ActivityBeansCache activityBeansCache;
    @Inject
    private SyncBeanManager iocManager;
    @Inject
    private PlaceManager placeManager;
    private WorkbenchLayout layout;
    @Inject
    private Logger logger;

    @AfterInitialization
    private void afterInit() {
        logger.info("Starting workbench...");

        layout.onBootstrap();
        addLayoutToRootPanel(layout);
        placeManager.goTo(new DefaultPlaceRequest(getHomePerspectiveActivity().getIdentifier()));

        // Resizing the Window should resize everything
        Window.addResizeHandler(event -> layout.resizeTo(event.getWidth(),
                                                         event.getHeight()));

        // Defer the initial resize call until widgets are rendered and sizes are available
        Scheduler.get().scheduleDeferred(() -> layout.onResize());

        notifyJSReady();
    }

    @PostConstruct
    private void earlyInit() {
        layout = layoutSelection.get();
        WorkbenchResources.INSTANCE.CSS().ensureInjected();
    }

    private native void notifyJSReady() /*-{
        if ($wnd.appFormerGwtFinishedLoading) {
            $wnd.appFormerGwtFinishedLoading();
        }
    }-*/;

    public PerspectiveActivity getHomePerspectiveActivity() {
        PerspectiveActivity defaultPerspective = null;
        final Collection<SyncBeanDef<PerspectiveActivity>> perspectives = iocManager.lookupBeans(PerspectiveActivity.class);

        for (final SyncBeanDef<PerspectiveActivity> perspective : perspectives) {
            final PerspectiveActivity instance = perspective.getInstance();
            if (instance.getIdentifier().equals("AuthoringPerspective")) {
                defaultPerspective = instance;
            } else {
                iocManager.destroyBean(instance);
            }
        }
        // The home perspective has always priority over the default
        return defaultPerspective;
    }

    void addLayoutToRootPanel(final WorkbenchLayout layout) {
        RootLayoutPanel.get().add(layout.getRoot());
    }
}
