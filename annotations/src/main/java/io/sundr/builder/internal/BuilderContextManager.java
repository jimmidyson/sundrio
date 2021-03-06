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

package io.sundr.builder.internal;

import io.sundr.builder.Builder;
import io.sundr.builder.Fluent;
import io.sundr.codegen.functions.ClassToJavaType;
import io.sundr.codegen.model.JavaType;
import io.sundr.codegen.model.JavaTypeBuilder;

import static io.sundr.codegen.utils.TypeUtils.newGeneric;
import static io.sundr.codegen.utils.TypeUtils.typeGenericOf;

public class BuilderContextManager {

    private static final ThreadLocal<BuilderContext> context = new ThreadLocal<>();

    public static void create() {
        context.set(new BuilderContext(Builder.class.getPackage().getName()));
    }

    public static void create(String packageName) {
        context.set(new BuilderContext(packageName));
    }

    public static synchronized BuilderContext getContext() {
        if (context.get() == null) {
            create();
        }
        return context.get();
    }
}
