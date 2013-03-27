package de.shop.kundenverwaltung.domain;

import static javax.persistence.TemporalType.DATE;
import static de.shop.util.Constants.KEINE_ID;
import static de.shop.util.Constants.MIN_ID;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;

import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.ScriptAssert;



import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.IdGroup;


@Entity
@Table(name = "kunde")
@NamedQueries ({
	@NamedQuery(name  = Kunde.FIND_KUNDEN,
				query = "SELECT k"
				+ " FROM Kunde k"),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_FETCH_BESTELLUNGEN,
				query = "SELECT  DISTINCT k"	
				+ " FROM Kunde k LEFT JOIN FETCH k.bestellungen"),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_ORDER_BY_ID,
		        query = "SELECT   k"
				+ " FROM  Kunde k"
		        + " ORDER BY k.id"),
	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_EMAIL,
   				query = "SELECT DISTINCT k"
   				+ " FROM   Kunde k"
   				+ " WHERE  k.email = :" + Kunde.PARAM_KUNDE_EMAIL),
   @NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME,
	            query = "SELECT k"
	            + " FROM   Kunde k"
	            + " WHERE  UPPER(k.nachname) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"),
   	@NamedQuery(name  = Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
	            query = "SELECT DISTINCT k"
			    + " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
			    + " WHERE  k.id = :" + Kunde.PARAM_KUNDE_ID),
	@NamedQuery(name  = Kunde.FIND_KUNDEN_BY_PLZ,
	            query = "SELECT k"
				+ " FROM  Kunde k"
			    + " WHERE k.adresse.plz = :" + Kunde.PARAM_KUNDE_ADRESSE_PLZ),
    @NamedQuery(name  = Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
				query = "SELECT DISTINCT k"
			    + " FROM   Kunde k LEFT JOIN FETCH k.bestellungen"
			    + " WHERE  UPPER(k.nachname) = UPPER(:" + Kunde.PARAM_KUNDE_NACHNAME + ")"),
	@NamedQuery(name = Kunde.FIND_KUNDE_BY_BESTELLUNG, 
				query = "SELECT k"
				+ " FROM Kunde k JOIN FETCH k.bestellungen b"
				+ " WHERE b.id = :" + Kunde.PARAM_KUNDE_BESTELLUNG_ID),
})
@ScriptAssert(lang = "javascript",
				script = "(_this.password == null && _this.passwordWdh == null)"
						+ "|| (_this.password != null && _this.password.equals(_this.passwordWdh))",
						message = "{kundenverwaltung.kunde.password.notEqual}",
				groups = PasswordGroup.class)
@XmlRootElement
public class Kunde implements java.io.Serializable {
	private static final long serialVersionUID = 8926240073895833886L;
	
	//UTF-8 Pattern fuer Umlaute
	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
	public static final String NACHNAME_PATTERN = NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
	public static final int NACHNAME_LENGTH_MIN = 2;
	public static final int NACHNAME_LENGTH_MAX = 32;
	public static final int VORNAME_LENGTH_MAX = 32;
	public static final int EMAIL_LENGTH_MAX = 128;
	public static final int PASSWORD_LENGTH_MAX = 256;
	public static final double UMSATZ_DEFAULT = 0.00;
	public static final float RABATT_DEFAULT = (float) 0.00;
	
	private static final String PREFIX = "Kunde.";
	public static final String FIND_KUNDEN = PREFIX + "findKunden";
	public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX
													+ "findKundenFetchBestellungen";
	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
	public static final String FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN = PREFIX
													+ "findKundenByIdFetchBestellungen";
	public static final String FIND_KUNDEN_BY_PLZ = PREFIX + "findKundenByPlz";
	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN = PREFIX
													+ "findKundenByNachnameFetchBestellungen";
	public static final String FIND_KUNDE_BY_BESTELLUNG = PREFIX + "findKundeByBestellung";
	public static final String FIND_KUNDEN_ORDER_BY_ID = PREFIX + "findKundenOrderById";
	
	public static final String PARAM_KUNDE_ID = "kundeId";
	public static final String PARAM_KUNDE_EMAIL = "email";
	public static final String PARAM_KUNDE_NACHNAME = "nachname";
	public static final String PARAM_KUNDE_ADRESSE_PLZ = "plz";
	public static final String PARAM_KUNDE_BESTELLUNG_ID = "bestellungId";
	
	@Id
	@GeneratedValue
	@Column(name = "k_id", nullable = false, updatable = false, unique = true)
	@Min(value = MIN_ID, message = "{kundenverwaltung.kunde.id.min}", groups = IdGroup.class)
	@XmlAttribute
	private Long id = KEINE_ID;

	@Column(length = NACHNAME_LENGTH_MAX)
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX, 
	message = "{kundenverwaltung.kunde.nachname.length}")
	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
	@XmlElement(required = true)
	private String nachname;

	@Column(length = VORNAME_LENGTH_MAX)
	@Size(max = VORNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.vorname.length}")
	@XmlElement(required = true)
	private String vorname;

	@Temporal(DATE)
	@Past(message = "{kundenverwaltung.kunde.seit.past}")
	
	//TODO JSON RESTFUL Webservice Annotation
	private Date seit;

	private FamilienstandType familienstand;

	private GeschlechtType geschlecht;

	private boolean newsletter = false;

	@NotNull(message = "{kundenverwaltung.kunde.rabatt.notNull}")
	private float rabatt = RABATT_DEFAULT;

	@Column(length = EMAIL_LENGTH_MAX, nullable = false, unique = true)
	@NotNull(message = "{kundenverwaltung.kunde.email.notNull}")
	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
	private String email;

	@Column(length = PASSWORD_LENGTH_MAX)
	@Size(max = PASSWORD_LENGTH_MAX, message = "{kundenverwaltung.kunde.password.length}")
	private String password;

	@Transient
	private String passwordWdh;
	
	@OneToMany
	@JoinColumn(name = "kunde_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@XmlTransient
	private List<Bestellung> bestellungen;
	
	@Transient
	@XmlElement(name = "bestellungen")
	private URI bestellungenUri;

	@OneToOne(cascade = { PERSIST, REMOVE }, mappedBy = "kunde")
	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
	@Valid
	@XmlElement(required = true)
	private Adresse adresse;

	@Column(nullable = false)
	@Temporal(DATE)
	@XmlTransient
	private Date erzeugt;

	@Column(nullable = false)
	@Temporal(DATE)
	@XmlTransient
	private Date aktualisiert;

	@PrePersist
	private void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	@PostLoad
	private void postLoad() {
		passwordWdh = password;
	}

	public void setValues(Kunde k) {
		nachname = k.nachname;
		vorname = k.vorname;
		familienstand = k.familienstand;
		geschlecht = k.geschlecht;
		rabatt = k.rabatt;
		seit = k.seit;
		newsletter = k.newsletter;
		email = k.email;
		password = k.password;
		passwordWdh = k.password;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	
	public URI getBestellungenUri() {
		return bestellungenUri;
	}
	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}

	
	
	public Date getSeit() {
		return seit == null ? null : (Date) seit.clone();
	}

	public void setSeit(Date seit) {
		this.seit = seit == null ? null : (Date) seit.clone();
	}
	
	public String getSeitAsString(int style, Locale locale) {
		Date temp = seit;
		if (temp == null) {
			temp = new Date();
		}
		final DateFormat f = DateFormat.getDateInstance(style, locale);
		return f.format(temp);
	}
		
	public void setSeit(String seitStr, int style, Locale locale) {
		final DateFormat f = DateFormat.getDateInstance(style, locale);
		try {
			this.seit = f.parse(seitStr);
		}
		catch (ParseException e) {
			throw new RuntimeException("Kein gueltiges Datumsformat fuer: " + seitStr, e);
		}
	}	

	public FamilienstandType getFamilienstand() {
		return familienstand;
	}

	public void setFamilienstand(FamilienstandType familienstand) {
		this.familienstand = familienstand;
	}

	public GeschlechtType getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(GeschlechtType geschlecht) {
		this.geschlecht = geschlecht;
	}

	public boolean isNewsletter() {
		return this.newsletter;
	}

	public void setNewsletter(boolean newsletter) {
		this.newsletter = newsletter;
	}

	public float getRabatt() {
		return rabatt;
	}

	public void setRabatt(float rabatt) {
		this.rabatt = rabatt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordWdh() {
		return passwordWdh;
	}

	public void setPasswordWdh(String passwordWdh) {
		this.passwordWdh = passwordWdh;
	}
	
	public Adresse getAdresse() {
		return adresse;
	}
	
	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}
	
	public List<Bestellung> getBestellungen() {
		if (bestellungen == null) {
			return null;
		}
		
		return Collections.unmodifiableList(bestellungen);
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
	
	public Kunde addBestellung(Bestellung bestellung) {
		if (bestellungen == null) {
			bestellungen = new ArrayList<>();
		}
		bestellungen.add(bestellung);
		return this;
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
		return "Kunde [id=" + id
			   + ", nachname=" + nachname + ", vorname=" + vorname
			   + ", seit=" + getSeitAsString(DateFormat.MEDIUM, Locale.GERMANY)	
			   + ", email=" + email
			   + ", password=" + password + ", passwordWdh=" + passwordWdh
			   + ", familienstand=" + familienstand
			   + ", geschlecht=" + geschlecht 
			   + ", erzeugt=" + erzeugt
			   + ", aktualisiert=" + aktualisiert + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
		final Kunde other = (Kunde) obj;
		
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		}
		else if (!email.equals(other.email)) {
			return false;
		}
		
		return true;
	}
	
}