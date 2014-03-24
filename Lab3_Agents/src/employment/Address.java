package employment;

import jade.content.Concept;

public class Address implements Concept {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String 	_street;					// Street name
	private Long    _number;          // Street number
	private String 	_city;						// City
	
	// Methods required to use this class to represent the ADDRESS role
	public void setStreet(String street) {
		_street=street;
	}
	public String getStreet() {
		return _street;
	}
	public void setNumber(Long number) {
		_number=number;
	}
	public Long getNumber() {
		return _number;
	}
	public void setCity(String city) {
		_city=city;
	}
	public String getCity() {
		return _city;
	}
	// Other application specific methods
	public boolean equals(Address a){
		if (!_street.equalsIgnoreCase(a.getStreet()))
			return false;
		if (_number.longValue() != a.getNumber().longValue())
			return false;
		if (!_city.equalsIgnoreCase(a.getCity()))
			return false;
		return true;
	}
}
