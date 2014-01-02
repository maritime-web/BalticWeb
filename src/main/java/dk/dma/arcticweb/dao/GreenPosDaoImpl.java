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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.dao.DaoImpl;
import dk.dma.embryo.domain.GreenPosReport;
import dk.dma.embryo.domain.GreenposMinimal;
import dk.dma.embryo.domain.GreenposSearch;

@Stateless
public class GreenPosDaoImpl extends DaoImpl implements GreenPosDao {

    public GreenPosDaoImpl() {
        super();
    }

    public GreenPosDaoImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public GreenPosReport findLatest(Long vesselMmsi) {
        TypedQuery<GreenPosReport> query = em.createNamedQuery("GreenPosReport:findLatest", GreenPosReport.class);
        query.setParameter("vesselMmsi", vesselMmsi);
        query.setMaxResults(1);

        List<GreenPosReport> result = query.getResultList();

        return getSingleOrNull(result);
    }

    @Override
    public List<GreenposMinimal> getLatest() {
        Query query = em.createNativeQuery("SELECT vesselName, vesselMmsi, DTYPE, max(ts) FROM GreenPosReport WHERE DATE(:date) <= DATE(ts) GROUP BY vesselName");

        query.setParameter("date", new Date(System.currentTimeMillis() - 7 * 24 * 3600 * 1000L), TemporalType.TIMESTAMP);

        List<Object[]> list = (List<Object[]>) query.getResultList();
        List<GreenposMinimal> result = new ArrayList<>(list.size());
        Calendar cal = Calendar.getInstance();
        int offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
        for (Object[] greenpos : list) {
            DateTime datetime = new DateTime(((Date) greenpos[3]).getTime() + offset, DateTimeZone.UTC);
            result.add(new GreenposMinimal((String) greenpos[0], Long.parseLong(greenpos[1].toString()), (String) greenpos[2], datetime.toDate()));
        }

        return result;
    }

    @Override
    public List<GreenPosReport> find(GreenposSearch search) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GreenPosReport> criteriaQuery = builder.createQuery(GreenPosReport.class);
        Root<GreenPosReport> root = criteriaQuery.from(GreenPosReport.class);
        criteriaQuery.select(root);

        List<Predicate> criterias = new LinkedList<>();

        if (search.getVesselMmsi() != null) {
            criterias.add(builder.equal(root.get("vesselMmsi"), search.getVesselMmsi()));
        }

        Predicate[] criteriaArr = new Predicate[criterias.size()];
        criteriaArr = criterias.toArray(criteriaArr);

        criteriaQuery.where(criteriaArr);

        Expression<String> field = root.get(search.getSortByField());
        Order order = "ASC".equals(search.getSortOrder()) ? builder.asc(field) : builder.desc(field);
        criteriaQuery.orderBy(order);

        TypedQuery<GreenPosReport> reports = em.createQuery(criteriaQuery);
        reports.setFirstResult(search.getFirst());
        reports.setMaxResults(search.getNumberOfReports());

        return reports.getResultList();
    }

    @Override
    public GreenPosReport findById(String id) {
        TypedQuery<GreenPosReport> query = em.createNamedQuery("GreenPosReport:findById", GreenPosReport.class);
        query.setParameter("id", id);
        query.setMaxResults(1);

        List<GreenPosReport> result = query.getResultList();

        return getSingleOrNull(result);
    }

}
