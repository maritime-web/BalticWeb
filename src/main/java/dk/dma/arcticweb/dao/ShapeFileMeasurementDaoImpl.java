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

import dk.dma.embryo.dao.DaoImpl;
import dk.dma.embryo.domain.ShapeFileMeasurement;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class ShapeFileMeasurementDaoImpl extends DaoImpl implements ShapeFileMeasurementDao {
    public ShapeFileMeasurement lookup(String fn, String prefix) {
        TypedQuery<ShapeFileMeasurement> query = em.createNamedQuery("ShapeFileMeasurement:lookup", ShapeFileMeasurement.class);

        query.setParameter("fileName", fn);
        query.setParameter("prefix", prefix);
        query.setMaxResults(1);

        List<ShapeFileMeasurement> result = query.getResultList();

        return getSingleOrNull(result);
    }

    public void deleteAll(String prefix) {
        Query query = em.createNamedQuery("ShapeFileMeasurement:deleteAll");
        query.setParameter("prefix", prefix);
        query.executeUpdate();
    }

    public List<ShapeFileMeasurement> list(String prefix) {
        TypedQuery<ShapeFileMeasurement> query = em.createNamedQuery("ShapeFileMeasurement:list", ShapeFileMeasurement.class);

        query.setParameter("prefix", prefix);

        return query.getResultList();
    }
}
