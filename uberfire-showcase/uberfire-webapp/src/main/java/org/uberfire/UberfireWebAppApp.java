/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire;

import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiModule;
import org.uberfire.client.RuntimePluginsServiceProxyBackendImpl;
import org.uberfire.client.VFSLockServiceProxyBackendImpl;
import org.uberfire.client.VFSServiceProxyBackendImpl;
import org.uberfire.client.WorkbenchServicesProxyBackendImpl;

import static org.jboss.errai.common.configuration.Target.GWT;
import static org.jboss.errai.common.configuration.Target.JAVA;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiApp(gwtModuleName = "org.uberfire.FastCompiledUberfireShowcase",
        targets = {JAVA, GWT},
        userOnHostPageEnabled = true)
@ErraiModule(includes = {"client.*", "shared.*"},
        iocAlternatives = {WorkbenchServicesProxyBackendImpl.class,
                VFSServiceProxyBackendImpl.class,
                VFSLockServiceProxyBackendImpl.class,
                RuntimePluginsServiceProxyBackendImpl.class})
public class UberfireWebAppApp {

}
