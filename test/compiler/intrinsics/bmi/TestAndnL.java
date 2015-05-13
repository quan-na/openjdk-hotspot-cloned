/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

/**
 * @test
 * @bug 8031321
 * @summary Verify that results of computations are the same w/
 *          and w/o usage of ANDN instruction
 * @library /testlibrary /../../test/lib
 * @modules java.base/sun.misc
 *          java.management
 * @build TestAndnL BMITestRunner Expr
 * @run main ClassFileInstaller sun.hotspot.WhiteBox
 *                              sun.hotspot.WhiteBox$WhiteBoxPermission
 * @run main/othervm -Xbootclasspath/a:. -XX:+UnlockDiagnosticVMOptions
 *                   -XX:+WhiteBoxAPI TestAndnL
 */

import sun.hotspot.cpuinfo.CPUInfo;

public class TestAndnL {

    public static void main(String args[]) throws Throwable {
        if (!CPUInfo.hasFeature("bmi1")) {
            System.out.println("INFO: CPU does not support bmi1 feature.");
        }

        BMITestRunner.runTests(AndnLExpr.class, args,
                               "-XX:+IgnoreUnrecognizedVMOptions",
                               "-XX:+UseBMI1Instructions");
        BMITestRunner.runTests(AndnLCommutativeExpr.class, args,
                              "-XX:+IgnoreUnrecognizedVMOptions",
                               "-XX:+UseBMI1Instructions");
    }

    public static class AndnLExpr extends Expr.BMIBinaryLongExpr {

        public long longExpr(long src1, long src2) {
            return ~src1 & src2;
        }

        public long longExpr(long src1, Expr.MemL src2) {
            return ~src1 & src2.value;
        }

        public long longExpr(Expr.MemL src1, long src2) {
            return ~src1.value & src2;
        }

        public long longExpr(Expr.MemL src1, Expr.MemL src2) {
            return ~src1.value & src2.value;
        }


    }

    public static class AndnLCommutativeExpr extends Expr.BMIBinaryLongExpr {

        public long longExpr(long src1, long src2) {
            return src1 & ~src2;
        }

        public long longExpr(long src1, Expr.MemL src2) {
            return src1 & ~src2.value;
        }

        public long longExpr(Expr.MemL src1, long src2) {
            return src1.value & ~src2;
        }

        public long longExpr(Expr.MemL src1, Expr.MemL src2) {
            return src1.value & ~src2.value;
        }

    }

}