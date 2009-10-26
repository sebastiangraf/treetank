package com.treetank.service.rest;

import java.util.HashMap;
import java.util.Map;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;

public class RestTestHelper {

    private RestTestHelper() {
    }

    public static Map<String, TreeTankWrapper> getTestInstances() {
        TestHelper.deleteEverything();
        final TreeTankWrapper wrapper1 = new TreeTankWrapper(
                ITestConstants.PATH1);
        final TreeTankWrapper wrapper2 = new TreeTankWrapper(
                ITestConstants.PATH2);
        final Map<String, TreeTankWrapper> map = new HashMap<String, TreeTankWrapper>();
        map.put(ITestConstants.REST_SERVICE1, wrapper1);
        map.put(ITestConstants.REST_SERVICE2, wrapper2);
        return map;
    }

}
