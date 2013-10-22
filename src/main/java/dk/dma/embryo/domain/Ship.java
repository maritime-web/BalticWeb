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
package dk.dma.embryo.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@NamedQueries({ @NamedQuery(name = "Ship:getByMmsi", query = "SELECT s FROM Ship s WHERE s.mmsi = :mmsi"),
        @NamedQuery(name = "Ship:getByMaritimeId", query = "SELECT s FROM Ship s WHERE s.maritimeId = :maritimeId"),
        @NamedQuery(name = "Ship:getByCallsign", query = "SELECT s FROM Ship s WHERE s.aisData.callsign = :callsign"),
        @NamedQuery(name = "Ship:getMmsiList", query = "SELECT s FROM Ship s WHERE s.mmsi in :mmsiNumbers") })
public class Ship extends BaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(unique = true)
    private String maritimeId;

    @Column(nullable = true)
    private Long mmsi;

    @Column(nullable = true, length = 32)
    private String type;

    @Min(0)
    @Max(200)
    @Column(nullable = true)
    private BigDecimal maxSpeed;

    @Min(0)
    @Column(nullable = true)
    private Integer grossTonnage;

    @Column(nullable = true, length = 64)
    private String commCapabilities;

    @Min(0)
    @Column(nullable = true)
    private Integer persons;

    @Column(nullable = true, length = 32)
    private String iceClass;

    @Column(nullable = true)
    private Boolean helipad;

    @OneToOne(mappedBy = "vessel", cascade = { CascadeType.ALL })
    private VoyagePlan voyagePlan;

    @OneToOne(cascade = { CascadeType.ALL })
    private Voyage activeVoyage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private VesselOwnerRole owner;

    private AisData aisData = new AisData();

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Ship(String maritimeId) {
        this.maritimeId = maritimeId;
    }

    public Ship() {
        this(UUID.randomUUID().toString());
    }

    public Ship(Long mmsi) {
        this();
        this.mmsi = mmsi;
    }

}
