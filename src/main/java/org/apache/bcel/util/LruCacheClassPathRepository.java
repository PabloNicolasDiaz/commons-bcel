/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.bcel.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.bcel.classfile.JavaClass;

/**
 * This repository maintains least-recently-used (LRU) cache of {@link JavaClass} with maximum size {@code cacheSize}.
 *
 * <p>This repository supports a class path consisting of too many JAR files to handle in {@link
 * ClassPathRepository} or {@link MemorySensitiveClassPathRepository} without causing {@code OutOfMemoryError}.
 *
 * @since 6.4.0
 */
public class LruCacheClassPathRepository extends ClassPathRepository {

    private final LinkedHashMap<String, JavaClass> loadedClass;

    public LruCacheClassPathRepository(final ClassPath path, final int cacheSize) {
        super(path);

        if (cacheSize < 1) {
            throw new IllegalArgumentException("cacheSize must be a positive number");
        }
        int initialCapacity = (int) (0.75 * cacheSize);
        boolean accessOrder = true; // Evicts least-recently-accessed
        loadedClass = new LinkedHashMap<String, JavaClass>(initialCapacity, cacheSize,
                accessOrder) {
            protected boolean removeEldestEntry(Map.Entry<String, JavaClass> eldest) {
                return size() > cacheSize;
            }
        };
    }

    @Override
    public void storeClass(final JavaClass javaClass) {
        // Not storing parent's _loadedClass
        loadedClass.put(javaClass.getClassName(), javaClass);
        javaClass.setRepository(this);
    }

    @Override
    public JavaClass findClass(final String className) {
        return loadedClass.get(className);
    }
}
