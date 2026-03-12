/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.frames
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
 * @since Jun 11, 2025
 *
 */
/**
 * <p>
 * Package containing resources for creating ingestion frames of simulated data.
 * </p>
 * <p>
 * <h2>Frame Factory Specification Records</h2>
 * Although the implementation class <code>IngestionFrameFactory</code> can be accessed and used directly, it is
 * recommended that client employ the use of <code>FrameFactorySpec</code> specification records whenever possible.
 * They are capable of generating <code>IFrameFactory</code> implementations with the 
 * <code>FrameFactorySpec.newFactory()</code> method.  There the implementation class type remains hidden; this
 * allows for future upgrades.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jun 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.frames;