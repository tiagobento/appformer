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

import org.uberfire.workbench.events.UberFireEvent;

/**
 * CDI event fired by the framework each time an Activity lifecycle method throws an exception. Observers of the event
 * can use its methods to get information about the lifecycle call that failed, and can also ask the framework to
 * suppress the default error message.
 */
public class ActivityLifecycleError implements UberFireEvent {

    private final Throwable exception;

    ActivityLifecycleError(Throwable exception) {
        this.exception = exception;
    }

    /**
     * Returns the exception thrown by the failed lifecycle method, if the failure was due to a thrown exception.
     *
     * @return the exception thrown by the failed lifecycle method. May be null.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * The different activity lifecycle calls that can fail.
     */
    public enum LifecyclePhase {
        STARTUP,
        OPEN,
        CLOSE,
        SHUTDOWN;
    }
}
