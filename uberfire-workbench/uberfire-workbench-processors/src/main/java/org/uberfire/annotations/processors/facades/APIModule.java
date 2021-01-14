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

package org.uberfire.annotations.processors.facades;

/**
 * A collection of type names in the UberFire API module.
 * Due to a bug in Eclipse annotation processor dependencies, we refer to all UberFire type names using Strings,
 * Elements, and TypeMirrors. We cannot refer to the annotation types as types themselves.
 */
public class APIModule {

    public static final String position = "org.uberfire.workbench.model.Position";
    public static final String placeRequest = "org.uberfire.mvp.PlaceRequest";
    public static final String setContent = "org.uberfire.lifecycle.SetContent";
    public static final String getContent = "org.uberfire.lifecycle.GetContent";
    public static final String getPreview = "org.uberfire.lifecycle.GetPreview";
    public static final String onClose = "org.uberfire.lifecycle.OnClose";
    public static final String onFocus = "org.uberfire.lifecycle.OnFocus";
    public static final String onLostFocus = "org.uberfire.lifecycle.OnLostFocus";
    public static final String onOpen = "org.uberfire.lifecycle.OnOpen";
    public static final String onShutdown = "org.uberfire.lifecycle.OnShutdown";
    public static final String onStartup = "org.uberfire.lifecycle.OnStartup";
    public static final String activatedBy = "org.jboss.errai.ioc.client.api.ActivatedBy";

    private APIModule() {}

    public static String getPositionClass() {
        return position;
    }

    public static String getPlaceRequestClass() {
        return placeRequest;
    }

    public static String getSetContentClass() {
        return setContent;
    }

    public static String getGetContentClass() {
        return getContent;
    }

    public static String getGetPreviewClass() {
        return getPreview;
    }

    public static String getOnCloseClass() {
        return onClose;
    }

    public static String getOnShutdownClass() {
        return onShutdown;
    }

    public static String getOnFocusClass() {
        return onFocus;
    }

    public static String getOnLostFocusClass() {
        return onLostFocus;
    }

    public static String getOnStartupClass() {
        return onStartup;
    }

    public static String getOnOpenClass() {
        return onOpen;
    }
}
