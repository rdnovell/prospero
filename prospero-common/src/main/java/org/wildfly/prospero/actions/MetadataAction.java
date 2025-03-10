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

package org.wildfly.prospero.actions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.wildfly.channel.Channel;
import org.wildfly.prospero.Messages;
import org.wildfly.prospero.api.InstallationMetadata;
import org.wildfly.prospero.api.exceptions.MetadataException;
import org.wildfly.prospero.model.ProsperoConfig;

/**
 * Metadata related actions wrapper.
 */
public class MetadataAction implements AutoCloseable {
    private final InstallationMetadata installationMetadata;

    public MetadataAction(Path installation) throws MetadataException {
        this.installationMetadata = InstallationMetadata.loadInstallation(installation);
    }

    protected MetadataAction(InstallationMetadata installationMetadata) {
        this.installationMetadata = installationMetadata;
    }

    public void addChannel(Channel channel) throws MetadataException {
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();

        if (channels.stream().filter(c->c.getName().equals(channel.getName())).findAny().isPresent()) {
            throw Messages.MESSAGES.channelExists(channel.getName());
        }

        channels.add(channel);
        installationMetadata.updateProsperoConfig(prosperoConfig);
    }

    public void removeChannel(String channelName) throws MetadataException {
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();
        final Optional<Channel> removedChannel = channels.stream().filter(c -> c.getName().equals(channelName)).findAny();
        if (removedChannel.isEmpty()) {
            throw Messages.MESSAGES.channelNotFound(channelName);
        }
        channels.remove(removedChannel.get());
        installationMetadata.updateProsperoConfig(prosperoConfig);
    }

    public void changeChannel(String channelName, Channel newChannel) throws MetadataException {
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();
        final Optional<Channel> modifiedChannel = channels.stream().filter(c -> c.getName().equals(channelName)).findAny();
        if (modifiedChannel.isEmpty()) {
            throw Messages.MESSAGES.channelNotFound(channelName);
        }
        channels.set(channels.indexOf(modifiedChannel.get()), newChannel);
        installationMetadata.updateProsperoConfig(prosperoConfig);
    }

    public List<Channel> getChannels() throws MetadataException {
        return new ArrayList<>(installationMetadata.getProsperoConfig().getChannels());
    }

    public Channel getChannel(int index) throws MetadataException {
        if (index < 0) {
            return null;
        }
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();
        if (channels.size() <= index) {
            return null;
        }
        return channels.get(index);
    }

    @Override
    public void close() {
        this.installationMetadata.close();
    }
}
