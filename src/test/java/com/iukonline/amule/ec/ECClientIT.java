/*
 * Copyright (c) 2012. Gianluca Vegetti - iuk@iukonline.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package com.iukonline.amule.ec;

import static org.junit.Assert.assertTrue;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iukonline.amule.ec.v204.ECClientV204;
import com.iukonline.amule.ec.v204.ECCodesV204;

/**
 * @author gvegetti
 *
 */
public class ECClientIT {


    final static String SERVER_HOST = "<TEST SERVER HOST HERE>";
    final static int SERVER_PORT = 4712;
    final static String SERVER_PASSWORD = "<TEST SERVER PASSWORD HERE>";

    static ECClientV204 cl;
    static Socket socket;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        cl = new ECClientV204();
        try {
            cl.setPassword(SERVER_PASSWORD);
        } catch (Exception e) {
            System.err.print("Error while generating password hash: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        cl.setClientName("EC Testing");
        cl.setClientVersion("pre-aplha");
        cl.setTracer(System.out);

        socket = new Socket(SERVER_HOST, SERVER_PORT);
        cl.setSocket(socket);

        //cl.enableUTF8Compression();
    }



    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSearch() throws Exception {
        System.out.println("Running searchStart...");
        String resString = cl.searchStart("matrix", null, null, -1, -1, 0, ECCodesV204.EC_SEARCH_KAD);
        System.out.println("Got search response " + resString);

        ECSearchResults res = null;

        byte progress = 0;
        while (progress < 100) {
            System.out.println("querying searchProgress...");
            progress = cl.searchProgress();
            System.out.println("got " + progress);
            res = cl.searchGetReults(res);

            System.out.println("Result \n" + res.toString());

            Thread.sleep(5000);
        }

        System.out.println("Search finished");

        if (! res.resultMap.isEmpty()) {
            System.out.println("Starting first result");
            ECSearchFile sf = res.resultMap.values().iterator().next();
            cl.searchStartResult(sf);

            cl.changeDownloadStatus(sf.getHash(), ECCodes.EC_OP_PARTFILE_DELETE);

            assertTrue(true);
            return;

        }

        assertTrue(false);
    }

    @Test
    public void fetchDlQueue() throws Exception {


        System.out.print("Running fetchDlQueue...\n");

        HashMap<String, ECPartFile> dlQueue = cl.getDownloadQueue(ECCodes.EC_DETAIL_CMD);

        if (dlQueue != null) {

            for (Iterator<ECPartFile> i = dlQueue.values().iterator(); i.hasNext(); ) {
                ECPartFile p = i.next();
                System.out.println(p.toString());
                cl.refreshPartFile(p, ECCodes.EC_DETAIL_CMD);
            }


            cl.refreshDlQueue(dlQueue);
            for (Iterator<ECPartFile> i = dlQueue.values().iterator(); i.hasNext(); ) {
                ECPartFile p = i.next();
                System.out.println(p.toString());
            }

        }

        assertTrue(dlQueue != null);




    }

    @Test
    public void getStats() throws Exception {
        System.out.println("Running get stats...");
        ECStats stats = cl.getStats(ECCodes.EC_DETAIL_FULL);
        if (stats != null) {
            System.out.println(stats.toString());
            System.out.format("isConnectedEd2k: %s, isConnectedKad: %s, isKadRunning: %s, isKadFirewalled: %s\n",
                            stats.getConnState().isConnectedEd2k(),
                            stats.getConnState().isConnectedKad(),
                            stats.getConnState().isKadRunning(),
                            stats.getConnState().isKadFirewalled());
        }
        assertTrue(stats != null);
    }

    @Test
    public void testCategories() throws Exception {
        String comment = "TEST CATEGORY FOR ECClientTest";
        System.out.println("Adding test category...");

        cl.createCategory(new ECCategory("ECClientTest", "/share/Download/", comment, ECPartFile.PR_HIGH, (byte)0xff0000));

        System.out.println("Running get categories...");
        ECCategory[] catList = cl.getCategories(ECCodes.EC_DETAIL_FULL);
        ECCategory newCat = null;

        if (catList != null) {
            for (int i = 0; i < catList.length; i++) {
                System.out.println(catList[i].toString());
                if (catList[i].getComment().equals(comment)) newCat = catList[i];
            }
        }

        if (newCat != null) {
            newCat.setComment("UDPATED!");
            System.out.println("Updating test category...");
            cl.updateCategory(newCat);

            System.out.println("Running get categories...");
            catList = cl.getCategories(ECCodes.EC_DETAIL_FULL);

            for (int i = 0; i < catList.length; i++) {
                System.out.println(catList[i].toString());
            }

            System.out.println("Deleting test category...");
            cl.deleteCategory(newCat.getId());

            System.out.println("Running get categories...");
            catList = cl.getCategories(ECCodes.EC_DETAIL_FULL);

            for (int i = 0; i < catList.length; i++) {
                System.out.println(catList[i].toString());
            }
        }

        assertTrue(newCat != null);



    }



}
