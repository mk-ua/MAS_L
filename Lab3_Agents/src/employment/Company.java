package employment;
import jade.content.Concept;

public class Company implements Concept {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String 	_name;						//Company's name
	private Address	_address;					//Headquarter's address
	
	// Methods required to use this class to represent the COMPANY role
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}
	public void setAddress(Address address) {
		_address=address;
	}
	public Address getAddress() {
		return _address;
	}
	
	// Other application specific methods
	public boolean equals(Company c){
		return (_name.equalsIgnoreCase(c.getName()));
	}
}
