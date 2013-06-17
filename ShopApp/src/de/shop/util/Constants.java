package de.shop.util;

public final class Constants {
	public static final String KUNDE_KEY = "kunde";
	public static final String KUNDEN_KEY = "kunden";
	
	public static final int WISCHEN_MIN_DISTANCE = 30;        // Min. Laenge des Wischens in Pixel
	public static final int WISCHEN_MAX_OFFSET_PATH = 30;     // Max. Abweichung in der Y-Richtung in Pixel
	public static final int WISCHEN_THRESHOLD_VELOCITY = 30;  // Geschwindigkeit: Pixel pro Sekunde
	
	public static final int TAB_KUNDE_STAMMDATEN = 0;
	public static final int TAB_KUNDE_BESTELLUNGEN = 1;
	
	public static final String PROTOCOL_DEFAULT = "http";
	public static final String HOST_DEFAULT = "10.0.2.2";
	public static final String PORT_DEFAULT = "8080";
	public static final String PATH_DEFAULT = "/shop/rest";
	public static final boolean MOCK_DEFAULT = false;
	
	private Constants() {}
}
