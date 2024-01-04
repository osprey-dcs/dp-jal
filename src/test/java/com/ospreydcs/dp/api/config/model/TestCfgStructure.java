package com.ospreydcs.dp.api.config.model;

/**
 * <p>
 * Structure class used for testing configuration parameter loading and override utility.
 * </p>
 * <p>
 * Assumes the root role of a configuration class.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Jan 2, 2024
 *
 */
@ACfgOverride.Root(root="DP_API_TESTSTRUCT")
public final class TestCfgStructure extends CfgStructure<TestCfgStructure> {
    
    public TestCfgStructure() {
        super(TestCfgStructure.class);
    }


    @ACfgOverride.Field(name="NAME")
    public String       name;
    
    @ACfgOverride.Field(name="VERSION")
    public Integer      version;
    
    @ACfgOverride.Struct(pathelem="DATA")
    public TestCfgStructure.SubStructure data;
    
    @ACfgOverride.Root(root="SUBSTRUCTURE_TEST")    // included for verification
    public static final class SubStructure extends CfgStructure<TestCfgStructure.SubStructure> {
        
        public SubStructure() {
            super(TestCfgStructure.SubStructure.class);
        }


        @ACfgOverride.Field(name="BOOLEAN")
        public Boolean  bol;
        
        @ACfgOverride.Field(name="INTEGER")
        public Integer  integer;
        
        @ACfgOverride.Field(name="DOUBLE")
        public Double   dbl;
        
        @ACfgOverride.Field(name="STRING")
        public String   str;
        
        @ACfgOverride.Struct(pathelem="STRUCT1")
        public SubStructure.SubSubStructure  sub1;
        
        @ACfgOverride.Struct(pathelem="STRUCT2")
        public SubStructure.SubSubStructure  sub2;
        
        @ACfgOverride.Root(root="SUBSUBSTRUCTURE_TEST") // included for verification
        public static final class SubSubStructure extends CfgStructure<SubStructure.SubSubStructure> {
            
            public SubSubStructure() {
                super(SubStructure.SubSubStructure.class);
            }

            @ACfgOverride.Field(name="BOOLEAN")
            public Boolean  bol;
            
            @ACfgOverride.Field(name="STRING")
            public String   str;
        }
    }
    
    
    //
    // Class Methods
    //
    
    /**
     * <p>
     * Creates and populates a new <code>TestCfgStructure</code> instance.
     * </p>
     * 
     * @return new <code>TestCfgStructure</code> instance populated with test data
     */
    public static TestCfgStructure    createStructure() {
        TestCfgStructure   test = new TestCfgStructure();
        
        test.name = "Test Structure";
        test.version = 1;
        
        test.data.bol = true;
        test.data.integer = 42;
        test.data.dbl = Math.E;
        test.data.str = "Hello World";
        
        test.data.sub1.bol = true;
        test.data.sub1.str = "substructure 1";
        test.data.sub2.bol = false;
        test.data.sub2.str = "substructure 2";
        
        return test;
    }

}