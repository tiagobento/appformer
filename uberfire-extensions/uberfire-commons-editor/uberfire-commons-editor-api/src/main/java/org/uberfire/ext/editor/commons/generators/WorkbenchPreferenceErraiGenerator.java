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

package org.uberfire.ext.editor.commons.generators;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.uberfire.annotations.processors.generators.UberfireClassGenerator;
import org.uberfire.ext.preferences.processors.GeneratorContext;
import org.uberfire.ext.preferences.processors.WorkbenchPreferenceGeneratedImplGenerator;
import org.uberfire.preferences.shared.annotations.WorkbenchPreference;

import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.NOTE;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class WorkbenchPreferenceErraiGenerator extends ErraiAptGenerators.MultipleFiles {

    private final ErraiAptExportedTypes exportedTypes;
    private final UberfireClassGenerator uberfireClassGenerator;

    private final WorkbenchPreferenceGeneratedImplGenerator portableGenerator;
    private final WorkbenchPreferenceGeneratedImplGenerator beanGenerator;

    // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
    public WorkbenchPreferenceErraiGenerator(final ErraiAptExportedTypes exportedTypes) {
        super(exportedTypes);
        this.exportedTypes = exportedTypes;
        this.uberfireClassGenerator = new UberfireClassGenerator(exportedTypes.processingEnvironment(), "");

        this.beanGenerator = new WorkbenchPreferenceGeneratedImplGenerator(GeneratorContext.BEAN);
        this.portableGenerator = new WorkbenchPreferenceGeneratedImplGenerator(GeneratorContext.PORTABLE);
    }

    @Override
    public Collection<ErraiAptGeneratedSourceFile> files() {

        //FIXME: tiago: uncomment
        return Collections.emptyList();
//        exportedTypes.processingEnvironment()
//                .getMessager()
//                .printMessage(NOTE, "Generating source files for uberfire-preferences-api");
//
//        return exportedTypes.findAnnotatedMetaClasses(WorkbenchPreference.class).stream()
//                .flatMap(this::generateFiles)
//                .collect(toList());
    }

    private Stream<ErraiAptGeneratedSourceFile> generateFiles(final MetaClass metaClass) {
        final ErraiAptGeneratedSourceFile beanImpl = uberfireClassGenerator.generateFile(metaClass, beanGenerator);
        final ErraiAptGeneratedSourceFile portableImpl = uberfireClassGenerator.generateFile(metaClass, portableGenerator);
        return Stream.of(beanImpl, portableImpl);
    }

    @Override
    public int layer() {
        return -1;
    }
}
