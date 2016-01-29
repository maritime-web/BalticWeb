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
package dk.dma.arcticweb.rest;

import javax.enterprise.context.RequestScoped;

import dk.dma.arcticweb.reporting.json.GreenposVesselDetailsAmendment;
import dk.dma.embryo.vessel.json.VesselRestService;

/**
 * Class is an ugly workaround to the fact, that it is impossible to add CDI interceptors to a class in a different CDI
 * jar archive. Extending and enabling as CDI bean it possible to apply interceptor (e.g. {@link GreenposVesselDetailsAmendment}).
 * 
 * @author Jesper Tejlgaard
 */
@RequestScoped
public class ArcticWebVesselRestService extends VesselRestService {

}
