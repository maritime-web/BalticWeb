/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.dao;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import dk.dma.embryo.common.persistence.DaoImpl;
import dk.dma.embryo.domain.ShapeFileMeasurement;

public class ShapeFileMeasurementDaoImpl extends DaoImpl implements ShapeFileMeasurementDao {
    public ShapeFileMeasurement lookup(String fn, String provider) {
        TypedQuery<ShapeFileMeasurement> query = em.createNamedQuery("ShapeFileMeasurement:lookup", ShapeFileMeasurement.class);

        query.setParameter("fileName", fn);
        query.setParameter("provider", provider);
        query.setMaxResults(1);

        List<ShapeFileMeasurement> result = query.getResultList();

        return getSingleOrNull(result);
    }

    public void deleteAll(String provider) {
        Query query = em.createNamedQuery("ShapeFileMeasurement:deleteAll");
        query.setParameter("provider", provider);
        query.executeUpdate();
    }

    public List<ShapeFileMeasurement> list(String provider) {
        TypedQuery<ShapeFileMeasurement> query = em.createNamedQuery("ShapeFileMeasurement:list", ShapeFileMeasurement.class);

        query.setParameter("provider", provider);

        return query.getResultList();
    }
}
