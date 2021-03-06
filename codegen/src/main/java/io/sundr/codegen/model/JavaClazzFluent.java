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

package io.sundr.codegen.model;

import io.sundr.builder.Fluent;
import io.sundr.builder.Nested;

import java.util.LinkedHashSet;
import java.util.Set;

public class JavaClazzFluent<T extends JavaClazzFluent<T>> extends AttributeSupportFluent<T> implements Fluent<T> {

    private JavaType type;
    private Set<JavaMethod> methods = new LinkedHashSet();
    private Set<JavaMethod> constructors = new LinkedHashSet();
    private Set<JavaProperty> fields = new LinkedHashSet();
    private Set<JavaType> imports = new LinkedHashSet();
    private Set<JavaClazz> nested = new LinkedHashSet();

    public JavaType getType() {
        return this.type;
    }

    public T withType(JavaType type) {
        this.type = type;
        return (T) this;
    }

    public Set<JavaMethod> getMethods() {
        return this.methods;
    }

    public T withMethods(Set<JavaMethod> methods) {
        this.methods.clear();
        this.methods.addAll(methods);
        return (T) this;
    }

    public Set<JavaMethod> getConstructors() {
        return this.constructors;
    }

    public T withConstructors(Set<JavaMethod> constructors) {
        this.constructors.clear();
        this.constructors.addAll(constructors);
        return (T) this;
    }

    public Set<JavaProperty> getFields() {
        return this.fields;
    }

    public T withFields(Set<JavaProperty> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
        return (T) this;
    }

    public Set<JavaType> getImports() {
        return this.imports;
    }

    public T withImports(Set<JavaType> imports) {
        this.imports.clear();
        this.imports.addAll(imports);
        return (T) this;
    }

    public Set<JavaClazz> getNested() {
        return this.nested;
    }

    public T withNested(Set<JavaClazz> nested) {
        this.nested.clear();
        this.nested.addAll(nested);
        return (T) this;
    }

    public TypeNested<T> withNewType() {
        return new TypeNested<T>();
    }

    public T addToMethods(JavaMethod item) {
        this.methods.add(item);
        return (T) this;
    }

    public MethodsNested<T> addNewMethod() {
        return new MethodsNested<T>();
    }

    public T addToConstructors(JavaMethod item) {
        this.constructors.add(item);
        return (T) this;
    }

    public ConstructorsNested<T> addNewConstructor() {
        return new ConstructorsNested<T>();
    }

    public T addToFields(JavaProperty item) {
        this.fields.add(item);
        return (T) this;
    }

    public FieldsNested<T> addNewField() {
        return new FieldsNested<T>();
    }

    public T addToImports(JavaType item) {
        this.imports.add(item);
        return (T) this;
    }

    public ImportsNested<T> addNewImport() {
        return new ImportsNested<T>();
    }

    public T addToNested(JavaClazz item) {
        this.nested.add(item);
        return (T) this;
    }

    public NestedNested<T> addNewNested() {
        return new NestedNested<T>();
    }

    public class TypeNested<N> extends JavaTypeFluent<TypeNested<N>> implements Nested<N> {

        public N and() {
            return (N) withType(new JavaTypeBuilder(this).build());
        }

        public N endType() {
            return and();
        }

    }

    public class MethodsNested<N> extends JavaMethodFluent<MethodsNested<N>> implements Nested<N> {

        public N endMethod() {
            return and();
        }

        public N and() {
            return (N) addToMethods(new JavaMethodBuilder(this).build());
        }

    }

    public class ConstructorsNested<N> extends JavaMethodFluent<ConstructorsNested<N>> implements Nested<N> {

        public N endConstructor() {
            return and();
        }

        public N and() {
            return (N) addToConstructors(new JavaMethodBuilder(this).build());
        }

    }

    public class FieldsNested<N> extends JavaPropertyFluent<FieldsNested<N>> implements Nested<N> {

        public N endField() {
            return and();
        }

        public N and() {
            return (N) addToFields(new JavaPropertyBuilder(this).build());
        }

    }

    public class ImportsNested<N> extends JavaTypeFluent<ImportsNested<N>> implements Nested<N> {

        public N and() {
            return (N) addToImports(new JavaTypeBuilder(this).build());
        }

        public N endImport() {
            return and();
        }

    }

    public class NestedNested<N> extends JavaClazzFluent<NestedNested<N>> implements Nested<N> {

        public N and() {
            return (N) addToNested(new JavaClazzBuilder(this).build());
        }

        public N endNested() {
            return and();
        }

    }


}
