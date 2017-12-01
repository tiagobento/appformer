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

package org.uberfire.annotations.processors.generators;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.uberfire.annotations.processors.AbstractGenerator;
import org.uberfire.annotations.processors.exceptions.GenerationException;

import static javax.tools.Diagnostic.Kind.NOTE;
import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.CLIENT;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class UberfireClassGenerator {

    private final ProcessingEnvironment processingEnvironment;
    private final String classNameSuffix;

    public UberfireClassGenerator(final ProcessingEnvironment processingEnvironment, final String classNameSuffix) {
        this.processingEnvironment = processingEnvironment;
        this.classNameSuffix = classNameSuffix;
    }

    public ErraiAptGeneratedSourceFile generateFile(final MetaClass metaClass,
                                                    final AbstractGenerator activityGenerator) {

        final Messager messager = processingEnvironment.getMessager();

        messager.printMessage(NOTE, "Discovered class [" + metaClass.getName() + "]");
        final String packageName = metaClass.getPackageName();
        final String activityClassName = metaClass.getName() + classNameSuffix;
        final TypeElement classElement = APTClassUtil.getTypeElement(metaClass.getCanonicalName());

        messager.printMessage(NOTE, "Generating code for [" + activityClassName + "]");
        final String sourceCode = generateSourceCode(packageName,
                                                     activityClassName,
                                                     classElement,
                                                     activityGenerator);

        return new ErraiAptGeneratedSourceFile(packageName,
                                               activityClassName,
                                               sourceCode,
                                               CLIENT);
    }

    private String generateSourceCode(final String packageName,
                                      final String activityClassName,
                                      final TypeElement classElement,
                                      final AbstractGenerator activityGenerator) {
        try {
            return activityGenerator.generate(packageName,
                                              activityClassName,
                                              classElement,
                                              processingEnvironment).toString();
        } catch (final GenerationException e) {
            //FIXME: improve exception handling
            throw new RuntimeException(e);
        }
    }
}
