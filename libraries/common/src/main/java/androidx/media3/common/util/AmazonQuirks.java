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

package androidx.media3.common.util;

import android.os.Build;
import android.util.Log;

public class AmazonQuirks {
    //ordering of the static initialization is important
    private static final String TAG = AmazonQuirks.class.getSimpleName();
    private static final String FIRETV_GEN1_DEVICE_MODEL        = "AFTB";
    private static final String FIRETV_GEN2_DEVICE_MODEL        = "AFTS";
    private static final String FIRETV_STICK_DEVICE_MODEL       = "AFTM";
    private static final String FIRETV_STICK_GEN2_DEVICE_MODEL  = "AFTT";
    private static final String KINDLE_TABLET_DEVICE_MODEL      = "KF";
    private static final String FIRE_PHONE_DEVICE_MODEL         = "SD";
    private static final String AMAZON                          = "Amazon";

    private static final String DEVICEMODEL = Build.MODEL;
    private static final String MANUFACTURER = Build.MANUFACTURER;

    private static final int AUDIO_HARDWARE_LATENCY_FOR_TABLETS = 90000;

    //caching
    private static final boolean isAmazonDevice ;
    private static final boolean isFireTVGen1;
    private static final boolean isFireTVStick;
    private static final boolean isFireTVGen2;
    private static final boolean isKindleTablet;
    private static final boolean isFirePhone;

    // This static block must be the last
    // INIT ORDERING IS IMPORTANT IN THIS BLOCK!
    static {
        isAmazonDevice = MANUFACTURER.equalsIgnoreCase(AMAZON);
        isFireTVGen1   = isAmazonDevice && DEVICEMODEL.equalsIgnoreCase(FIRETV_GEN1_DEVICE_MODEL);
        isFireTVGen2   = isAmazonDevice && DEVICEMODEL.equalsIgnoreCase(FIRETV_GEN2_DEVICE_MODEL);
        isFireTVStick  = isAmazonDevice && DEVICEMODEL.equalsIgnoreCase(FIRETV_STICK_DEVICE_MODEL);
        isKindleTablet = isAmazonDevice && DEVICEMODEL.startsWith(KINDLE_TABLET_DEVICE_MODEL);
        isFirePhone    = isAmazonDevice && DEVICEMODEL.startsWith(FIRE_PHONE_DEVICE_MODEL);
    }

    private AmazonQuirks() {}

    public static boolean isDolbyPassthroughQuirkEnabled() {
        // Sets dolby passthrough quirk for Amazon Fire TV (Gen 1) Family
        return isFireTVGen1Family();
    }

    public static boolean isAmazonDevice() {
        return isAmazonDevice;
    }

    public static boolean isFireTVGen1Family() {
        return isFireTVGen1 || isFireTVStick;
    }

    public static boolean isFireTVGen2() {
        return isFireTVGen2;
    }

    // We assume that this function is called only for supported
    // passthrough mimetypes such as AC3, EAC3 etc
    public static boolean useDefaultPassthroughDecoder() {
        //use platform decoder only for
        // - FireTV Gen1
        // - FireTV Stick
        if(isFireTVGen1Family()) {
            Log.i(TAG, "Using platform Dolby decoder");
            return false;
        }
        Log.i(TAG, "Using default Dolby pass-through decoder");
        return true;
    }
    
    public static boolean isLatencyQuirkEnabled() {
        // Sets latency quirk for Amazon KK and JB Tablets and Fire Phone
        return (Util.SDK_INT <= 19) && (isKindleTablet || isFirePhone);
    }
 
    public static int getAudioHWLatency() {
        // this function is called only when the above function
        // returns true for latency quirk. So no need to check for
        // SDK version and device type again
        return AUDIO_HARDWARE_LATENCY_FOR_TABLETS;
    }
}
