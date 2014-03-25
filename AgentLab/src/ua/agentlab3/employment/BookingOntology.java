/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package ua.agentlab3.employment;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

public class BookingOntology extends Ontology {

	private static final long serialVersionUID = -5422083371425226993L;

/**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "booking-ontology";

  // VOCABULARY
  // Concepts
  public static final String ADDRESS = "ADDRESS";
  public static final String ADDRESS_NAME = "street";
  public static final String ADDRESS_NUMBER = "number";
  public static final String ADDRESS_CITY = "city";
  
  public static final String PERSON = "PERSON";
  public static final String PERSON_NAME = "name";
  public static final String PERSON_AGE = "age";
  public static final String PERSON_ADDRESS = "address";
  
  public static final String COMPANY = "COMPANY";
  public static final String COMPANY_NAME = "name";
  public static final String COMPANY_ADDRESS = "address";
  public static final String CONCERT_NAME = "concertName";
  
  // Actions
  public static final String BOOKING = "BOOKING";
  public static final String BUYER = "person";
  public static final String BOOKING_COMPANY = "company";
  // Predicates
  public static final String BOOKED_TICKET_FROM = "BOOKED-TICKET-FROM";
  public static final String BOOKED_TICKET_OWNER = "BOOKED-TICKET-OWNER";
  public static final String SELLING_COMPANY = "SELLING-COMPANY";
  public static final String BOOKING_ERROR = "ENGAGEMENT-ERROR";
  public static final String PERSON_TOO_YOUNG = "PERSON-TOO-YOUNG";
  
  private static Ontology theInstance = new BookingOntology();
	
  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
   public static Ontology getInstance() {
		return theInstance;
   }
	
  /**
   * Constructor
   */
  private BookingOntology() {
    //__CLDC_UNSUPPORTED__BEGIN
  	super(NAME, BasicOntology.getInstance());


    try {
		//Concept schemas
    	add(new ConceptSchema(ADDRESS), Address.class);
		add(new ConceptSchema(PERSON), Person.class);
		add(new ConceptSchema(COMPANY), Company.class);
		
		//Predicate Shemas
		add(new PredicateSchema(BOOKED_TICKET_FROM), BookedTicketFrom.class);
		add(new PredicateSchema(PERSON_TOO_YOUNG), PersonToYoung.class);
		add(new PredicateSchema(BOOKING_ERROR), BookingError.class);
		
		// Agent Action Schema
		add(new AgentActionSchema(BOOKING), Booking.class);
		
    	ConceptSchema cs = (ConceptSchema)getSchema(ADDRESS);
		cs.add(ADDRESS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
		cs.add(ADDRESS_NUMBER, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
		cs.add(ADDRESS_CITY, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema)getSchema(PERSON);
    	cs.add(PERSON_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
    	cs.add(PERSON_AGE, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    	cs.add(PERSON_ADDRESS, (ConceptSchema)getSchema(ADDRESS), ObjectSchema.OPTIONAL);
    	
    	cs = (ConceptSchema)getSchema(COMPANY);
    	cs.add(COMPANY_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
    	cs.add(COMPANY_ADDRESS, (ConceptSchema)getSchema(ADDRESS), ObjectSchema.OPTIONAL);
    	cs.add(CONCERT_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    	
    	
    	PredicateSchema ps = (PredicateSchema)getSchema(BOOKED_TICKET_FROM);
    	ps.add(BOOKED_TICKET_OWNER, (ConceptSchema)getSchema(PERSON));
    	ps.add(SELLING_COMPANY, (ConceptSchema)getSchema(COMPANY));
    	
		AgentActionSchema as = (AgentActionSchema)getSchema(BOOKING);
		as.add(BUYER, (ConceptSchema)getSchema(PERSON));
		as.add(BOOKING_COMPANY, (ConceptSchema)getSchema(COMPANY)); 	
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } 
}

