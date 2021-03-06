/*
 * Copyright 2015 The original authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.sundr.dsl.internal.utils;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.sundr.codegen.model.JavaClazz;
import io.sundr.codegen.model.JavaClazzBuilder;
import io.sundr.codegen.model.JavaKind;
import io.sundr.codegen.model.JavaMethod;
import io.sundr.codegen.model.JavaMethodBuilder;
import io.sundr.codegen.model.JavaType;
import io.sundr.codegen.utils.ModelUtils;
import io.sundr.dsl.annotations.EntryPoint;
import io.sundr.dsl.annotations.Multiple;
import io.sundr.dsl.annotations.Previous;
import io.sundr.dsl.annotations.TargetName;
import io.sundr.dsl.annotations.Terminal;
import io.sundr.dsl.internal.processor.DslProcessorContext;
import io.sundr.dsl.internal.type.functions.Generics;
import io.sundr.dsl.internal.type.functions.Merge;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.sundr.codegen.utils.StringUtils.captializeFirst;
import static io.sundr.dsl.internal.Constants.INTERFACE_SUFFIX;
import static io.sundr.dsl.internal.Constants.IS_COMPOSITE;
import static io.sundr.dsl.internal.Constants.IS_ENTRYPOINT;
import static io.sundr.dsl.internal.Constants.IS_GENERIC;
import static io.sundr.dsl.internal.Constants.IS_TERMINAL;
import static io.sundr.dsl.internal.Constants.KEYWORDS;
import static io.sundr.dsl.internal.Constants.METHOD_NAME;
import static io.sundr.dsl.internal.Constants.CARDINALITY_MULTIPLE;
import static io.sundr.dsl.internal.Constants.ORIGINAL_RETURN_TYPE;
import static io.sundr.dsl.internal.Constants.TERMINATING_TYPES;
import static io.sundr.dsl.internal.Constants.TRANSITIONS;
import static io.sundr.dsl.internal.Constants.TRANSPARENT;
import static io.sundr.dsl.internal.Constants.USE_PREVIOUS_TRANSITIONS;
import static io.sundr.dsl.internal.Constants.VOID;

public final class JavaTypeUtils {

    private JavaTypeUtils() {
        //Utility Class
    }

    /**
     * Convert an {@link javax.lang.model.element.ExecutableElement} to a {@link io.sundr.codegen.model.JavaClazz}
     *
     * @param context           The context of the operation.
     * @param executableElement The target element.
     * @return                  An instance of {@link io.sundr.codegen.model.JavaClazz} that describes the interface.
     */
    public static JavaClazz executableToInterface(DslProcessorContext context, ExecutableElement executableElement) {
        //Do generate the interface
        String methodName = executableElement.getSimpleName().toString();
        Boolean multiple = executableElement.getAnnotation(Multiple.class) != null;
        Boolean usePreviousTransitions = executableElement.getAnnotation(Previous.class) != null;
        Boolean isEntryPoint = executableElement.getAnnotation(EntryPoint.class) != null;
        Boolean isTerminal = executableElement.getAnnotation(Terminal.class) != null
                || !isVoid(executableElement);

        Set<String> transitions = new LinkedHashSet<>();
        Set<String> keywords = new LinkedHashSet<>();
        for (AnnotationMirror annotationMirror : context.getToTransitionAnnotations().apply(executableElement)) {
            transitions.addAll(context.getToTransitionClassName().apply(annotationMirror));
        }

        for (AnnotationMirror annotationMirror : context.getToKeywordAnnotations().apply(executableElement)) {
            keywords.add(context.getToKeywordClassName().apply(annotationMirror));
        }

        JavaType returnType = null;
        if (isTerminal(executableElement)) {
            returnType = isVoid(executableElement) ?
                    VOID :
                    context.getToType().apply(executableElement.getReturnType().toString());
        } else {
            returnType = TRANSPARENT;
        }

        JavaType genericType = Generics.MAP.apply(returnType);

        JavaMethod sourceMethod = context.getToMethod().apply(executableElement);
        JavaMethod targetMethod = new JavaMethodBuilder(sourceMethod).withReturnType(genericType).build();

        TargetName targetInterfaceName = executableElement.getAnnotation(TargetName.class);
        String interfaceName = targetInterfaceName != null ?
                targetInterfaceName.value() :
                toInterfaceName(targetMethod.getName());

        return new JavaClazzBuilder()
                .withNewType()
                    .withPackageName(ModelUtils.getPackageElement(executableElement).toString())
                    .withClassName(interfaceName)
                    .addToGenericTypes(genericType)
                    .withKind(JavaKind.INTERFACE)
                    .addToAttributes(ORIGINAL_RETURN_TYPE, returnType)
                    .addToAttributes(IS_ENTRYPOINT, isEntryPoint)
                    .addToAttributes(IS_TERMINAL, isTerminal)
                    .addToAttributes(KEYWORDS, keywords)
                    .addToAttributes(TRANSITIONS, isTerminal ? Collections.emptySet() : transitions)
                    .addToAttributes(CARDINALITY_MULTIPLE, multiple)
                    .addToAttributes(USE_PREVIOUS_TRANSITIONS, usePreviousTransitions)
                    .addToAttributes(TERMINATING_TYPES, isTerminal ? new LinkedHashSet<>(Arrays.asList(returnType)) : Collections.emptySet())
                    .addToAttributes(METHOD_NAME, methodName)
                .endType()
                .addToMethods(targetMethod)
                .build();
    }


    /**
     * Convert a {@link Collection} of {@link javax.lang.model.element.ExecutableElement}s to a {@link java.util.Set} of {@link io.sundr.codegen.model.JavaClazz}es.
     *
     * @param context           The context of the operation.
     * @param elements          The target elements.
     * @return                  A set of {@link io.sundr.codegen.model.JavaClazz} that describes the interfaces.
     */
    public static Set<JavaClazz> executablesToInterfaces(DslProcessorContext context, Collection<ExecutableElement> elements) {
        Map<String, JavaClazz> byName = new LinkedHashMap<>();
        for (ExecutableElement current : elements) {
            JavaClazz clazz = executableToInterface(context, current);
            String name = clazz.getType().getFullyQualifiedName();
            if (byName.containsKey(name)) {
                JavaClazz other = byName.remove(name);
                byName.put(name, Merge.CLASSES.apply(new JavaClazz[]{other, clazz}));
            } else {
                byName.put(name, clazz);
            }
        }
        return new LinkedHashSet<>(byName.values());
    }



    public static final Set<JavaType> getTerminatingTypes(JavaType type) {
        Set<JavaType> result = new LinkedHashSet<>();
        if (type.getAttributes().containsKey(TERMINATING_TYPES)) {
            result.addAll((Collection<JavaType>) type.getAttributes().get(TERMINATING_TYPES));
        }
        if (type.getAttributes().containsKey(IS_COMPOSITE)
                && (Boolean) type.getAttributes().get(IS_TERMINAL)
                && !(type.getAttributes().get(ORIGINAL_RETURN_TYPE).equals(TRANSPARENT))) {
            result.add((JavaType) type.getAttributes().get(ORIGINAL_RETURN_TYPE));
        }
        return result;
    }

    public static Set<JavaType> extractInterfaces(Set<JavaType> types) {
        Set<JavaType> result = new LinkedHashSet<>();
        for (JavaType type : types) {
            result.addAll(extractInterfaces(type));
        }
        return result;
    }

    public static Set<JavaType> extractInterfaces(JavaType type) {
        Set<JavaType> result = new LinkedHashSet<>();
        if (type.getInterfaces().isEmpty()) {
            result.add(type);
        } else {
            for (JavaType interfaceType : type.getInterfaces()) {
                result.addAll(extractInterfaces(interfaceType));
            }
        }
        return result;
    }

    public static String toInterfaceName(String name) {
        if (name.endsWith(INTERFACE_SUFFIX)) {
            return name;
        }
        return captializeFirst(name) + INTERFACE_SUFFIX;
    }

    public static boolean isVoid(ExecutableElement executableElement) {
        return executableElement.getReturnType().toString().equals("void");
    }

    public static boolean isTerminal(ExecutableElement executableElement) {
        return executableElement.getAnnotation(Terminal.class) != null;
    }

    public static boolean isEntryPoint(JavaClazz clazz) {
        return clazz.getType().getAttributes().containsKey(IS_ENTRYPOINT)
                && (clazz.getType().getAttributes().get(IS_ENTRYPOINT) instanceof Boolean)
                && (Boolean) clazz.getType().getAttributes().get(IS_ENTRYPOINT);
    }

    public static boolean isTerminal(JavaClazz clazz) {
        return clazz.getType().getAttributes().containsKey(IS_TERMINAL)
                && (clazz.getType().getAttributes().get(IS_TERMINAL) instanceof Boolean)
                && (Boolean) clazz.getType().getAttributes().get(IS_TERMINAL);
    }

    public static boolean isCardinalityMultiple(JavaClazz clazz) {
        return clazz.getType().getAttributes().containsKey(CARDINALITY_MULTIPLE)
                && (clazz.getType().getAttributes().get(CARDINALITY_MULTIPLE) instanceof Boolean)
                && (Boolean) clazz.getType().getAttributes().get(CARDINALITY_MULTIPLE);
    }

    public static boolean isGeneric(JavaType type) {
        return type.getAttributes().containsKey(IS_GENERIC)
                && (type.getAttributes().get(IS_GENERIC) instanceof Boolean)
                && (Boolean) type.getAttributes().get(IS_GENERIC);
    }

    public static boolean usePreviousTransitions(JavaClazz clazz) {
        return clazz.getType().getAttributes().containsKey(USE_PREVIOUS_TRANSITIONS)
                && (clazz.getType().getAttributes().get(USE_PREVIOUS_TRANSITIONS) instanceof Boolean)
                && (Boolean) clazz.getType().getAttributes().get(USE_PREVIOUS_TRANSITIONS);
    }
}
