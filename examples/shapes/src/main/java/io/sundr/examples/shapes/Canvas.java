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

package io.sundr.examples.shapes;

import io.sundr.builder.annotations.Buildable;

import java.util.Collections;
import java.util.List;

public class Canvas {

    private final List<Shape> shapes;

    @Buildable
    public Canvas(List<Shape> shapes) {
        this.shapes = Collections.unmodifiableList(shapes);
    }

    public List<Shape> getShapes() {
        return shapes;
    }
}
