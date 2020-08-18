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
 */

package ${packageName};

import javax.annotation.Generated;
import javax.enterprise.context.Dependent;

import org.uberfire.experimental.client.service.auth.ExperimentalActivityReference;
import org.uberfire.workbench.model.ActivityResourceType;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Dependent
@Generated("org.uberfire.annotations.processors.ExperimentalFeatureProcessor")
public class ${className} implements ExperimentalActivityReference {

    @Override
    public String getActivityTypeName() {
        return "${activityClass}";
    }

    @Override
    public String getActivityId() {
        return "${activityId}";
    }

    @Override
    public String getExperimentalFeatureId() {
        return "${featureId}";
    }

    @Override
    public ActivityResourceType getActivityType() {
        return ActivityResourceType.${activityType};
    }
}
