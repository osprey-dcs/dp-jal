/*
 * Project: dp-api-common
 * File:	QueryRecoveryTestResult.java
 * Package: com.ospreydcs.dp.jal.tools.query.recovery
 * Type: 	QueryRecoveryTestResult
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
 * @since Jul 23, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.query.recovery;

/**
 * <p>
 * Record containing results of a <code>QueryRecoveryTestCase</code> evaluation.
 * </p>
 * <p>
 * Instances of <code>QueryRecoveryTestResult</code> are intended for creation by the 
 * <code>{@link QueryRecoveryTestCase#evaluate(QueryResponseRecoverer, java.util.List)</code> method.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jul 23, 2025
 *
 */
public record QueryRecoveryTestResult(
        ) 
{

}
