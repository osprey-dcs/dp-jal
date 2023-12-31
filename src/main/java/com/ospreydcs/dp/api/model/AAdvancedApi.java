/*
 * Project: dp-api-common
 * File:	AAdvancedApi.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	AAdvancedApi
 *
 * Copyright 2010-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 * @author Christopher K. Allen
 * @org    OspreyDCS
 * @since Nov 19, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates a public API that can change at any time, and has no guarantee of API stability and
 * backward-compatibility. If users want stabilization or signature change of a specific API that
 * is currently annotated {@code @AdvancedApi}, please comment on its tracking issue on github
 * with rationale, use cases, and so forth, so that the Data Platform team may prioritize the process 
 * toward stabilization of the API.
 * </p>
 * <p>
 * Usage guidelines:
 * <ol>
 * <li>This annotation is used only on public API. Internal interfaces should not use it.</li>
 * <li>After gRPC has gained API stability, this annotation can only be added to new API. Adding it
 * to an existing API is considered API-breaking.</li>
 * <li>Removing this annotation from an API gives it stable status.</li>
 * </ol>
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>This annotation is intended only for <em>Data Platform</em> library code. 
 *     Users should not attach this annotation to their own code.
 *     </li>
 * <li>The annotation contains a properties <code>reference</code> that can be used
 *     to direct users to an URL location for further information.
 *     </li>
 * <li>See: <a href="https://github.com/osprey-dcs/data-platform">data-platform</a>, 
 *     for code and documentation.
 *     </li>
 * </ul> 
 *
 * @author Christopher K. Allen
 * @since Nov 19, 2022
 *
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.ANNOTATION_TYPE,
         ElementType.CONSTRUCTOR,
         ElementType.FIELD,
         ElementType.METHOD,
         ElementType.PACKAGE,
         ElementType.TYPE})
public @interface AAdvancedApi {

    /**
     * Provides a reference location (i.e., an URL) for further documentation of the
     * advanced API feature.
     * 
     * @return reference location for further API information
     */
    public String reference() default "";
}
