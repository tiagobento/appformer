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

import org.junit.Before;
import org.junit.Test;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PartDefinition;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PanelDefinitionImplTest {

    private PanelDefinitionImpl panelDefinition;
    private PanelDefinitionImpl otherPanel;
    private PartDefinition part;
    private PlaceRequest placeRequest;
    private PanelDefinitionImpl parent;

    @Before
    public void setUp() throws Exception {
        panelDefinition = new PanelDefinitionImpl("foo1");
        otherPanel = new PanelDefinitionImpl("foo2");
        parent = new PanelDefinitionImpl("foo3");
        placeRequest = mock(PlaceRequest.class);
        part = new PartDefinitionImpl(placeRequest);
    }

    @Test(expected = IllegalStateException.class)
    public void settingTwoDifferentParentsShouldThrowException() throws Exception {
        panelDefinition.setParent(otherPanel);
        panelDefinition.setParent(parent);
    }

    @Test
    public void addPartTest() throws Exception {
        assertFalse(panelDefinition.getPlace().contains(part));
        panelDefinition.setPlace(part);
        assertTrue(panelDefinition.getPlace().contains(part));
    }

    @Test
    public void addPartToADifferentPanelChangePanel() throws Exception {
        assertFalse(panelDefinition.getPlace().contains(part));
        panelDefinition.setPlace(part);
        assertTrue(panelDefinition.getPlace().contains(part));
        otherPanel.setPlace(part);
        assertTrue(otherPanel.getPlace().contains(part));
        assertEquals(otherPanel,
                     part.getParentPanel());
        assertFalse(panelDefinition.getPlace().contains(part));
    }

    @Test
    public void addPartTwiceShouldWork() throws Exception {
        assertFalse(panelDefinition.getPlace().contains(part));
        panelDefinition.setPlace(part);
        panelDefinition.setPlace(part);
        assertTrue(panelDefinition.getPlace().contains(part));
    }

    @Test
    public void partShouldNotBePresentAfterRemoval() throws Exception {
        panelDefinition.setPlace(part);
        assertNotNull(part.getParentPanel());
        assertTrue(panelDefinition.getPlace().contains(part));
        panelDefinition.removePlace(part);
        assertNull(part.getParentPanel());
        assertFalse(panelDefinition.getPlace().contains(part));
    }

    @Test
    public void removeNonexistentPartShouldDoNothingAndReturnFalse() throws Exception {
        boolean result = panelDefinition.removePlace(part);
        assertEquals(false,
                     result);
    }

    @Test
    public void widthShouldNotRevertOnceSet() throws Exception {
        assertNull(panelDefinition.getWidth());
        panelDefinition.setWidth(1234);
        panelDefinition.setWidth(null);
        assertEquals((Integer) 1234,
                     panelDefinition.getWidth());
    }

    @Test
    public void heightShouldNotRevertOnceSet() throws Exception {
        assertNull(panelDefinition.getHeight());
        panelDefinition.setHeight(1234);
        panelDefinition.setHeight(null);
        assertEquals((Integer) 1234,
                     panelDefinition.getHeight());
    }

    @Test
    public void appendChildShouldAddPanelToChildren() {
        panelDefinition.appendChild(otherPanel);
        assertTrue(panelDefinition.getChildren().contains(otherPanel));
        assertEquals(panelDefinition,
                     otherPanel.getParent());
    }

    @Test
    public void appendChildToPanelTwiceShouldWork() {
        panelDefinition.appendChild(otherPanel);
        assertTrue(panelDefinition.getChildren().contains(otherPanel));
        panelDefinition.appendChild(otherPanel);
        assertTrue(panelDefinition.getChildren().contains(otherPanel));
    }
}
