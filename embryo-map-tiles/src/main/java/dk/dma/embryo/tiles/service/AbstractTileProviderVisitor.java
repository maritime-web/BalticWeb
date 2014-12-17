/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.tiles.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import dk.dma.embryo.common.configuration.Provider;
import dk.dma.embryo.common.configuration.ProviderVisitor;
import dk.dma.embryo.common.configuration.Type;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.tiles.model.TileSet;

/**
 * Created by Jesper Tejlgaard on 11/10/14.
 */
public abstract class AbstractTileProviderVisitor implements ProviderVisitor {
    protected final DateTime limit;
    protected final EmbryoLogService embryoLogService;
    protected final Result result = new Result();
    protected final TileSetDao tileSetDao;
    private final Map<String, Map<String, TileSet>> tileSets = new HashMap<>();

    public AbstractTileProviderVisitor(DateTime limit, TileSetDao tileSetDao, EmbryoLogService embryoLogService) {
        this.limit = limit;
        this.tileSetDao = tileSetDao;
        this.embryoLogService = embryoLogService;
    }

    protected Provider currentProvider;

    @Override
    public void visit(Provider provider) {
        this.currentProvider = provider;
    }

    protected Map<String, TileSet> getTileSets(Type type) {
        if (!tileSets.containsKey(type.getName())) {
            List<TileSet> images = tileSetDao.listByProviderAndType(currentProvider.getShortName(), type.getName());
            Map<String, TileSet> imageMap = TileSet.toMap(images);
            tileSets.put(type.getName(), imageMap);
        }
        return tileSets.get(type.getName());
    }

    public Result getResult() {
        return this.result;
    }
}
