package com.ospreydcs.dp.api.grpc;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ospreydcs.dp.api.config.DpApiConfig;
import com.ospreydcs.dp.api.grpc.DpIngestionConnectionFactory.DpIngestionConnection;;

/**
 * <p>
 * JUnit test cases for class <code>DpIngestionConnectionFactory</code>.
 * </p>
 *
 * @author Christopher K. Allen
 * @since Dec 28, 2023
 *
 * @see DpIngestionConnectionFactory
 */
public class DpIngestionConnectionFactoryTest {

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

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.grpc.DpIngestionConnectionFactory#connect()}.
     */
    @Test
    public final void testConnect() {
        try {
            DpIngestionConnection   conn = DpIngestionConnectionFactory.connect();
            
            conn.shutdownSoft();
            
        } catch (DpGrpcException e) {
            fail("Threw execption: " + e.getMessage()); 
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link com.ospreydcs.dp.api.config.grpc.DpIngestionConnectionFactory#connect(String, int)}.
     */
    @Test
    public final void testConnectStringInt() {
        String  strUrl = DpApiConfig.getInstance().services.ingestion.channel.host.url;
        int     intPort = DpApiConfig.getInstance().services.ingestion.channel.host.port;
        
        long    lngTmout = DpApiConfig.getInstance().services.ingestion.timeout.limit;
        TimeUnit tuTmout = DpApiConfig.getInstance().services.ingestion.timeout.unit;
        
        try {
            DpIngestionConnection   conn = DpIngestionConnectionFactory.connect(strUrl, intPort);

            conn.shutdownSoft();
            conn.awaitTermination(lngTmout, tuTmout);

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
