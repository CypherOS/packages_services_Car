/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.car.apitest;

import android.car.Car;
import android.car.hardware.hvac.CarHvacManager;
import android.car.hardware.CarPropertyConfig;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@MediumTest
public class CarHvacManagerTest extends CarApiTestBase {
    private static final String TAG = CarHvacManagerTest.class.getSimpleName();

    private CarHvacManager mHvacManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mHvacManager = (CarHvacManager) getCar().getCarManager(Car.HVAC_SERVICE);
        assertNotNull(mHvacManager);
    }

    public void testAllHvacProperties() throws Exception {
        List<CarPropertyConfig> properties = mHvacManager.getPropertyList();
        Set<Class> supportedTypes = new HashSet<>(Arrays.asList(
                new Class[] { Integer.class, Float.class, Boolean.class }));

        for (CarPropertyConfig property : properties) {
            if (supportedTypes.contains(property.getPropertyType())) {
                assertTypeAndZone(property);
            } else {
                fail("Type is not supported for " + property);
            }
        }
    }

    private void assertTypeAndZone(CarPropertyConfig property) {
        switch (property.getPropertyId()) {
            case CarHvacManager.ID_MIRROR_DEFROSTER_ON: // non-zoned bool
                assertEquals(Boolean.class, property.getPropertyType());
                assertTrue(property.isGlobalProperty());
                break;
            case CarHvacManager.ID_STEERING_WHEEL_TEMP: // non-zoned int
            case CarHvacManager.ID_TEMPERATURE_UNITS:
                assertEquals(Integer.class, property.getPropertyType());
                assertTrue(property.isGlobalProperty());
                checkIntMinMax(property);
                break;
            case CarHvacManager.ID_OUTSIDE_AIR_TEMP:
                assertEquals(Float.class, property.getPropertyType());
                assertTrue(property.isGlobalProperty());
                checkFloatMinMax(property);
                break;
            case CarHvacManager.ID_ZONED_TEMP_SETPOINT: // zoned float
            case CarHvacManager.ID_ZONED_TEMP_ACTUAL:
                assertEquals(Float.class, property.getPropertyType());
                assertFalse(property.isGlobalProperty());
                checkFloatMinMax(property);
                break;
            case CarHvacManager.ID_ZONED_FAN_SPEED_SETPOINT: // zoned int
            case CarHvacManager.ID_ZONED_FAN_SPEED_RPM:
            case CarHvacManager.ID_ZONED_FAN_POSITION_AVAILABLE:
            case CarHvacManager.ID_ZONED_FAN_POSITION:
            case CarHvacManager.ID_ZONED_SEAT_TEMP:
                assertEquals("Wrong type, expecting Int type for id:" + property.getPropertyId(),
                    Integer.class, property.getPropertyType());
                assertFalse("Wrong zone, should not be global for id: " + property.getPropertyId() +
                    ", area type:" + property.getAreaType(), property.isGlobalProperty());
                checkIntMinMax(property);
                break;
            case CarHvacManager.ID_ZONED_AC_ON: // zoned boolean
            case CarHvacManager.ID_ZONED_AUTOMATIC_MODE_ON:
            case CarHvacManager.ID_ZONED_AIR_RECIRCULATION_ON:
            case CarHvacManager.ID_ZONED_MAX_AC_ON:
            case CarHvacManager.ID_ZONED_DUAL_ZONE_ON:
            case CarHvacManager.ID_ZONED_MAX_DEFROST_ON:
            case CarHvacManager.ID_ZONED_HVAC_POWER_ON:
            case CarHvacManager.ID_WINDOW_DEFROSTER_ON:
                assertEquals(Boolean.class, property.getPropertyType());
                assertFalse(property.isGlobalProperty());
                break;
        }
    }

    private void checkIntMinMax(CarPropertyConfig<Integer> property) {
        Log.i(TAG, "checkIntMinMax property:" + property);
        if (!property.isGlobalProperty()) {
            int[] areaIds = property.getAreaIds();
            assertTrue(areaIds.length > 0);
            assertEquals(areaIds.length, property.getAreaCount());

            for (int areId : areaIds) {
                assertTrue(property.hasArea(areId));
                int min = property.getMinValue(areId);
                int max = property.getMaxValue(areId);
                assertTrue(min <= max);
            }
        } else {
            int min = property.getMinValue();
            int max = property.getMaxValue();
            assertTrue(min <= max);
            for (int i = 0; i < 32; i++) {
                assertFalse(property.hasArea(0x1 << i));
                assertNull(property.getMinValue(0x1 << i));
                assertNull(property.getMaxValue(0x1 << i));
            }
        }
    }

    private void checkFloatMinMax(CarPropertyConfig<Float> property) {
        Log.i(TAG, "checkFloatMinMax property:" + property);
        if (!property.isGlobalProperty()) {
            int[] areaIds = property.getAreaIds();
            assertTrue(areaIds.length > 0);
            assertEquals(areaIds.length, property.getAreaCount());

            for (int areId : areaIds) {
                assertTrue(property.hasArea(areId));
                float min = property.getMinValue(areId);
                float max = property.getMaxValue(areId);
                assertTrue(min <= max);
            }
        } else {
            float min = property.getMinValue();
            float max = property.getMaxValue();
            assertTrue(min <= max);
            for (int i = 0; i < 32; i++) {
                assertFalse(property.hasArea(0x1 << i));
                assertNull(property.getMinValue(0x1 << i));
                assertNull(property.getMaxValue(0x1 << i));
            }
        }
    }
}