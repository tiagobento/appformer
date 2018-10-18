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

package org.uberfire.jsbridge.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.IsWidget;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Any;
import jsinterop.base.Js;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.marshalling.client.Marshalling;
import org.uberfire.client.mvp.AbstractWorkbenchScreenActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsWorkbenchScreenActivity extends AbstractWorkbenchScreenActivity {

    private InvocationPostponer invocationsPostponer;

    private PlaceRequest place;
    private JsNativeScreen screen;
    private List<Subscription> subscriptions;

    public JsWorkbenchScreenActivity(final JsNativeScreen screen,
                                     final PlaceManager placeManager) {

        super(placeManager);
        this.screen = screen;
        this.subscriptions = new ArrayList<>();
        this.invocationsPostponer = new InvocationPostponer();
    }

    public void updateRealContent(final JavaScriptObject jsObject) {
        this.screen.updateRealContent(jsObject);
        this.invocationsPostponer.executeAll();
    }

    //
    //
    //LIFECYCLE

    @Override
    public void onStartup(final PlaceRequest place) {

        this.place = place;

        if (!this.screen.scriptLoaded()) {
            this.invocationsPostponer.postpone(() -> this.onStartup(place));
            return;
        }

        this.registerSubscriptions();
        screen.run("af_onStartup", JsPlaceRequest.fromPlaceRequest(place));
    }

    @Override
    public void onOpen() {

        // render no matter if the script was loaded or not, even if the call results in a blank screen being rendered.
        screen.render();

        if (!this.screen.scriptLoaded()) {
            this.invocationsPostponer.postpone(this::onOpen);
            return;
        }

        screen.run("af_onOpen");
        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {

        if (this.screen.scriptLoaded()) {
            screen.run("af_onClose");
        }

        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public boolean onMayClose() {

        if (this.screen.scriptLoaded()) {
            return !screen.defines("af_onMayClose") || (boolean) screen.run("af_onMayClose");
        }

        return true;
    }

    @Override
    public void onShutdown() {

        this.invocationsPostponer.clear();

        if (this.screen.scriptLoaded()) {
            this.unsubscribeFromAllEvents();
            screen.run("af_onShutdown");
        }
    }

    @Override
    public void onFocus() {
        if (this.screen.scriptLoaded()) {
            screen.run("af_onFocus");
        }
    }

    @Override
    public void onLostFocus() {
        if (this.screen.scriptLoaded()) {
            screen.run("af_onLostFocus");
        }
    }

    // PROPERTIES
    @Override
    public String getTitle() {
        return (String) screen.get("componentTitle");
    }

    @Override
    public Position getDefaultPosition() {
        return CompassPosition.ROOT;
    }

    @Override
    public PlaceRequest getPlace() {
        return place;
    }

    @Override
    public String getIdentifier() {
        return (String) screen.get("componentId");
    }

    @Override
    public IsWidget getTitleDecoration() {
        return null;
    }

    @Override
    public Menus getMenus() {
        return null;
    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }

    @Override
    public PlaceRequest getOwningPlace() {
        return null;
    }

    @Override
    public IsWidget getWidget() {
        return ElementWrapperWidget.getWidget(screen.getElement());
    }

    @Override
    public String contextId() {
        return (String) screen.get("componentContextId");
    }

    @Override
    public int preferredHeight() {
        return -1;
    }

    @Override
    public int preferredWidth() {
        return -1;
    }

    //
    //
    //CDI Events Subscriptions

    private void registerSubscriptions() {
        DomGlobal.console.info("Registering event subscriptions for " + this.getIdentifier() + "...");
        final JsObject subscriptions = (JsObject) this.screen.get("subscriptions");
        for (final String eventFqcn : JsObject.keys(subscriptions)) {
            if (subscriptions.hasOwnProperty(eventFqcn)) {
                final Any jsObject = Js.uncheckedCast(subscriptions);
                final Object callback = jsObject.asPropertyMap().get(eventFqcn);

                //TODO: Parent classes of "eventFqcn" should be subscribed to as well?
                //FIXME: Marshall/unmarshall is happening twice

                final Subscription subscription = CDI.subscribe(eventFqcn, new AbstractCDIEventCallback<Object>() {
                    public void fireEvent(final Object event) {
                        AppFormerJsBridge.callNative(callback, Marshalling.toJSON(event));
                    }
                });

                //Subscribes to client-sent events.
                this.subscriptions.add(subscription);

                //TODO: Handle local-only events
                //Forwards server-sent events to the local subscription.
                ErraiBus.get().subscribe("cdi.event:" + eventFqcn, CDI.ROUTING_CALLBACK);
            }
        }
    }

    private void unsubscribeFromAllEvents() {
        DomGlobal.console.info("Removing event subscriptions for " + this.getIdentifier() + "...");
        this.subscriptions.forEach(Subscription::remove);
        this.subscriptions = new ArrayList<>();
    }

    private class InvocationPostponer {

        private final Stack<Runnable> invocations;

        public InvocationPostponer() {
            this.invocations = new Stack<>();
        }

        public void postpone(final Runnable invocation) {
            this.invocations.push(invocation);
        }

        public void executeAll() {
            while (!this.invocations.isEmpty()) {
                this.invocations.pop().run();
            }
        }

        public void clear() {
            this.invocations.clear();
        }
    }
}
