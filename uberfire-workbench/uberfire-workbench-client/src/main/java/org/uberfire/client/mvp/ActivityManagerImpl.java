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

package org.uberfire.client.mvp;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.util.JSFunctions;
import org.uberfire.mvp.PlaceRequest;

@ApplicationScoped
public class ActivityManagerImpl implements ActivityManager {

    private final Map<String, Activity> startedActivities = new IdentityHashMap<>();
    private final Map<String, SyncBeanDef<Activity>> activitiesById = new HashMap<>();

    @Inject
    private SyncBeanManager iocManager;

    @PostConstruct
    void init() {
        JSFunctions.nativeRegisterGwtEditorProvider();
        iocManager.lookupBeans(Activity.class)
                .stream()
                .filter(IOCBeanDef::isActivated)
                .forEach(bean -> {
                    final String id = bean.getName();
                    if (activitiesById.containsKey(id)) {
                        throw new RuntimeException("Conflict detected: Activity already exists with id " + id);
                    }
                    activitiesById.put(id, bean);
                    if (bean.isAssignableTo(EditorActivity.class)) {
                        JSFunctions.nativeRegisterGwtClientBean(id, bean);
                    }
                });
    }

    @Override
    public Activity getEditorActivity() {
        final Collection<SyncBeanDef<EditorActivity>> editors = iocManager.lookupBeans(EditorActivity.class);
        if (editors.size() != 1) {
            throw new RuntimeException("There must be exactly one instance of EditorActivity. Found " + editors.size());
        }
        return editors.iterator().next().getInstance();
    }

    @Override
    public Activity getActivity(final PlaceRequest place) {
        final Activity activity = activitiesById.get(place.getIdentifier()).getInstance();
        if (activity == null) {
            throw new RuntimeException("There must be one activity associated with a place request: ." + place);
        }
        return startedActivities.computeIfAbsent(activity.getIdentifier(), a -> {
            activity.onStartup(place);
            return activity;
        });
    }

    @Override
    public void destroyActivity(final Activity activity) {
        if (startedActivities.remove(activity.getIdentifier()) == null) {
            throw new IllegalStateException("Activity " + activity + " is not currently in the started state");
        }
        final SyncBeanDef<Activity> bean = activitiesById.get(activity.getIdentifier());
        if (bean == null || bean.getScope() == Dependent.class) {
            iocManager.destroyBean(activity);
        }
    }
}
