package com.ospreydcs.dp.api.config.test;

import com.ospreydcs.dp.api.config.model.ACfgOverride;
import com.ospreydcs.dp.api.config.model.CfgStructure;

/**
 * Defines test query data parameters
 */
@ACfgOverride.Root(root="DP_API_TEST_QUERY_DATA")
public class TestQueryDataConfig extends CfgStructure<TestQueryDataConfig> {

    /** Default constructor required for base class */
    public TestQueryDataConfig() {
        super(TestQueryDataConfig.class);
    }

    @ACfgOverride.Field(name="PERSISTENCE")
    public  String  persistence;
}