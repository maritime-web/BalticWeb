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
package dk.dma.embryo.dataformats.transform;

import java.util.ArrayList;
import java.util.List;

import dk.dma.embryo.dataformats.model.IceObservation;
import dk.dma.embryo.dataformats.model.ShapeFileMeasurement;

/**
 * @author Jesper Tejlgaard
 */
public class Shape2IceListTransformer {

    private final Shape2IceTransformerFactory factory;

    public Shape2IceListTransformer(Shape2IceTransformerFactory factory) {
        this.factory = factory;
    }

    public List<IceObservation> transform(List<ShapeFileMeasurement> shapes) {
        List<IceObservation> result = new ArrayList<>(shapes.size());

        for (ShapeFileMeasurement sfm : shapes) {
            Shape2IceTransformer transformer = factory.createTransformer(sfm.getProvider());
            IceObservation observation = transformer.transform(sfm);
            if (System.currentTimeMillis() - observation.getDate().getTime() < 3600 * 1000L * 24 * 30) {
                result.add(observation);
            }
        }

        return result;
    }

}
