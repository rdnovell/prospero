/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.prospero.cli.commands;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.wildfly.prospero.actions.Console;
import org.wildfly.prospero.actions.InstallationHistoryAction;
import org.wildfly.prospero.api.ArtifactChange;
import org.wildfly.prospero.api.SavedState;
import org.wildfly.prospero.cli.AbstractConsoleTest;
import org.wildfly.prospero.cli.ActionFactory;
import org.wildfly.prospero.cli.CliMessages;
import org.wildfly.prospero.cli.ReturnCodes;
import org.wildfly.prospero.test.MetadataTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HistoryCommandTest extends AbstractConsoleTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock
    private InstallationHistoryAction historyAction;

    private Path installationDir;

    @Override
    protected ActionFactory createActionFactory() {
        return new ActionFactory() {
            @Override
            public InstallationHistoryAction history(Path targetPath, Console console) {
                return historyAction;
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        installationDir = tempDir.newFolder().toPath();
        MetadataTestUtils.createInstallationMetadata(installationDir);
        MetadataTestUtils.createGalleonProvisionedState(installationDir);
    }

    @Test
    public void currentDirNotValidInstallation() {
        int exitCode = commandLine.execute(CliConstants.Commands.HISTORY);
        Assert.assertEquals(ReturnCodes.INVALID_ARGUMENTS, exitCode);
        assertTrue(getErrorOutput().contains(CliMessages.MESSAGES.invalidInstallationDir(HistoryCommand.currentDir())
                .getMessage()));
    }

    @Test
    public void displayListOfStates() throws Exception {
        when(historyAction.getRevisions()).thenReturn(Arrays.asList(
                new SavedState("abcd", Instant.ofEpochSecond(System.currentTimeMillis()), SavedState.Type.INSTALL)));

        int exitCode = commandLine.execute(CliConstants.Commands.HISTORY, CliConstants.DIR, installationDir.toString());
        assertEquals(ReturnCodes.SUCCESS, exitCode);
        verify(historyAction).getRevisions();
        assertTrue(getStandardOutput().contains("abcd"));
    }

    @Test
    public void displayDetailsOfStateIfRevisionSet() throws Exception {
        when(historyAction.compare(any())).thenReturn(Arrays.asList(new ArtifactChange(
                        new DefaultArtifact("foo", "bar", "jar", "1.1"),
                        new DefaultArtifact("foo", "bar", "jar", "1.2"))));

        int exitCode = commandLine.execute(CliConstants.Commands.HISTORY, CliConstants.DIR, installationDir.toString(),
                CliConstants.REVISION, "abcd");
        assertEquals(ReturnCodes.SUCCESS, exitCode);
        verify(historyAction).compare(eq(new SavedState("abcd")));
        assertTrue(getStandardOutput().contains("foo:bar"));
    }
}