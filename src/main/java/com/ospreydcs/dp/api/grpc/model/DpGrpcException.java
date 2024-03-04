/*
 * Project: dp-api-common
 * File:	DpGrpcException.java
 * Package: com.ospreydcs.dp.api.grpc
 * Type: 	DpGrpcException
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
 * @since Sep 21, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.grpc.model;

/**
 * <p>
 * Exception generated specific to gRPC (and Protocol Buffers) operations. 
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 21, 2022
 *
 */
public class DpGrpcException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>DpGrpcException</code>.
     *
     */
    public DpGrpcException() {
    }

    /**
     * Creates a new instance of <code>DpGrpcException</code>.
     *
     * @param message   exception message
     */
    public DpGrpcException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of <code>DpGrpcException</code>.
     *
     * @param cause source of this general exception
     */
    public DpGrpcException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance of <code>DpGrpcException</code>.
     *
     * @param message   exception message
     * @param cause     source of the this general exception
     */
    public DpGrpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of <code>DpGrpcException</code>.
     *
     * @param message           exception message
     * @param cause             source of the original exception
     * @param enableSuppression whether or not exception suppression should be enabled
     * @param writableStackTrace whether or not stack trace operation should be enabled
     */
    public DpGrpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
