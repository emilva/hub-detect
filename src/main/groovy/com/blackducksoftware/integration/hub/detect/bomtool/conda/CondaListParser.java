/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.bomtool.conda;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class CondaListParser {
    @Autowired
    private Gson gson;

    @Autowired
    private ExternalIdFactory externalIdFactory;

    public DependencyGraph parse(final String listJsonText, final String infoJsonText) {
        final Type listType = new TypeToken<ArrayList<CondaListElement>>() {}.getType();
        final List<CondaListElement> condaList = gson.fromJson(listJsonText, listType);
        final CondaInfo condaInfo = gson.fromJson(infoJsonText, CondaInfo.class);
        final String platform = condaInfo.getPlatform();

        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        for (CondaListElement condaListElement : condaList) {
            graph.addChildToRoot(condaListElementToDependency(platform, condaListElement));
        }

        return graph;
    }

    public Dependency condaListElementToDependency(final String platform, final CondaListElement element) {
        String name = element.getName();
        String version = String.format("%s-%s-%s",element.getVersion(),element.getBuildString(),platform);
        ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.ANACONDA, name, version);

        return new Dependency(name, version, externalId);
    }

}
