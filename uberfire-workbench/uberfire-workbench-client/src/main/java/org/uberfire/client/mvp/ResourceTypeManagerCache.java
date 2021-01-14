/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.uberfire.client.mvp;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResourceTypeManagerCache {

    private final List<ActivityAndMetaInfo> resourceActivities = new ArrayList<>();

    public ResourceTypeManagerCache() {
        // Empty
    }

    public List<ActivityAndMetaInfo> getResourceActivities() {
        return resourceActivities;
    }

    public void addResourceActivity(ActivityAndMetaInfo activityAndMetaInfo) {
        resourceActivities.add(activityAndMetaInfo);
    }

    public void sortResourceActivitiesByPriority() {
        resourceActivities.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
    }
}
