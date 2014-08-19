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
package dk.dma.arcticweb.reporting.persistence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

import dk.dma.arcticweb.reporting.model.GreenPosReport;
import dk.dma.arcticweb.reporting.model.GreenposMinimal;
import dk.dma.arcticweb.reporting.model.GreenposSearch;
import dk.dma.embryo.common.persistence.DaoImpl;

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
        Query query = em.createNativeQuery("SELECT vesselName, vesselMmsi, DTYPE, max(ts) FROM (SELECT vesselName, vesselMmsi, DTYPE, ts FROM GreenPosReport WHERE DATE(:date) <= DATE(ts) ORDER BY ts DESC) rep GROUP BY vesselName");

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
