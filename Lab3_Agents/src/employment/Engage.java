package employment;

import jade.content.Concept;

public class Engage implements Concept {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Company	_company;							//Company engage
	private Person	_person;							//Person engaged
	//These methods are used by the JADE-framework
	public void setPerson(Person person) {
		_person=person;
	}
	public Person getPerson() {
		return _person;
	}

	public void setCompany(Company company) {
		_company=company;
	}
	public Company getCompany() {
		return _company;
	}

}
