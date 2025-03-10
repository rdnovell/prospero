/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.prospero.cli.commands;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;

import org.wildfly.prospero.cli.CliConsole;
import org.wildfly.prospero.cli.ReturnCodes;
import picocli.CommandLine;

@CommandLine.Command(name = CliConstants.Commands.MAIN_COMMAND, resourceBundle = "UsageMessages",
        versionProvider = MainCommand.VersionProvider.class)
public class MainCommand implements Callable<Integer> {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    private final CliConsole console;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {CliConstants.H, CliConstants.HELP}, usageHelp = true)
    boolean help;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {CliConstants.V, CliConstants.VERSION}, versionHelp = true)
    boolean version;

    public MainCommand(CliConsole console) {
        this.console = console;
    }

    @Override
    public Integer call() throws IOException {
        // print welcome message - this is not printed when -h option is set
        PropertyResourceBundle usageBundle = new PropertyResourceBundle(
                getClass().getResourceAsStream("/UsageMessages.properties"));

        console.println(CommandLine.Help.Ansi.AUTO.string(usageBundle.getString("prospero.welcomeMessage")));

        // print main command usage
        spec.commandLine().usage(console.getStdOut());
        return ReturnCodes.SUCCESS;
    }

    static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() throws Exception {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Manifest manifest = new Manifest(url.openStream());
                if ("prospero-cli".equals(manifest.getMainAttributes().getValue("Specification-Title"))) {
                    return new String[]{manifest.getMainAttributes().getValue("Implementation-Version")};
                }
            }

            return new String[] {"unknown"};
        }
    }
}
