package com.ospreydcs.dp.api.query.model.request;

import com.ospreydcs.dp.api.query.DpDataRequest;

/**
 * <p>
 * Enumeration of possible query domain decomposition strategies.
 * </p>
 * <p>
 * Query domain decompositions are used for <em>composite query</em> construction.
 * </p>
 * 
 * @see RequestDomainDecomp
 * @see DpDataRequest#buildCompositeRequest(CompositeType, int)
 */
public enum CompositeType {
    
    /**
     * Query domain is decomposed horizontally (by data sources).
     * <p>
     * The data sources are divided equally amongst the concurrent queries. <br/>
     * That is, each separate query is for a different set of data sources. <br/>
     * All queries have the same time range. 
     */
    HORIZONTAL,

    /**
     * Query domain is decomposed vertically (in time).
     * <p>
     * The time range is divided equally amongst the concurrent queries. <br/>
     * That is, each separate query is for a different time range. <br/>
     * All queries have the same data sources.
     */
    VERTICAL,
    
    /**
     * Query domain is decomposed into a grid (i.e., of blocks in source and time).
     * <p>
     * Both the data sources and time range is divided equally amongst the concurrent queries.<br/>
     * That is, each separate query contains a subset of data sources and subset of time ranges.<br/> 
     */
    GRID,
    
    /**
     * Query domain is not decomposed.
     * <p>
     * Query domain decomposition is NOT applicable.  The query domain remains in 
     * its original form.
     */
    NONE;
}