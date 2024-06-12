package com.ospreydcs.dp.api.grpc.ingest;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.model.DpGrpcException;;

/**
 * <p>
 * JUnit test cases for class <code>DpIngestionConnectionFactoryDeprecated</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 * @see DpIngestionConnectionFactoryDeprecated
 */
public class DpIngestionConnectionFactoryDeprecatedTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    
    //
    // Test Cases
    //
    
    /**
     * Test method for {@link DpIngestionConnectionFactoryDeprecated#connect()}.
     */
    @Test
    public final void testConnect() {
        try {
            DpIngestionConnection   conn = DpIngestionConnectionFactoryDeprecated.connect();
            
            conn.shutdownSoft();
            
        } catch (DpGrpcException e) {
            fail("Threw execption: " + e.getMessage());
            
        } catch (InterruptedException e) {
            fail("Shutdown threw InterruptedException: " + e.getMessage());
            
        }
    }

    /**
     * Test method for {@link DpIngestionConnectionFactoryDeprecated#connect(String, int)}.
     */
    @Test
    public final void testConnectStringInt() {
        String  strUrl = DpApiConfig.getInstance().connections.ingestion.channel.host.url;
        int     intPort = DpApiConfig.getInstance().connections.ingestion.channel.host.port;
        
        long    lngConTmout = DpApiConfig.getInstance().connections.ingestion.timeout.limit;
        TimeUnit tuConTmout = DpApiConfig.getInstance().connections.ingestion.timeout.unit;
        
        try {
            DpIngestionConnection   conn = DpIngestionConnectionFactoryDeprecated.connect(strUrl, intPort);

            conn.shutdownSoft();
            conn.awaitTermination(lngConTmout, tuConTmout);

            Assert.assertTrue("Connection failed to terminated in alloted time", conn.isTerminated() );
            
        } catch (DpGrpcException e) {
            fail("Threw connection execption: " + e.getMessage()); 
            e.printStackTrace();
            
        } catch (InterruptedException e) {
            fail("Threw interrupted exception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    /**
     * Test method for {@link DpIngestionConnectionFactoryDeprecated#connect(String, int, boolean, long, TimeUnit)}.
     */
    @Test
    public final void testConnectStringIntBooleanLongTimeUnit() {
        String  strUrl = DpApiConfig.getInstance().connections.ingestion.channel.host.url;
        int     intPort = DpApiConfig.getInstance().connections.ingestion.channel.host.port;
        
        boolean bolPlainText = DpApiConfig.getInstance().connections.ingestion.channel.grpc.usePlainText;
        
        long    lngTmout = DpApiConfig.getInstance().connections.ingestion.channel.grpc.timeoutLimit;
        TimeUnit tuTmout = DpApiConfig.getInstance().connections.ingestion.channel.grpc.timeoutUnit;
        
        try {
            DpIngestionConnection   conn = DpIngestionConnectionFactoryDeprecated.connect(strUrl, intPort,
                    bolPlainText,
                    lngTmout, 
                    tuTmout);

            conn.shutdownSoft();
            conn.awaitTermination();

            Assert.assertTrue("Connection failed to terminated in alloted time", conn.isTerminated() );
            
        } catch (DpGrpcException e) {
            fail("Threw connection execption: " + e.getMessage()); 
            e.printStackTrace();
            
        } catch (InterruptedException e) {
            fail("Threw interrupted exception: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

}
