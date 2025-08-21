/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.jal.tools.query.channel
 * Type: 	package-info
 *
 * Copyright 2010-2025 the original author or authors.
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
 * @since May 9, 2025
 *
 */
/**
 * <p>
 * Package containing resources and tools for testing the Java API Library <code>QueryChannel</code> system.
 * </p>
 * <p>
 * The <code>{@link QueryChannel}</code> class manages <code>{@link QueryStream}</code> instances, executing them
 * as separate thread tasks for recovering requested time-series data.  It provides a simple interface for 
 * data recovery and is the first "high-level" stage in Data Platform Query Service time-series data recovery
 * and assembly.
 * </p>
 * <p>
 * Thus, consequent to the above, tools measuring the performance of the <code>QueryChannel</code> class 
 * directly reflect the maximum upper limit achievable with time-series data requests.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since May 9, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.channel;