/*
 * Project: dp-jal
 * File:	package-info.java
 * Package: com.ospreydcs.dp.jal.tools.common.datagen.factories.specs
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
 * @since Dec 11, 2025
 *
 */
/**
 * <p>
 * Sub-package containing specification records for the various datum and column factories.
 * </p>
 * <p>
 * Factory specification records are tools for configuration, management, and creation of datum and data column factories.
 * These immutable records maintain the configuration parameters for the various factories, offering optional
 * default values taken from the JAL Tools default configuration.
 * </p>
 * <p>
 * <h2>Factory Creation</h2>
 * Most factory specification records, say for example <code>FactorySpec</code>, provide a creation method for the
 * datum or column factory that they manage.  Typically the factory creation is performed with a method 
 * <code>newFactory()</code> with signature 
 * <code>
 * <pre>
 * public Factory   newFactory() { ... };
 * </pre>
 * </code>
 * where <code>Factory</code> is the class type of the datum or column factory managed by <code>FactorySpec</code>.
 * </p>
 * <p>
 * <h2>Command-Line Parsing</h2>
 * Factory specification records are particularly attractive for parsing the command-lines of application containing 
 * configuration parameters for datum and column factories.  Most specification records contain a creator of the
 * form
 * <code>
 * <pre>
 * public static FactorySpec parse(String...args) throws TypeNotPresentException, ConfigurationException, ... 
 * </pre>
 * </code>
 * where   
 * <ul>
 * <li><code>FactorySpec</code> is the factory specification record,</li>
 * <li><code>parse()</code> is the command-line arguments parsing creator,</li>
 * <li><code>args</code> string arrray containing the partial command line for the application.</li>
 * </ul>
 * The when successful (i.e., no exceptions are thrown) the <code>parse()</code> returns a fully populated
 * <code>FactorySpec</code> record ready for factory creation.  
 * See the documentation for each specification record <code>parse()</code> method for the required formatting
 * of the <code>args</code> argument.  Typically, the method provides various formatting options allowing for
 * default parameter values.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since Dec 11, 2025
 *
 */
package com.ospreydcs.dp.jal.tools.common.datagen.factories.specs;