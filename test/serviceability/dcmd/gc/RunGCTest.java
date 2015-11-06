/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jdk.test.lib.OutputAnalyzer;
import jdk.test.lib.dcmd.CommandExecutor;
import jdk.test.lib.dcmd.JMXExecutor;

/*
 * @test
 * @summary Test of diagnostic command GC.run
 * @library /testlibrary
 * @modules java.base/sun.misc
 *          java.compiler
 *          java.management
 *          jdk.jvmstat/sun.jvmstat.monitor
 * @build jdk.test.lib.*
 * @build jdk.test.lib.dcmd.*
 * @run testng/othervm -XX:+PrintGCDetails -Xloggc:RunGC.gclog -XX:-ExplicitGCInvokesConcurrent RunGCTest
 */
public class RunGCTest {
    public void run(CommandExecutor executor) {
        executor.execute("GC.run");

        Path gcLogPath = Paths.get("RunGC.gclog").toAbsolutePath();
        String gcLog = null;

        try {
            gcLog = new String(Files.readAllBytes(gcLogPath));
        } catch (IOException e) {
            Assert.fail("Test error: Could not read GC log file: " + gcLogPath, e);
        }

        OutputAnalyzer output = new OutputAnalyzer(gcLog, "");
        output.shouldContain("[Full GC (Diagnostic Command)");
    }

    @Test
    public void jmx() {
        run(new JMXExecutor());
    }
}
