package com.ospreydcs.dp.api.query.model.request;

/**
 * <p> 
 * Record containing the parameters for supported query domain decompositions.
 * </p>
 * <p>
 * Query domain decompositions are used for <em>decompose query</em> construction.
 * </p>
 * 
 * @param type              the type of decompose query 
 * @param cntHorizontal     number of sub-division for the data sources axis
 * @param cntVertical       number of sub-divisions for the time range axis.
 * 
 */
public record DataRequestDecompParams(DataRequestDecompType type, int cntHorizontal, int cntVertical) {
    
    /** Return the total number of domain covering sets */
    public int totalCovers() { return cntHorizontal * cntVertical; };
}