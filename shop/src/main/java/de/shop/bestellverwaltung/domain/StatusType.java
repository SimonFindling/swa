package de.shop.bestellverwaltung.domain;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum StatusType {
	IN_BEARBEITUNG,
	VERSANDT,
	STORNIERT;
}
