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

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Type;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.tiles.model.TileSet;

/**
 * Created by Jesper Tejlgaard on 11/10/14.
 */
public class MarkTileSetsForDeletionVisitor extends AbstractTileProviderVisitor {
    private final Logger logger = LoggerFactory.getLogger(MarkTileSetsForDeletionVisitor.class);

    public MarkTileSetsForDeletionVisitor(DateTime limit, TileSetDao tileSetDao, EmbryoLogService embryoLogService) {
        super(limit, tileSetDao, embryoLogService);
    }

    @Override
    public void visit(Type type) {
        try {
            List<TileSet> images = tileSetDao.listByProviderAndType(currentProvider.getShortName(), type.getName());
            for (TileSet tileSet : images) {
                if (tileSet.getTs().isBefore(limit) && tileSet.getStatus() != TileSet.Status.CONVERTING && tileSet.getStatus() != TileSet.Status.DELETING) {
                    try {
                        tileSet.setStatus(TileSet.Status.DELETING);
                        tileSetDao.saveEntity(tileSet);
                        result.deleted++;
                    } catch (Exception e) {
                        String msg = "Fatal error marking tile set entry " + tileSet.getName() + " as being deleted";
                        logger.error(msg, e);
                        embryoLogService.error(msg, e);
                        result.errorCount++;
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Fatal error marking tile sets as being deleted for provider " + currentProvider.getShortName() + " and type " + type.getName();
            logger.error(msg, e);
            embryoLogService.error(msg, e);
            result.errorCount++;
        }
    }
}
