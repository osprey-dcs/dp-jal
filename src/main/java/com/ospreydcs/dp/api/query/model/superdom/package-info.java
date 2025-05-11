/*
 * Project: dp-api-common
 * File:	package-info.java
 * Package: com.ospreydcs.dp.api.query.model.superdom
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
 * @since Apr 16, 2025
 *
 */

/**
 * <p>
 * Java API Library query API model package containing resources for handling time-domain collisions within
 * recovered raw, time-series data.
 * </p>
 * <p>
 * The enabled class in this package is <code>TimeDomainProcessor</code>.  This class takes raw, response data
 * (time-series) that has already been correlated and searches for time-domain collisions within the timestamp
 * messages for each correlated block (i.e., <code>RawCorrelatedData</code> instance).  It then collects the
 * raw, correlated data containing collisions into "super domains", which are collections of raw data with an 
 * established time interval where the collisions occur.  
 * </p>
 * <p>
 * The super domains of raw data are represented by the <code>RawSuperDomData</code> class, which DO NOT inherit from
 * the <code>RawCorrelatedData</code> base class.  A <code>RawSuperDomData</code> instance can be coalesced into a
 * <code>SampledBlockSuperDom</code> instance which DOES inherit from the <code>SampledBlock</code> class and, thus,
 * can be aggregated into a <code>SampledAggregate</code> object.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since Apr 16, 2025
 *
 */
package com.ospreydcs.dp.api.query.model.superdom;