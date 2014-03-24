package employment;

import jade.content.onto.*;
import jade.content.schema.*;
public class EmploymentOntology extends Ontology {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public static final String NAME = "employment-ontology";

  // VOCABULARY
  // Concepts
  public static final String ADDRESS = "ADDRESS";
  public static final String ADDRESS_NAME = "street";
  public static final String ADDRESS_NUMBER = "number";
  public static final String ADDRESS_CITY = "city";
  
  public static final String PERSON = "PERSON";
  public static final String PERSON_NAME = "name";
  public static final String PERSON_AGE = "age";
  public static final String PERSON_EXP = "experience";
  public static final String PERSON_ADDRESS = "address";
  
  public static final String COMPANY = "COMPANY";
  public static final String COMPANY_NAME = "name";
  public static final String COMPANY_ADDRESS = "address";
  // Actions
  public static final String ENGAGE = "ENGAGE";
  public static final String ENGAGE_PERSON = "person";
  public static final String ENGAGE_COMPANY = "company";
  // Predicates
  public static final String WORKS_FOR = "WORKS-FOR";
  public static final String WORKS_FOR_PERSON = "person";
  public static final String WORKS_FOR_COMPANY = "company";
  public static final String ENGAGEMENT_ERROR = "ENGAGEMENT-ERROR";
  public static final String PERSON_TOO_OLD = "PERSON-TOO-OLD";
  public static final String PERSON_TOO_YOUNG = "PERSON-EXP-TOO-YOUNG";
  
  private static Ontology theInstance = new EmploymentOntology();
   public static Ontology getInstance() {
		return theInstance;
   }
  private EmploymentOntology() {
    //__CLDC_UNSUPPORTED__BEGIN
  	super(NAME, BasicOntology.getInstance());


    try {
		add(new ConceptSchema(ADDRESS), Address.class);
		add(new ConceptSchema(PERSON), Person.class);
		add(new ConceptSchema(COMPANY), Company.class);
		add(new PredicateSchema(WORKS_FOR), WorksFor.class);
		add(new PredicateSchema(PERSON_TOO_OLD), PersonTooOld.class);
		add(new PredicateSchema(PERSON_TOO_YOUNG), PersonExpTooYoung.class);
		add(new PredicateSchema(ENGAGEMENT_ERROR), EngagementError.class);
		add(new AgentActionSchema(ENGAGE), Engage.class);
		
    	ConceptSchema cs = (ConceptSchema)getSchema(ADDRESS);
		cs.add(ADDRESS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
		cs.add(ADDRESS_NUMBER, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
		cs.add(ADDRESS_CITY, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema)getSchema(PERSON);
    	cs.add(PERSON_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
    	cs.add(PERSON_AGE, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    	cs.add(PERSON_EXP, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    	cs.add(PERSON_ADDRESS, (ConceptSchema)getSchema(ADDRESS), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema)getSchema(COMPANY);
    	cs.add(COMPANY_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
    	cs.add(COMPANY_ADDRESS, (ConceptSchema)getSchema(ADDRESS), ObjectSchema.OPTIONAL);
    	
    	PredicateSchema ps = (PredicateSchema)getSchema(WORKS_FOR);
    	ps.add(WORKS_FOR_PERSON, (ConceptSchema)getSchema(PERSON));
    	ps.add(WORKS_FOR_COMPANY, (ConceptSchema)getSchema(COMPANY));
    	
		AgentActionSchema as = (AgentActionSchema)getSchema(ENGAGE);
		as.add(ENGAGE_PERSON, (ConceptSchema)getSchema(PERSON));
		as.add(ENGAGE_COMPANY, (ConceptSchema)getSchema(COMPANY)); 	
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } 
}
