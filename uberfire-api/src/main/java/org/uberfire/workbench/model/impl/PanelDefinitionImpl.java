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
package org.uberfire.workbench.model.impl;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PanelDefinition;

/**
 * Default implementation of PanelDefinition
 */
@Portable
@JsType
public class PanelDefinitionImpl implements PanelDefinition {

    private PlaceRequest place;
    private Integer height = null;
    private Integer width = null;
    private boolean isRoot;


    @JsIgnore
    public PanelDefinitionImpl() {
    }

    @Override
    public void setPlace(final PlaceRequest place) {
        this.place = place;
    }

    @Override
    @JsIgnore
    public PlaceRequest getPlace() {
        return place;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    @Override
    @JsIgnore
    public Integer getHeight() {
        return height;
    }

    @Override
    @JsIgnore
    public void setHeight(Integer height) {
        if (height != null) {
            this.height = height;
        }
    }

    @Override
    @JsIgnore
    public Integer getWidth() {
        return width;
    }

    @Override
    @JsIgnore
    public void setWidth(Integer width) {
        if (width != null) {
            this.width = width;
        }
    }

    @Override
    public String toString() {
        String fullName = getClass().getName();
        String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);
        return simpleName + " [place=" + place + "]";
    }
}
