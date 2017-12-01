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

package org.uberfire.generators;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.uberfire.annotations.processors.AbstractGenerator;
import org.uberfire.annotations.processors.ContextActivityGenerator;
import org.uberfire.annotations.processors.EditorActivityGenerator;
import org.uberfire.annotations.processors.PerspectiveActivityGenerator;
import org.uberfire.annotations.processors.PopupActivityGenerator;
import org.uberfire.annotations.processors.ScreenActivityGenerator;
import org.uberfire.annotations.processors.SplashScreenActivityGenerator;
import org.uberfire.annotations.processors.generators.UberfireClassGenerator;
import org.uberfire.client.annotations.WorkbenchContext;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.annotations.WorkbenchSplashScreen;

import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class WorkbenchClientApiActivitiesErraiGenerator extends ErraiAptGenerators.MultipleFiles {

    private static final Map<Class<? extends Annotation>, AbstractGenerator> GENERATORS;

    static {
        GENERATORS = new HashMap<>();
        GENERATORS.put(WorkbenchPopup.class, new PopupActivityGenerator());
        GENERATORS.put(WorkbenchScreen.class, new ScreenActivityGenerator());
        GENERATORS.put(WorkbenchEditor.class, new EditorActivityGenerator());
        GENERATORS.put(WorkbenchContext.class, new ContextActivityGenerator());
        GENERATORS.put(WorkbenchSplashScreen.class, new SplashScreenActivityGenerator());
        GENERATORS.put(WorkbenchPerspective.class, new PerspectiveActivityGenerator());
    }

    private final ErraiAptExportedTypes exportedTypes;
    private final UberfireClassGenerator uberfireClassGenerator;

    // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
    public WorkbenchClientApiActivitiesErraiGenerator(final ErraiAptExportedTypes exportedTypes) {
        super(exportedTypes);
        this.exportedTypes = exportedTypes;
        this.uberfireClassGenerator = new UberfireClassGenerator(exportedTypes.processingEnvironment(), "Activity");
    }

    @Override
    public Collection<ErraiAptGeneratedSourceFile> files() {

        exportedTypes.processingEnvironment()
                .getMessager()
                .printMessage(NOTE, "Generating source files for uberfire-client-api");

        return GENERATORS.entrySet().stream()
                .flatMap(e -> generateFiles(e.getKey(), e.getValue()))
                .collect(toList());
    }

    private Stream<ErraiAptGeneratedSourceFile> generateFiles(final Class<? extends Annotation> annotation,
                                                              final AbstractGenerator abstractGenerator) {

        return exportedTypes.findAnnotatedMetaClasses(annotation)
                .stream()
                .map(metaclass -> uberfireClassGenerator.generateFile(metaclass, abstractGenerator));
    }

    @Override
    public int layer() {
        return -1;
    }
}
