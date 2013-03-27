package de.shop.bestellverwaltung.domain;

import static de.shop.util.Constants.KEINE_ID;
import static de.shop.util.Constants.MIN_ID;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.TemporalType.DATE;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.validator.constraints.NotEmpty;

import de.shop.util.IdGroup;
import de.shop.util.PreExistingGroup;

@Entity
@Table(name = "lieferung")

@NamedQueries({
	@NamedQuery(name  = Lieferung.FIND_LIEFERUNGEN_BY_LIEFERNR_FETCH_BESTELLUNGEN,
                query = "SELECT l"
                	    + " FROM Lieferung l LEFT JOIN FETCH l.bestellungen"
			            + " WHERE l.liefernr LIKE :" + Lieferung.PARAM_LIEFERNR),
})
@XmlRootElement
public class Lieferung implements Serializable {
	private static final long serialVersionUID = -205683416841761248L;
	
	private static final int LIEFERNR_LENGTH = 12;
	private static final int TRANSPORTUNTERNEHMEN_LENGTH = 12;
	
	private static final String PREFIX = "Lieferung.";
	public static final String FIND_LIEFERUNGEN_BY_LIEFERNR_FETCH_BESTELLUNGEN =
            PREFIX + "findLieferungenByLieferNrFetchBestellungen";
	public static final String FIND_LIEFERUNGEN_BY_AKTUALISIERT =
            PREFIX + "findLieferungenByAktualisiert";
	public static final String PARAM_LIEFERNR = "liefernr";
	public static final String PARAM_AKTUALISIERT = "aktualisiert";
	
	@Id
	@GeneratedValue
	@Column(name = "l_id", unique = true, nullable = false, updatable = false)
	@Min(value = MIN_ID, message = "{bestellverwaltung.lieferung.id.min}", groups = IdGroup.class)
	@XmlAttribute
	private Long id = KEINE_ID;
	
	@Column(length = LIEFERNR_LENGTH, unique = true)
	@NotNull(message = "{bestellverwaltung.lieferung.liefernr.notNull}")
	@XmlElement(required = true)
	private String liefernr;

	@Column(name = "transport_unternehmen", length = TRANSPORTUNTERNEHMEN_LENGTH, unique = true)
	@XmlElement
	private String transportUnternehmen;

	@ManyToMany(mappedBy = "lieferungen", cascade = { PERSIST, MERGE })
	@NotEmpty(message = "{bestellverwaltung.lieferung.bestellungen.notEmpty}",
	groups = PreExistingGroup.class)
	@Valid
	@XmlTransient
	private List<Bestellung> bestellungen;
	
	@Transient
	@XmlElementWrapper(name = "bestellungen", required = true)
	@XmlElement(name = "bestellung", required = true)
	private List<URI> bestellungenUris;

	@Column(nullable = false)
	@Temporal(DATE)
	@XmlTransient
	private Date erzeugt;

	@Column(nullable = false)
	@Temporal(DATE)
	@XmlTransient
	private Date aktualisiert;
	
	public Lieferung(String liefernr) {
		super();
		this.liefernr = liefernr;
	}
	public Lieferung() {
		super();
	}

	@PrePersist
	protected void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}
	
	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getLiefernr() {
		return liefernr;
	}
	
	public void setLiefernr(String liefernr) {
		this.liefernr = liefernr;
	}
	
	public String getTransportUnternehmen() {
		return transportUnternehmen;
	}
	
	public void setTransportUnternehmen(String transportUnternehmen) {
		this.transportUnternehmen = transportUnternehmen;
	}
	
	public List<Bestellung> getBestellungen() {
		return bestellungen == null ? null : Collections.unmodifiableList(bestellungen);
	}
	
	public void setBestellungen(List<Bestellung> bestellungen) {
		if (this.bestellungen == null) {
			this.bestellungen = bestellungen;
			return;
		}
		
		this.bestellungen.clear();
		if (bestellungen != null) {
			this.bestellungen.addAll(bestellungen);
		}
	}
	
	public void addBestellung(Bestellung bestellung) {
		if (bestellungen == null) {
			bestellungen = new ArrayList<>();
		}
		bestellungen.add(bestellung);
	}
	
	public List<URI> getBestellungenUris() {
		return bestellungenUris;
	}
	
	public void setBestellungenUris(List<URI> bestellungenUris) {
		this.bestellungenUris = bestellungenUris;
	}
	
	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}
	
	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
	}
	
	public Date getAktualisiert() {
		return aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	@Override
	public String toString() {
		return "Lieferung [id=" + id + ", lieferNr=" + liefernr + ", TransportUnternehmen=" + transportUnternehmen
		       + ", erzeugt=" + erzeugt
		       + ", aktualisiert=" + aktualisiert + ']';
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((liefernr == null) ? 0 : liefernr.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Lieferung other = (Lieferung) obj;
		
		if (liefernr == null) {
			if (other.liefernr != null) {
				return false;
			}
		}
		else if (!liefernr.equals(other.liefernr)) {
			return false;
		}

		return true;
	}
	

}