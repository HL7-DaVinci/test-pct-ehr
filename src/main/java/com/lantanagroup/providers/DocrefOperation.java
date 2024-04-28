package com.lantanagroup.providers;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import org.hl7.fhir.r4.model.*;

public class DocrefOperation {

  @Operation(name = "$docref")
  public Bundle docref(
    @OperationParam(name = "patient", min = 1, max = 1, type = IdType.class) IdType thePatient,
    @OperationParam(name = "start", min = 0, max = 1, type = DateType.class) DateType theStart,
    @OperationParam(name = "end", min = 0, max = 1, type = DateType.class) DateType theEnd,
    @OperationParam(name = "type", min = 0, max = 1, type = CodeableConcept.class) CodeableConcept theType,
    @OperationParam(name = "on-demand", min = 0, max = 1, type = BooleanType.class) BooleanType theOndemand
  ) {
    // TODO: Implement operation $docref
    throw new NotImplementedOperationException("Operation $docref is not implemented");
    
    // Bundle retVal = new Bundle();
    // return retVal;
    
  }

  
}
