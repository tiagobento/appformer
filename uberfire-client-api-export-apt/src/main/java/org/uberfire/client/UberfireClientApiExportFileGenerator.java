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

package org.uberfire.client;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import org.jboss.errai.common.apt.generator.AbstractErraiModuleExportFileGenerator;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;

import static org.uberfire.client.UberfireClientApiExportFileGenerator.SupportedAnnotationTypes.WORKBENCH_CONTEXT;
import static org.uberfire.client.UberfireClientApiExportFileGenerator.SupportedAnnotationTypes.WORKBENCH_EDITOR;
import static org.uberfire.client.UberfireClientApiExportFileGenerator.SupportedAnnotationTypes.WORKBENCH_PERSPECTIVE;
import static org.uberfire.client.UberfireClientApiExportFileGenerator.SupportedAnnotationTypes.WORKBENCH_POPUP;
import static org.uberfire.client.UberfireClientApiExportFileGenerator.SupportedAnnotationTypes.WORKBENCH_SCREEN;
import static org.uberfire.client.UberfireClientApiExportFileGenerator.SupportedAnnotationTypes.WORKBENCH_SPLASH_SCREEN;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({WORKBENCH_SPLASH_SCREEN, WORKBENCH_CONTEXT, WORKBENCH_EDITOR, WORKBENCH_PERSPECTIVE, WORKBENCH_POPUP, WORKBENCH_SCREEN})
public class UberfireClientApiExportFileGenerator extends AbstractErraiModuleExportFileGenerator {

    @Override
    protected String getCamelCaseErraiModuleName() {
        return "uberfireApiClient";
    }

    @Override
    protected Class<?> getExportingStrategiesClass() {
        return UberfireClientApiExportingStrategies.class;
    }

    public interface UberfireClientApiExportingStrategies {

        @ErraiExportingStrategy(WORKBENCH_SPLASH_SCREEN)
        void workbenchSplashScreen();

        @ErraiExportingStrategy(WORKBENCH_SCREEN)
        void workbenchScreen();

        @ErraiExportingStrategy(WORKBENCH_CONTEXT)
        void workbenchContext();

        @ErraiExportingStrategy(WORKBENCH_PERSPECTIVE)
        void workbenchPerspective();

        @ErraiExportingStrategy(WORKBENCH_EDITOR)
        void workbenchEditor();

        @ErraiExportingStrategy(WORKBENCH_POPUP)
        void workbenchPopup();
    }

    public interface SupportedAnnotationTypes {

        String WORKBENCH_SPLASH_SCREEN = "org.uberfire.client.annotations.WorkbenchSplashScreen";
        String WORKBENCH_SCREEN = "org.uberfire.client.annotations.WorkbenchScreen";
        String WORKBENCH_CONTEXT = "org.uberfire.client.annotations.WorkbenchContext";
        String WORKBENCH_PERSPECTIVE = "org.uberfire.client.annotations.WorkbenchPerspective";
        String WORKBENCH_EDITOR = "org.uberfire.client.annotations.WorkbenchEditor";
        String WORKBENCH_POPUP = "org.uberfire.client.annotations.WorkbenchPopup";
    }
}