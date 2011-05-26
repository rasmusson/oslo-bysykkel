package no.sr;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.android.maps.GeoPoint;

public class XMLStationHandler extends DefaultHandler {

	private Station station;
	private Double longitute;
	private Double latitude;
	
	private Boolean descriptionTag;
	private Boolean longituteTag;
	private Boolean latitudeTag;
	
	public Station getData() {
		return station;
	}
	
	@Override
	public void startDocument() throws SAXException {
		station = new Station();
	}
	
	
	@Override
	public void endDocument() throws SAXException {
		station.setLocation(new GeoPoint(new Double(latitude * 1000000).intValue(), new Double(longitute * 1000000).intValue()));
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (localName.equals("description")) {
			descriptionTag = true;
		} else if (localName.equals("longitute")) {
			longituteTag = true;
		} else if (localName.equals("latitude")) {
			latitudeTag = true;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String text = new String(ch, start, length);
		if (descriptionTag) {
			station.setDescription(text);
		} else if (longituteTag) {
			longitute = Double.parseDouble(text);
		} else if (latitudeTag) {
			latitude = Double.parseDouble(text);;
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("description")) {
			descriptionTag = false;
		} else if (localName.equals("longitute")) {
			longituteTag = false;
		} else if (localName.equals("latitude")) {
			latitudeTag = false;
		}
	}
}
