package de.shop.bestellverwaltung.domain;


import static de.shop.util.Constants.KEINE_ID;
import static de.shop.util.Constants.MIN_ID;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.TemporalType.DATE;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.IdGroup;
import de.shop.util.PreExistingGroup;


@Entity
@Table(name = "bestellung")

@NamedQueries({
   	@NamedQuery(name  = Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_LIEFERUNGEN,
			    query = "SELECT DISTINCT b"
                        + " FROM   Bestellung b LEFT JOIN FETCH b.lieferungen"
   			            + " WHERE  b.id =:" + Bestellung.PARAM_ID),
    @NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_ARTIKEL_ID,
    			query = "SELECT b" 
    					+ " FROM Bestellung b JOIN b.bestellpositionen be"
    					+ " JOIN be.artikel a"
    					+ " WHERE a.id =:" + Bestellung.PARAM_ARTIKEL_ID),
    @NamedQuery(name  = Bestellung.FIND_BESTELLUNGEN_BY_KUNDE,
    	        query = "SELECT b"
    				    + " FROM   Bestellung b"
    		            + " WHERE  b.kunde.id = :" + Bestellung.PARAM_KUNDE_ID),
    @NamedQuery(name  = Bestellung.FIND_KUNDE_BY_ID,
    	 			    query = "SELECT b.kunde"
    	                + " FROM   Bestellung b"
    	  			    + " WHERE  b.id = :" + Bestellung.PARAM_ID)		            
    		            
})
@XmlRootElement
public class Bestellung implements java.io.Serializable {
	private static final long serialVersionUID = -975945312254032788L;

	
	private static final String PREFIX = "Bestellung.";
	public static final String FIND_BESTELLUNG_BY_ID_FETCH_LIEFERUNGEN =
		                       PREFIX + "findBestellungenByIdFetchLieferungen";
	public static final String FIND_BESTELLUNGEN_BY_KUNDE = PREFIX + "findBestellungenByKunde";
	public static final String FIND_KUNDE_BY_ID = PREFIX + "findKundeById";
	public static final String FIND_BESTELLUNGEN_BY_ARTIKEL_ID = 
			                   PREFIX + "findebestellungbyartikel";
	public static final String PARAM_ID = "id";
	public static final String PARAM_ARTIKEL_ID = "aid";
	public static final String PARAM_KUNDE_ID = "kundeId";

	@Id
	@GeneratedValue
	@Column(name = "b_id", unique = true, nullable = false, updatable = false)
	@Min(value = MIN_ID, message = "{bestellverwaltung.bestellung.id.min}", groups = IdGroup.class)
	@XmlAttribute
	private Long  id = KEINE_ID;
	
	@Column(name = "status")
	private StatusType status = StatusType.IN_BEARBEITUNG;
	
	@OneToMany(fetch = EAGER, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "bestellung_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@NotEmpty(message = "{bestellverwaltung.bestellung.bestellpositionen.notEmpty}")
	@Valid
	@XmlElementWrapper(name = "bestellpositionen", required = true)
	@XmlElement(name = "bestellposition", required = true)
	private List<Bestellposition> bestellpositionen;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
	@NotNull(message = "{bestellverwaltung.bestellung.kunde.notNull}", groups = PreExistingGroup.class)
	@XmlTransient
	private Kunde kunde;
	
	@Transient
	@XmlElement(name = "kunde", required = true)
	private URI kundeUri;
	
	@ManyToMany
	@JoinTable(name = "bestellung_lieferung",
			   joinColumns = @JoinColumn(name = "bestellung_fk"),
			                 inverseJoinColumns = @JoinColumn(name = "lieferung_fk"))
	@XmlTransient
	private List<Lieferung> lieferungen;
	
	@Transient
	@XmlElement(name = "lieferungen")
	private URI lieferungenUri;
	
	@Column(nullable = false)
	@Temporal(DATE)
	@XmlTransient
	private Date erzeugt;

	@Column(nullable = false)
	@Temporal(DATE)
	@XmlTransient
	private Date aktualisiert;

	public Bestellung() {
		super();
	}
	
	public Bestellung(List<Bestellposition> bestellpositionen) {
		super();
		this.bestellpositionen = bestellpositionen;
	}

	
	@PrePersist
	private void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}
	
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public List<Bestellposition> getBestellpositionen() {
		return bestellpositionen == null ? null : Collections.unmodifiableList(bestellpositionen);
	}
	
	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		if (this.bestellpositionen == null) {
			this.bestellpositionen = bestellpositionen;
			return;
		}
		
		this.bestellpositionen.clear();
		if (bestellpositionen != null) {
			this.bestellpositionen.addAll(bestellpositionen);
		}
	}
	
	public Bestellung addBestellposition(Bestellposition bestellposition) {
		if (bestellpositionen == null) {
			bestellpositionen = new ArrayList<>();
		}
		bestellpositionen.add(bestellposition);
		return this;
	}

	public Kunde getKunde() {
		return kunde;
	}
	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}
	
	public List<Lieferung> getLieferungen() {
		return lieferungen == null ? null : Collections.unmodifiableList(lieferungen);
	}
	
	public void setLieferungen(List<Lieferung> lieferungen) {
		if (this.lieferungen == null) {
			this.lieferungen = lieferungen;
			return;
		}
		
		this.lieferungen.clear();
		if (lieferungen != null) {
			this.lieferungen.addAll(lieferungen);
		}
	}

	public URI getLieferungenUri() {
		return lieferungenUri;
	}
	
	public void setLieferungenUri(URI lieferungenUri) {
		this.lieferungenUri = lieferungenUri;
	}

	public URI getKundeUri() {
		return kundeUri;
	}
	
	public void setKundeUri(URI kundeUri) {
		this.kundeUri = kundeUri;
	}
	
	public void addLieferung(Lieferung lieferung) {
		if (lieferungen == null) {
			lieferungen = new ArrayList<>();
		}
		lieferungen.add(lieferung);
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
		
		return "Bestellung [id=" + id 
		       + ", erzeugt=" + erzeugt
		       + ", aktualisiert=" + aktualisiert 
		       + "status=" + status + ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((erzeugt == null) ? 0 : erzeugt.hashCode());
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
		final Bestellung other = (Bestellung) obj;
		
		
		
		if (erzeugt == null) {
			if (other.erzeugt != null) {
				return false;
			}
		}
		else if (!erzeugt.equals(other.erzeugt)) {
			return false;
		}
		
		return true;
	}

}