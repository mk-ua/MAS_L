package employment;
import jade.content.Predicate;

public class WorksFor implements Predicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Company	_company;							//Company employer
	private Person	_person;							//Person employee
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
