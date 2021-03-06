/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.security.management.keycloak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.errai.security.shared.api.Group;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.ext.security.management.api.AbstractEntityManager;
import org.uberfire.ext.security.management.api.Capability;
import org.uberfire.ext.security.management.api.CapabilityStatus;
import org.uberfire.ext.security.management.api.exception.GroupNotFoundException;
import org.uberfire.ext.security.management.api.exception.RealmManagementNotAuthorizedException;
import org.uberfire.ext.security.management.api.exception.UnsupportedServiceCapabilityException;
import org.uberfire.ext.security.management.keycloak.client.resource.RealmResource;
import org.uberfire.ext.security.management.keycloak.client.resource.RoleMappingResource;
import org.uberfire.ext.security.management.keycloak.client.resource.RoleResource;
import org.uberfire.ext.security.management.keycloak.client.resource.RoleScopeResource;
import org.uberfire.ext.security.management.keycloak.client.resource.RolesResource;
import org.uberfire.ext.security.management.keycloak.client.resource.UserResource;
import org.uberfire.ext.security.management.util.SecurityManagementUtils;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KeyCloakGroupManagerTest extends DefaultKeyCloakTest {

    private KeyCloakGroupManager groupsManager;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        super.setup();
        initGroupManager();
        doAnswer(invocationOnMock -> {
            ((Consumer<RealmResource>) invocationOnMock.getArguments()[0]).accept(realmResource);
            return null;
        }).when(groupsManager).consumeRealm(any(Consumer.class));
    }

    @Test
    public void testCapabilities() {
        assertEquals(groupsManager.getCapabilityStatus(Capability.CAN_SEARCH_GROUPS),
                     CapabilityStatus.ENABLED);
        assertEquals(groupsManager.getCapabilityStatus(Capability.CAN_READ_GROUP),
                     CapabilityStatus.ENABLED);
        assertEquals(groupsManager.getCapabilityStatus(Capability.CAN_ADD_GROUP),
                     CapabilityStatus.ENABLED);
        assertEquals(groupsManager.getCapabilityStatus(Capability.CAN_DELETE_GROUP),
                     CapabilityStatus.ENABLED);
        assertEquals(groupsManager.getCapabilityStatus(Capability.CAN_UPDATE_GROUP),
                     CapabilityStatus.UNSUPPORTED);
    }

    @Test
    public void testAllowsEmpty() {
        assertTrue(groupsManager.getSettings().allowEmpty());
    }

    @Test(expected = RealmManagementNotAuthorizedException.class)
    public void testGroupNotAuthorized() throws Exception {
        initGroupManager();
        doThrow(mockForbiddenResponse()).when(keycloakMock).realm();
        groupsManager.get(ROLE);
    }

    @Test
    public void testGetGroup5() {
        String name = ROLE + 5;
        Group group = groupsManager.get(name);
        assertGroup(group,
                    name);
    }
    @Test(expected = GroupNotFoundException.class)
    public void testGetGroup200() {
        String name = ROLE + 200;
        Group group = groupsManager.get(name);
    }

    @Test
    public void testGetAllGroups() {
        List<Group> groups = groupsManager.getAll();
        assertEquals(52, roleResources.size());
        assertEquals(50, groups.size());
    }

    @Test(expected = RuntimeException.class)
    public void testSearchPageZero() {
        AbstractEntityManager.SearchRequest request = buildSearchRequestMock("",
                                                                             0,
                                                                             5);
        AbstractEntityManager.SearchResponse<Group> response = groupsManager.search(request);
    }

    @Test
    public void testSearchAllFirstPage() {
        AbstractEntityManager.SearchRequest request = buildSearchRequestMock("",
                                                                             1,
                                                                             5);
        AbstractEntityManager.SearchResponse<Group> response = groupsManager.search(request);
        assertNotNull(response);
        List<Group> groups = response.getResults();
        int total = response.getTotal();
        boolean hasNextPage = response.hasNextPage();
        assertEquals(total,
                     rolesCount);
        assertTrue(hasNextPage);
        assertEquals(groups.size(),
                     5);
        Group group0 = groups.get(0);
        assertGroup(group0,
                    ROLE + 0);
        Group group4 = groups.get(4);
        assertGroup(group4,
                    ROLE + 4);
    }

    @Test
    public void testSearchAllSecondPage() {
        AbstractEntityManager.SearchRequest request = buildSearchRequestMock("",
                                                                             2,
                                                                             5);
        AbstractEntityManager.SearchResponse<Group> response = groupsManager.search(request);
        assertNotNull(response);
        List<Group> groups = response.getResults();
        int total = response.getTotal();
        boolean hasNextPage = response.hasNextPage();
        assertEquals(total,
                     rolesCount);
        assertTrue(hasNextPage);
        assertEquals(groups.size(),
                     5);
        Group group5 = groups.get(0);
        assertGroup(group5,
                    ROLE + 5);
        Group group9 = groups.get(4);
        assertGroup(group9,
                    ROLE + 9);
    }

    @Test
    public void testSearchAllLastPage() {
        AbstractEntityManager.SearchRequest request = buildSearchRequestMock("",
                                                                             10,
                                                                             5);
        AbstractEntityManager.SearchResponse<Group> response = groupsManager.search(request);
        assertNotNull(response);
        List<Group> groups = response.getResults();
        int total = response.getTotal();
        boolean hasNextPage = response.hasNextPage();
        assertEquals(total,
                     rolesCount);

        assertTrue(!hasNextPage);
        assertEquals(groups.size(),
                     5);
        Group group45 = groups.get(0);
        assertGroup(group45,
                    ROLE + 45);
        Group group49 = groups.get(4);
        assertGroup(group49,
                    ROLE + 49);
    }

    @Test
    public void testCreateGroup() {
        String groupName = "newgroup";
        Group newgroup = groupsManager.create(SecurityManagementUtils.createGroup("newgroup"));
        assertGroup(newgroup, groupName);
    }

    @Test(expected = UnsupportedServiceCapabilityException.class)
    public void testUpdateGroup() {
        groupsManager.update(SecurityManagementUtils.createGroup("id1"));
    }

    @Test
    public void testDeleteGroup() {
        RoleResource role0Resource = roleResources.get(0);
        groupsManager.delete("role0");
        verify(role0Resource,
               times(1)).remove();
    }

    @Test
    public void testAssignUsers() {
        final Collection<String> users = new ArrayList<String>();
        users.add("user0");
        UserResource user0Resource = userResources.get(0);
        RoleMappingResource roleMappingResource = user0Resource.roles();
        RoleScopeResource roleScopeResource = roleMappingResource.realmLevel();
        groupsManager.assignUsers("role0",
                                  users);
        ArgumentCaptor<List> rolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleScopeResource,
               times(1)).add(rolesCaptor.capture());
        List rolesAdded = rolesCaptor.getValue();
        assertEquals(1,
                     rolesAdded.size());
    }

    @Test
    public void testGetClientById() {
        String clientId = groupsManager.getClientIdByName(realmResource);
        assertEquals("clientId", clientId);
    }

    @Test
    public void testGetRoleResource() {
        RolesResource rolesResource = groupsManager.getRolesResource(realmResource, true);
        assertEquals(52, rolesResource.list().size());
    }

    @Test
    public void testGetClientRoles() {
        String name = ROLE + 5;
        when(groupsManager.getRolesResource(realmResource, true)).thenReturn(rolesResource);
        Group group = groupsManager.get(name);
        assertGroup(group,
                    name);
    }

    private void assertGroup(Group group,
                             String name) {
        assertNotNull(group);
        assertEquals(name,
                     group.getName());
    }

    private void initGroupManager() throws Exception {
        groupsManager = spy(new KeyCloakGroupManager());
        doReturn(keycloakMock).when(groupsManager).getKeyCloakInstance();
        groupsManager.initialize(userSystemManager);
    }
}
