package employment;

import jade.content.Predicate;

public class Person implements Predicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String 	_name;						//Person's name
	private Long    _age;						//Person's age
	private Long    _experience;				//Person's experiences
	private Address _address;					//Address' age
	
	// Methods required to use this class to represent the PERSON role
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}
	public void setAge(Long age) {
		_age=age;
	}
	public Long getAge() {
		return _age;
	}
	public void setExperience(Long experience) {
		_experience=experience;
	}
	public Long getExperience() {
		return _experience;
	}
	public void setAddress(Address address) {
		_address=address;
	}
	public Address getAddress() {
		return _address;
	}
	// Other application specific methods
	public boolean equals(Person p){
		if (!_name.equalsIgnoreCase(p.getName()))
			return false;
		if (_age != null && p.getAge() != null) // Age is an optional field
			if (_age.longValue() != p.getAge().longValue())
				return false;
		if (_experience != null && p.getExperience() != null) // Age is an optional field
			if (_experience.longValue() != p.getExperience().longValue())
				return false;
		if (_address != null && p.getAddress() != null) // Address is an optional field
			if (!_address.equals(p.getAddress()))
				return false;
		return true;
	}
}
