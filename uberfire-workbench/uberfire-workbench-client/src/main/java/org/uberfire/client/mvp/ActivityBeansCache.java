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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.util.GWTEditorNativeRegister;
import org.uberfire.client.workbench.annotations.AssociatedResources;

/**
 *
 */
@ApplicationScoped
@EnabledByProperty(value = "uberfire.plugin.mode.active", negated = true)
public class ActivityBeansCache {

    /**
     * All active activity beans mapped by their CDI bean name (names are mandatory for activity beans).
     */
    private final Map<String, SyncBeanDef<Activity>> activitiesById = new HashMap<>();
    /**
     * All active Activities that have an {@link AssociatedResources} annotation and are not splash screens.
     */

    private SyncBeanManager iocManager;
    private final List<ActivityAndMetaInfo> resourceActivities = new ArrayList<>();
    private GWTEditorNativeRegister gwtEditorNativeRegister;

    public ActivityBeansCache() {
    }

    @Inject
    public ActivityBeansCache(SyncBeanManager iocManager,
                              GWTEditorNativeRegister gwtEditorNativeRegister) {
        this.iocManager = iocManager;
        this.gwtEditorNativeRegister = gwtEditorNativeRegister;
    }

    @PostConstruct
    void init() {
        registerGwtEditorProvider();

        final Collection<SyncBeanDef<Activity>> availableActivities = getAvailableActivities();

        for (final SyncBeanDef<Activity> activityBean : availableActivities) {

            final String id = activityBean.getName();

            validateUniqueness(id);

            activitiesById.put(id, activityBean);

            if (isClientEditor(activityBean.getQualifiers())) {
                registerGwtClientBean(id, activityBean);
            }
            final List<String> metaInfo = generateActivityMetaInfo(activityBean);
            if (metaInfo != null) {
                addResourceActivity(activityBean,
                                    metaInfo);
            }
        }
    }

    void registerGwtEditorProvider() {
        gwtEditorNativeRegister.nativeRegisterGwtEditorProvider();
    }

    void registerGwtClientBean(final String id, final SyncBeanDef<Activity> activityBean) {
        gwtEditorNativeRegister.nativeRegisterGwtClientBean(id, activityBean);
    }

    private void addResourceActivity(SyncBeanDef<Activity> activityBean,
                                     List<String> metaInfo) {
        ActivityAndMetaInfo activityAndMetaInfo = new ActivityAndMetaInfo(iocManager,
                                                                          activityBean,
                                                                          metaInfo);
        resourceActivities.add(activityAndMetaInfo);
    }

    Collection<SyncBeanDef<Activity>> getAvailableActivities() {
        Collection<SyncBeanDef<Activity>> activeBeans = new ArrayList<>();
        for (SyncBeanDef<Activity> bean : iocManager.lookupBeans(Activity.class)) {
            if (bean.isActivated()) {
                activeBeans.add(bean);
            }
        }
        return activeBeans;
    }

    private boolean isClientEditor(final Set<Annotation> qualifiers) {
        for (final Annotation qualifier : qualifiers) {
            if (qualifier instanceof IsClientEditor) {
                return true;
            }
        }
        return false;
    }

    private void validateUniqueness(final String id) {
        if (activitiesById.containsKey(id)) {
            throw new RuntimeException("Conflict detected: Activity already exists with id " + id);
        }
    }

    public boolean hasActivity(String id) {
        return activitiesById.containsKey(id);
    }

    /**
     * Returns the activity with the given CDI bean name from this cache, or null if there is no such activity or the
     * activity with the given name is not an activated bean.
     * @param id the CDI name of the bean (see {@link Named}), or in the case of runtime plugins, the name the activity
     * was registered under.
     */
    public SyncBeanDef<Activity> getActivity(final String id) {
        if (id == null) {
            return null;
        }
        return activitiesById.get(id);
    }

    /**
     * Returns the activated activity with the highest priority that can handle the given file. Returns null if no
     * activated activity can handle the path.
     * @param path the file to find a path-based activity for (probably a {@link WorkbenchClientEditorActivity}, but this cache
     * makes no guarantees).
     */
    public SyncBeanDef<Activity> getActivity(final Path path) {

        Optional<ActivityAndMetaInfo> optional = resourceActivities.stream()
                .filter(activityAndMetaInfo -> activitySupportsPath(activityAndMetaInfo, path))
                .findAny();

        if (optional.isPresent()) {
            return optional.get().getActivityBean();
        }

        throw new RuntimeException();
    }

    private boolean activitySupportsPath(ActivityAndMetaInfo activity, Path path) {
        // Check if the editor resources types support the given path
        return Stream.of(activity.getResourceTypes())
                .anyMatch(clientResourceType -> clientResourceType.accept(path));
    }

    public List<SyncBeanDef<Activity>> getPerspectiveActivities() {
        List<SyncBeanDef<Activity>> results = new ArrayList<>();
        for (SyncBeanDef<Activity> beanDef : activitiesById.values()) {
            if (beanDef.isAssignableTo(PerspectiveActivity.class)) {
                results.add(beanDef);
            }
        }
        return results;
    }

    List<String> generateActivityMetaInfo(SyncBeanDef<Activity> activityBean) {
        return ActivityMetaInfo.generate(activityBean);
    }
}
