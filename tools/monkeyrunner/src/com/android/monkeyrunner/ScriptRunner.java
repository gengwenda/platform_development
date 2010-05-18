/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.monkeyrunner;

import com.google.common.collect.Lists;

import org.python.core.PyObject;
import org.python.util.InteractiveConsole;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


/**
 * Runs Jython based scripts.
 */
public class ScriptRunner {

    /** The "this" scope object for scripts. */
    private final Object scope;
    private final String variable;

    /** Private constructor. */
    private ScriptRunner(Object scope, String variable) {
        this.scope = scope;
        this.variable = variable;
    }

    /** Creates a new instance for the given scope object. */
    public static ScriptRunner newInstance(Object scope, String variable) {
        return new ScriptRunner(scope, variable);
    }

    /**
     * Runs the specified Jython script. First runs the initialization script to
     * preload the appropriate client library version.
     */
    public static void run(String scriptfilename) {
        try {
            // Add the current directory of the script to the python.path search path.
            File f = new File(scriptfilename);
            initPython(Lists.newArrayList(f.getParent()),
                    new String[] { f.getCanonicalPath() });

            PythonInterpreter python = new PythonInterpreter();

            python.execfile(scriptfilename);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void runString(String script) {
        initPython();
        PythonInterpreter python = new PythonInterpreter();
        python.exec(script);
    }

    private static void initPython() {
        List<String> arg = Collections.emptyList();
        initPython(arg, new String[] {""});
    }

    private static void initPython(List<String> pythonPath,
            String[] argv) {
        Properties props = new Properties();

        // Build up the python.path
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("java.class.path"));
        for (String p : pythonPath) {
            sb.append(":").append(p);
        }
        props.setProperty("python.path", sb.toString());

        /** Initialize the python interpreter. */
        // Default is 'message' which displays sys-package-mgr bloat
        // Choose one of error,warning,message,comment,debug
        props.setProperty("python.verbose", "error");

        PythonInterpreter.initialize(System.getProperties(), props, argv);
    }

    /**
     * Create and run a console using a new python interpreter for the test
     * associated with this instance.
     */
    public void console() throws IOException {
        initPython();
        InteractiveConsole python = new InteractiveConsole();
        initInterpreter(python, scope, variable);
        python.interact();
    }

    /**
     * Start an interactive python interpreter using the specified set of local
     * variables. Use this to interrupt a running test script with a prompt:
     *
     * @param locals
     */
    public static void console(PyObject locals) {
        initPython();
        InteractiveConsole python = new InteractiveConsole(locals);
        python.interact();
    }

    /**
     * Initialize a python interpreter.
     *
     * @param python
     * @param scope
     * @throws IOException
     */
    public static void initInterpreter(PythonInterpreter python, Object scope, String variable)
    throws IOException {
        // Store the current test case as the this variable
        python.set(variable, scope);
    }
}
