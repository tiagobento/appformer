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

import jsinterop.annotations.JsType;
import org.uberfire.workbench.model.PerspectiveDefinition;

@JsType
public interface PerspectiveActivity extends Activity {

    /**
     * Returns a new copy of the layout (panels and their parts) that should be used if no persisted state is available.
     * Each time this method is called, it must produce a new PerspectiveDefinition. This rule applies whether or not
     * the perspective is transient.
     * @return the perspective layout to use when a previously saved one is not available.
     */
    PerspectiveDefinition getDefaultPerspectiveLayout();

    @Override
    default String getName() {
        return getDefaultPerspectiveLayout().getName();
    }
}
