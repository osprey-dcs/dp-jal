/*
 * Project: dp-api-common
 * File:	ClientRequestId.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	ClientRequestId
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
 * @since Apr 16, 2024
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

/**
 * <p>
 * Record for encapsulating the ingest data request unique identifier.
 * </p>
 * <p>
 * The ingest data request UID, or client request ID, is an identifier provided by Ingestion
 * Service clients to identify each ingest data request it sends.  This record is intended to
 * abstract the notion since the exact form of the identifier may change in the future.
 * </p>
 * 
 * @param   requestId    the client ingest data request identifier (as a string)
 * 
 * @author Christopher K. Allen
 * @since Apr 16, 2024
 *
 */
public record ClientRequestId(String requestId) {

}
