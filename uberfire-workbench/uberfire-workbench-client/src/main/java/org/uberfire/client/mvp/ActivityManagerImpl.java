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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.util.GWTEditorNativeRegister;
import org.uberfire.mvp.PlaceRequest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@ApplicationScoped
@EnabledByProperty(value = "uberfire.plugin.mode.active", negated = true)
public class ActivityManagerImpl implements ActivityManager {

    private final Map<Activity, PlaceRequest> startedActivities = new IdentityHashMap<>();
    private final Map<String, SyncBeanDef<Activity>> activitiesById = new HashMap<>();

    @Inject
    private SyncBeanManager iocManager;
    @Inject
    private GWTEditorNativeRegister gwtEditorNativeRegister;

    @PostConstruct
    void init() {
        gwtEditorNativeRegister.nativeRegisterGwtEditorProvider();

        final Collection<SyncBeanDef<Activity>> availableActivities = new ArrayList<>();
        for (SyncBeanDef<Activity> bean : iocManager.lookupBeans(Activity.class)) {
            if (bean.isActivated()) {
                availableActivities.add(bean);
            }
        }

        for (final SyncBeanDef<Activity> activityBean : availableActivities) {

            final String id = activityBean.getName();

            if (activitiesById.containsKey(id)) {
                throw new RuntimeException("Conflict detected: Activity already exists with id " + id);
            }

            activitiesById.put(id, activityBean);

            if (activityBean.getInstance() instanceof EditorActivity) {
                gwtEditorNativeRegister.nativeRegisterGwtClientBean(id, activityBean);
            }
        }
    }

    @Override
    public Set<Activity> getActivities(final PlaceRequest placeRequest) {
        final Collection<SyncBeanDef<Activity>> beans;
        beans = resolveById(placeRequest.getIdentifier());
        return startIfNecessary(getActivitiesFromBeans(beans),
                                placeRequest);
    }

    @Override
    public void destroyActivity(final Activity activity) {
        if (startedActivities.remove(activity) != null) {
            if (getBeanScope(activity) == Dependent.class) {
                iocManager.destroyBean(activity);
            }
        } else {
            throw new IllegalStateException("Activity " + activity + " is not currently in the started state");
        }
    }

    private Class<?> getBeanScope(Activity startedActivity) {
        final IOCBeanDef<?> beanDef = activitiesById.get(startedActivity.getPlace().getIdentifier());
        if (beanDef == null) {
            return Dependent.class;
        }
        return beanDef.getScope();
    }

    private Set<Activity> getActivitiesFromBeans(final Collection<SyncBeanDef<Activity>> activityBeans) {
        final HashSet<Activity> activities = new HashSet<>(activityBeans.size());

        for (final SyncBeanDef<Activity> activityBean : activityBeans) {
            if (!activityBean.isActivated()) {
                continue;
            }
            final Activity instance = activityBean.getInstance();
            activities.add(instance);
        }

        return activities;
    }

    private Activity startIfNecessary(Activity activity,
                                      PlaceRequest place) {
        if (activity == null) {
            return null;
        }
        try {
            if (!startedActivities.containsKey(activity)) {
                startedActivities.put(activity,
                                      place);
                activity.onStartup(place);
            }
            return activity;
        } catch (Exception ex) {
            destroyActivity(activity);
            return null;
        }
    }

    private Set<Activity> startIfNecessary(Set<Activity> activities,
                                           PlaceRequest place) {
        Set<Activity> validatedActivities = new HashSet<>();
        for (Activity activity : activities) {
            Activity validated = startIfNecessary(activity,
                                                  place);
            if (validated != null) {
                validatedActivities.add(validated);
            }
        }
        return validatedActivities;
    }

    private Collection<SyncBeanDef<Activity>> resolveById(final String identifier) {
        if (identifier == null) {
            return emptyList();
        }

        SyncBeanDef<Activity> beanDefActivity = activitiesById.get(identifier);
        if (beanDefActivity == null) {
            return emptyList();
        }
        return singletonList(beanDefActivity);
    }
}
