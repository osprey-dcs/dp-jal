/*
 * Project: dp-api-common
 * File:	AUnavailable.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	AUnavailable
 *
 * Copyright 2010-2023 the original author or authors.
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
 * @since Dec 31, 2023
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation indicating that an API feature, attribute, or method is available but not yet implemented. 
 * </p>
 * <p>
 * Appearance of this annotation indicates that an API feature is intended for future availability but
 * is not currently operational.  Thus developers may anticipate the feature but <b>do not</b> use it 
 * until this annotation is removed.
 * </p>
 * <p>
 * <h2>NOTES:</h2>
 * <ul>
 * <li>This annotation is intended only for <em>Data Platform</em> library code. 
 *     Developers should not attach this annotation to their own code.
 *     </li>
 * <li>The annotation contains a properties <code>reference</code> that can be used
 *     to direct users to an URL location for further information.
 *     </li>
 * <li>See: <a href="https://github.com/osprey-dcs/data-platform">data-platform</a>, 
 *     for code and documentation.
 *     </li>
 * </ul> 
 *
 * @param status    <code>{@link AUnavailable.STATUS}</code> enumeration indicating current status of API feature
 * @param note      <code>String</code> containing any developer public comments or instructions for users 
 * 
 * @author Christopher K. Allen
 * @since Dec 31, 2023
 *
 */
@Documented
@Retention(CLASS)
@Target({TYPE, FIELD, METHOD, TYPE_PARAMETER, TYPE_USE})
public @interface AUnavailable {
    
    /**
     * Enumeration of the status conditions for the feature.
     *
     * @author Christopher K. Allen
     * @since Dec 31, 2023
     *
     */
    public static enum STATUS {
        
        /** Unknown or unset status condition */
        UNKNOWN,
        
        /** Currently under review - the feature may or may not be available */
        UNDER_REVIEW,
        
        /** The feature has been accepted and will be developed */
        ACCEPTED,
        
        /** The feature had been rejected and is scheduled for removal */
        REJECTED
    }
    
    /**
     * Provide the current status of the unavailable feature.
     * 
     * @return  feature status
     */
    public STATUS status() default STATUS.UNKNOWN;
    
    /**
     * Provide an API developer note or comment regarding use or status of the advanced API feature.
     * 
     * @return API developer comment on API feature
     */
    public String note() default "";

}
