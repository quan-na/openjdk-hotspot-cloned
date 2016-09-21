/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Results of running the JstatGcTool ("jstat -gccapacity <pid>")
 *
 * Output example:
 * NGCMN    NGCMX     NGC     S0C   S1C       EC      OGCMN      OGCMX       OGC         OC       MCMN     MCMX      MC     YGC    FGC
 * 41984.0 671744.0  41984.0 5248.0 5248.0  31488.0    83968.0  1343488.0    83968.0    83968.0    512.0 110592.0   4480.0      0     0

 * Output description:
 * NGCMN   Minimum new generation capacity (KB).
 * NGCMX   Maximum new generation capacity (KB).
 * NGC         Current new generation capacity (KB).
 * S0C          Current survivor space 0 capacity (KB).
 * S1C          Current survivor space 1 capacity (KB).
 * EC            Current eden space capacity (KB).
 * OGCMN   Minimum old generation capacity (KB).
 * OGCMX   Maximum old generation capacity (KB).
 * OGC         Current old generation capacity (KB).
 * OC            Current old space capacity (KB).
 * MCMN    Minimum metaspace capacity (KB).
 * MCMX    Maximum metaspace capacity (KB).
 * MC          Current metaspace capacity (KB).
 * YGC         Number of Young generation GC Events.
 * FGC         Number of Full GC Events.
 */
package utils;

import common.ToolResults;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

public class JstatGcCapacityResults extends JstatResults {

    public JstatGcCapacityResults(ToolResults rawResults) {
        super(rawResults);
    }

    /**
     * Checks the overall consistency of the results reported by the tool
     */
    public void assertConsistency() {

        // Check exit code
        assertThat(getExitCode() == 0, "Unexpected exit code: " + getExitCode());

        // Check Young Gen consistency
        float NGCMN = getFloatValue("NGCMN");
        float NGCMX = getFloatValue("NGCMX");
        assertThat(NGCMX >= NGCMN, "NGCMN > NGCMX (min generation capacity > max generation capacity)");

        float NGC = getFloatValue("NGC");
        assertThat(NGC >= NGCMN, "NGC < NGCMN (generation capacity < min generation capacity)");
        assertThat(NGC <= NGCMX, "NGC > NGCMX (generation capacity > max generation capacity)");

        float S0C = getFloatValue("S0C");
        assertThat(S0C <= NGC, "S0C > NGC (survivor space 0 capacity > new generation capacity)");

        float S1C = getFloatValue("S1C");
        assertThat(S1C <= NGC, "S1C > NGC (survivor space 1 capacity > new generation capacity)");

        float EC = getFloatValue("EC");
        assertThat(EC <= NGC, "EC > NGC (eden space capacity > new generation capacity)");

        // Verify relative size of NGC and S0C + S1C + EC.
        // The rule depends on if the tenured GC is parallel or not.
        // For parallell GC:     NGC >= S0C + S1C + EC
        // For non-parallell GC: NGC == S0C + S1C + EC
        boolean isTenuredParallelGC = isTenuredParallelGC();
        String errMsg = String.format(
                "NGC %s (S0C + S1C + EC) (NGC = %.1f, S0C = %.1f, S1C = %.1f, EC = %.1f, (S0C + S1C + EC) = %.1f)",
                isTenuredParallelGC ? "<" : "!=", NGC, S0C, S1C, EC, S0C + S1C + EC);
        if (isTenuredParallelGC) {
            assertThat(NGC >= S0C + S1C + EC, errMsg);
        } else {
            assertThat(checkFloatIsSum(NGC, S0C, S1C, EC), errMsg);
        }

        // Check Old Gen consistency
        float OGCMN = getFloatValue("OGCMN");
        float OGCMX = getFloatValue("OGCMX");
        assertThat(OGCMX >= OGCMN, "OGCMN > OGCMX (min generation capacity > max generation capacity)");

        float OGC = getFloatValue("OGC");
        assertThat(OGC >= OGCMN, "OGC < OGCMN (generation capacity < min generation capacity)");
        assertThat(OGC <= OGCMX, "OGC > OGCMX (generation capacity > max generation capacity)");
        float OC = getFloatValue("OC");
        assertThat(OC == OGC, "OC != OGC (old generation capacity != old space capacity (these values should be equal since old space is made up only from one old generation))");

        // Check Metaspace consistency
        float MCMN = getFloatValue("MCMN");
        float MCMX = getFloatValue("MCMX");
        assertThat(MCMX >= MCMN, "MCMN > MCMX (min generation capacity > max generation capacity)");
        float MC = getFloatValue("MC");
        assertThat(MC >= MCMN, "MC < MCMN (generation capacity < min generation capacity)");
        assertThat(MC <= MCMX, "MGC > MCMX (generation capacity > max generation capacity)");


    }

    /**
     * Check if the tenured generation are currently using a parallel GC.
     */
    protected static boolean isTenuredParallelGC() {
        // Currently the only parallel GC for the tenured generation is PS MarkSweep.
        List<String> parallelGCs = Arrays.asList(new String[] { "PS MarkSweep"});
        try {
            List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean bean : beans) {
                if (parallelGCs.contains(bean.getName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final float FLOAT_COMPARISON_TOLERANCE = 0.0011f;

    private static boolean checkFloatIsSum(float sum, float... floats) {
        for (float f : floats) {
            sum -= f;
        }

        return Math.abs(sum) <= FLOAT_COMPARISON_TOLERANCE;
    }

    private void assertThat(boolean b, String message) {
        if (!b) {
            throw new RuntimeException(message);
        }
    }

}
