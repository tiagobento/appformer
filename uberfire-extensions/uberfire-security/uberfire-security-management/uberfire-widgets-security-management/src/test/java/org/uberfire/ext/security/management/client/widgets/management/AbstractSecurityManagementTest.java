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

package org.uberfire.ext.security.management.client.widgets.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.security.management.api.Capability;
import org.uberfire.ext.security.management.api.CapabilityStatus;
import org.uberfire.ext.security.management.api.UserManager;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.api.service.RoleManagerService;
import org.uberfire.ext.security.management.api.service.UserManagerService;
import org.uberfire.ext.security.management.client.ClientSecurityExceptionMessageResolver;
import org.uberfire.ext.security.management.client.ClientUserSystemManager;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.workbench.events.NotificationEvent;

import static org.mockito.Mockito.*;

public abstract class AbstractSecurityManagementTest {

    protected static final String ATTRIBUTE_USER_ID = "user.id";
    protected static final String ATTRIBUTE_USER_FIRST_NAME = "user.firstName";
    protected static final String ATTRIBUTE_USER_LAST_NAME = "user.lastName";
    protected static final String ATTRIBUTE_USER_ENABLED = "user.enabled";
    protected static final String ATTRIBUTE_USER_EMAIL = "user.email";

    @Mock
    protected EventSourceMock<NotificationEvent> workbenchNotification;
    @Mock
    protected UserManagerService userManagerService;
    @Mock
    protected GroupManagerService groupsManagerService;
    @Mock
    protected RoleManagerService rolesManagerService;
    @Mock
    protected ErrorPopupPresenter errorPopupPresenter;
    @Mock
    protected ClientSecurityExceptionMessageResolver exceptionMessageResolver;
    protected Caller<UserManagerService> usersManagerServiceCaller;
    protected Caller<GroupManagerService> groupsManagerServiceCaller;
    protected Caller<RoleManagerService> rolesManagerServiceCaller;
    protected ClientUserSystemManager userSystemManager;

    protected void setup() {
        MockitoAnnotations.initMocks(this);
        usersManagerServiceCaller = new CallerMock<UserManagerService>(userManagerService);
        groupsManagerServiceCaller = new CallerMock<GroupManagerService>(groupsManagerService);
        rolesManagerServiceCaller = new CallerMock<RoleManagerService>(rolesManagerService);
        userSystemManager = spy(new ClientUserSystemManager(usersManagerServiceCaller,
                                                            groupsManagerServiceCaller,
                                                            rolesManagerServiceCaller,
                                                            exceptionMessageResolver,
                                                            errorPopupPresenter));

        // final Map<Capability, CapabilityStatus> usersCapabilities = getUserCapabilities();
        // final Map<Capability, CapabilityStatus> groupsCapabilities = getGroupCapabilities();
        final Collection<UserManager.UserAttribute> userAttributes = getUserAttributes();
    }

    protected Map<Capability, CapabilityStatus> getUserCapabilities() {
        Map<Capability, CapabilityStatus> capabilities = new HashMap<Capability, CapabilityStatus>();
        capabilities.put(Capability.CAN_SEARCH_USERS,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_ADD_USER,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_UPDATE_USER,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_DELETE_USER,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_READ_USER,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_MANAGE_ATTRIBUTES,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_ASSIGN_GROUPS,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_CHANGE_PASSWORD,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_ASSIGN_ROLES,
                         CapabilityStatus.UNSUPPORTED);
        return capabilities;
    }

    protected Map<Capability, CapabilityStatus> getGroupCapabilities() {
        Map<Capability, CapabilityStatus> capabilities = new HashMap<Capability, CapabilityStatus>();
        capabilities.put(Capability.CAN_SEARCH_GROUPS,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_ADD_GROUP,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_UPDATE_GROUP,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_DELETE_GROUP,
                         CapabilityStatus.ENABLED);
        capabilities.put(Capability.CAN_READ_GROUP,
                         CapabilityStatus.ENABLED);
        return capabilities;
    }

    protected Collection<UserManager.UserAttribute> getUserAttributes() {
        Collection<UserManager.UserAttribute> attributes = new ArrayList<UserManager.UserAttribute>();

        final UserManager.UserAttribute USER_ID = mock(UserManager.UserAttribute.class);
        attributes.add(USER_ID);

        final UserManager.UserAttribute USER_FIST_NAME = mock(UserManager.UserAttribute.class);
        attributes.add(USER_FIST_NAME);

        final UserManager.UserAttribute USER_LAST_NAME = mock(UserManager.UserAttribute.class);
        attributes.add(USER_LAST_NAME);

        final UserManager.UserAttribute USER_ENABLED = mock(UserManager.UserAttribute.class);
        attributes.add(USER_ENABLED);

        final UserManager.UserAttribute USER_EMAIL = mock(UserManager.UserAttribute.class);
        attributes.add(USER_EMAIL);

        return attributes;
    }

    protected List<User> buildUsersList(final int size) {
        final List<User> users = new ArrayList<User>();
        for (int x = 0; x < size; x++) {
            final User user = mockUser(getUserIdentifier(x));
            when(user.getIdentifier()).thenReturn(getUserIdentifier(x));
            users.add(user);
        }
        return users;
    }

    protected List<Group> buildGroupsList(final int size) {
        final List<Group> groups = new ArrayList<Group>();
        for (int x = 0; x < size; x++) {
            final Group g = mock(Group.class);
            when(g.getName()).thenReturn(getGroupIdentifier(x));
            groups.add(g);
        }
        return groups;
    }

    protected List<Role> buildRolesList(final int size) {
        final List<Role> groups = new ArrayList<Role>();
        for (int x = 0; x < size; x++) {
            final Role g = mock(Role.class);
            when(g.getName()).thenReturn(getRoleIdentifier(x));
            groups.add(g);
        }
        return groups;
    }

    protected Collection<String> buildGroupIdsList(final int size) {
        final List<String> groups = new ArrayList<String>();
        for (int x = 0; x < size; x++) {
            groups.add(getGroupIdentifier(x));
        }
        return groups;
    }

    public User mockUser(final String id) {
        final User user = mock(User.class);
        when(user.getIdentifier()).thenReturn(id);
        return user;
    }

    public Group mockGroup(final String name) {
        final Group g = mock(Group.class);
        when(g.getName()).thenReturn(name);
        return g;
    }

    protected String getUserIdentifier(final int x) {
        return "user" + x;
    }

    protected String getGroupIdentifier(final int x) {
        return "group" + x;
    }

    protected String getRoleIdentifier(final int x) {
        return "role" + x;
    }
}
