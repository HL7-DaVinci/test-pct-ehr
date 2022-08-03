package ca.uhn.fhir.jpa.starter.interceptors;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;

import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryResourceMatcher;

import ca.uhn.fhir.rest.annotation.Search;

import ca.uhn.fhir.jpa.searchparam.MatchUrlService;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryMatchResult;
import ca.uhn.fhir.jpa.searchparam.matcher.IndexedSearchParamExtractor;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParams;
import ca.uhn.fhir.rest.api.MethodOutcome;

import ca.uhn.fhir.context.FhirContext;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Date;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.*;
import java.io.*;
import ca.uhn.fhir.jpa.provider.*;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.rest.client.api.*;
import ca.uhn.fhir.parser.*;

import ca.uhn.fhir.jpa.starter.utils.RequestHandler;
import ca.uhn.fhir.jpa.starter.utils.FileLoader;

/**
 * Class for intercepting and handling the subsciptions
 */
@Interceptor
public class DataInterceptor {
   private final Logger myLogger = LoggerFactory.getLogger(DataInterceptor.class.getName());

   private String baseUrl = "http://localhost:8080";//"https://davinci-pct-ehr.logicahealth.org";//"http://localhost:8080";//

   private IGenericClient client; //
   private boolean dataLoaded;
   private FhirContext myCtx;

   private IParser jparser;
   private final String[] structureDefinitions = {
       "ri_resources/a_resources/StructureDefinition-compoundDrugLinkingNum.json",
       "ri_resources/a_resources/StructureDefinition-countrySubdivisionCode.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-aeob-bundle.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-aeob.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-coverage.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-gfe-bundle-institutional.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-gfe-bundle-professional.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-gfe-professional.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-location.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-organization.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-patient.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-practitioner.json",
       "ri_resources/a_resources/StructureDefinition-davinci-pct-practitionerrole.json",
       "ri_resources/a_resources/StructureDefinition-disclaimer.json",
       "ri_resources/a_resources/StructureDefinition-endpoint.json",
       "ri_resources/a_resources/StructureDefinition-estimatedDateOfService.json",
       "ri_resources/a_resources/StructureDefinition-expirationDate.json",
       "ri_resources/a_resources/StructureDefinition-gfeBillingProviderLineItemCtrlNum.json",
       "ri_resources/a_resources/StructureDefinition-gfeProviderAssignedIdentifier.json",
       "ri_resources/a_resources/StructureDefinition-gfeReference.json",
       "ri_resources/a_resources/StructureDefinition-gfeSubmitter.json",
       "ri_resources/a_resources/StructureDefinition-interTransIdentifier.json",
       "ri_resources/a_resources/StructureDefinition-outOfNetworkProviderInfo.json",
       "ri_resources/a_resources/StructureDefinition-pct-gfe-Institutional.json",
       "ri_resources/a_resources/StructureDefinition-plannedPeriodOfService.json",
       "ri_resources/a_resources/StructureDefinition-providerEventMethodology.json",
       "ri_resources/a_resources/StructureDefinition-providerRole.json",
       "ri_resources/a_resources/StructureDefinition-referralNumber.json",
       "ri_resources/a_resources/StructureDefinition-subjectToMedicalMgmt.json"
   };
   private final String[] valueSets = {
       "ri_resources/a_resources/ValueSet-PCTCareTeamRoleVS.json",
       "ri_resources/a_resources/ValueSet-PCTDiagnosisTypeVS.json",
       "ri_resources/a_resources/ValueSet-PCTDiagnosticCodes.json",
       "ri_resources/a_resources/ValueSet-PCTGFEItemAdjudicationVS.json",
       "ri_resources/a_resources/ValueSet-PCTGFEItemCptHcpcsVS.json",
       "ri_resources/a_resources/ValueSet-PCTGFEItemNDCVS.json",
       "ri_resources/a_resources/ValueSet-PCTGFEItemRevenueVS.json",
       "ri_resources/a_resources/ValueSet-PCTGFETypeOfBillVS.json",
       "ri_resources/a_resources/ValueSet-PCTOrgContactPurposeTypeVS.json",
       "ri_resources/a_resources/ValueSet-PCTOrgIdentifierTypeVS.json",
       "ri_resources/a_resources/ValueSet-PCTOrganizationTypeVS.json",
       "ri_resources/a_resources/ValueSet-PCTProcedureSurgicalCodes.json",
       "ri_resources/a_resources/ValueSet-PCTProcedureTypeVS.json",
       "ri_resources/a_resources/ValueSet-PCTSubjectToMedicalMgmtReasonVS.json",
       "ri_resources/a_resources/ValueSet-PCTSupportingInfoTypeVS.json",
       "ri_resources/a_resources/ValueSet-pct-coverage-copay-codes.json"
   };
   private final String[] codeSystems = {
      "ri_resources/a_resources/CodeSystem-PCTAdjudicationCategoryType.json",
      "ri_resources/a_resources/CodeSystem-PCTCareTeamRole.json",
      "ri_resources/a_resources/CodeSystem-PCTCoverageCopayTypeCS.json",
      "ri_resources/a_resources/CodeSystem-PCTDiagnosisType.json",
      "ri_resources/a_resources/CodeSystem-PCTGFEItemCptHcpcsCS.json",
      "ri_resources/a_resources/CodeSystem-PCTGFEItemRevenueCS.json",
      "ri_resources/a_resources/CodeSystem-PCTGFETypeOfBillCS.json",
      "ri_resources/a_resources/CodeSystem-PCTOrgContactPurposeType.json",
      "ri_resources/a_resources/CodeSystem-PCTOrgIdentifierTypeCS.json",
      "ri_resources/a_resources/CodeSystem-PCTOrganizationTypeCS.json",
      "ri_resources/a_resources/CodeSystem-PCTProcedureSurgicalCS.json",
      "ri_resources/a_resources/CodeSystem-PCTProcedureType.json",
      "ri_resources/a_resources/CodeSystem-PCTSubjectToMedicalMgmtReasonCS.json",
      "ri_resources/a_resources/CodeSystem-PCTSupportingInfoType.json"
  };
  private final String[] operationDefinitions = {
      "ri_resources/a_resources/OperationDefinition-GFE-submit.json"
  };
  private final String[] organizations = {
      "ri_resources/a_resources/Organization-Submitter-Org-1.json",
      "ri_resources/a_resources/Organization-org1001.json",
      "ri_resources/a_resources/Organization-org1002.json",
      "ri_resources/a_resources/Organization-org2723.json",
      "ri_resources/a_resources/Organization-org2724.json"
  };
  private final String[] endpoints = {
    "ri_resources/a_resources/Endpoint-endpoint001.json"
  };
  private final String[] patients = {
      "ri_resources/a_resources/Patient-patient1001.json",
      "ri_resources/a_resources/Patient-patient2930.json"
  };
  private final String[] practitioners = {
      "ri_resources/a_resources/Practitioner-Submitter-Practitioner-1.json",
      "ri_resources/a_resources/Practitioner-prac001.json",
      "ri_resources/a_resources/Practitioner-prac002.json"
  };
  private final String[] contracts = {
      "ri_resources/b_references/Contract-contract1001.json"
  };
  private final String[] coverages = {
      "ri_resources/b_references/Coverage-coverage1001.json",
      "ri_resources/b_references/Coverage-Coverage2931.json"
  };
  private final String[] locations = {
      "ri_resources/b_references/Location-Provider-Org-Loc-2.json"
  };
  private final String[] claims = {
      "ri_resources/d_references/Claim-PCT-GFE-Institutional-1.json",
      "ri_resources/d_references/Claim-PCT-GFE-Professional-1.json",
      "ri_resources/d_references/Claim-PCT-GFE-Institutional-MRI.json",
  };
  private final String[] explanationOfBenefits = {
      "ri_resources/d_references/ExplanationOfBenefit-PCT-AEOB-1.json"
  };
  private final String[] practitionerRoles = {
      "ri_resources/c_references/PractitionerRole-pracRole002.json"
  };
  private final String[] bundles = {
      "ri_resources/gfe/Bundle-GFE-Bundle-Inst.json",
      "ri_resources/gfe/Bundle-GFE-Bundle-Prof.json"
  };

   /**
    * Constructor using a specific logger
    */
   public DataInterceptor(FhirContext ctx) {
       dataLoaded = false;
       myCtx = ctx;
       client = myCtx.newRestfulGenericClient(baseUrl + "/fhir");
       jparser = myCtx.newJsonParser();
       jparser.setPrettyPrint(true);
   }
   /**
    *
    * Set the base url
    * @param url the url
    */
   public void setBaseUrl(String url) {
      baseUrl = url;
   }
   /**
    * Override the incomingRequestPreProcessed method, which is called
    * for each incoming request before any processing is done
    * @param  theRequest  the request to the server
    * @param  theResponse the response from the server
    * @return             whether to continue processing
    */
   @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
   public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
      String[] parts = theRequest.getRequestURI().toString().split("/");
      // Here is where the Claim should be evaluated
      if (!dataLoaded) {
         dataLoaded = true;
         System.out.println("First request made to Server");
         System.out.println("Loading all data");
         for (String filename: structureDefinitions) {
            loadDataStructureDefinition(filename);
         }
         for (String filename: valueSets) {
            loadDataValueSet(filename);
         }
         for (String filename: codeSystems) {
            loadDataCodeSystem(filename);
         }
         for (String filename: operationDefinitions) {
            loadDataOperationDefinition(filename);
         }
         for (String filename: organizations) {
            loadDataOrganization(filename);
         }
         for (String filename: patients) {
            loadDataPatient(filename);
         }
         for (String filename: endpoints) {
            loadDataEndpoint(filename);
         }
         for (String filename: practitioners) {
            loadDataPractitioner(filename);
         }
         for (String filename: contracts) {
            loadDataContract(filename);
         }
         for (String filename: coverages) {
            loadDataCoverage(filename);
         }
         for (String filename: locations) {
            loadDataLocation(filename);
         }
         for (String filename: practitionerRoles) {
            loadDataPractitionerRole(filename);
         }
         for (String filename: claims) {
            loadDataClaim(filename);
         }
         // for (String filename: explanationOfBenefits) {
         //    loadDataExplanationOfBenefit(filename);
         // }
         // for (String filename: bundles) {
         //    loadDataBundle(filename);
         // }
      }
      return true;
  }
  public void loadDataStructureDefinition(String resource) {
      String p = FileLoader.loadResource(resource);
      StructureDefinition r = jparser.parseResource(StructureDefinition.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the StructureDefinition");
      }
  }
  public void loadDataValueSet(String resource) {
      String p = FileLoader.loadResource(resource);
      ValueSet r = jparser.parseResource(ValueSet.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the ValueSet");
      }
  }
  public void loadDataCodeSystem(String resource) {
      String p = FileLoader.loadResource(resource);
      CodeSystem r = jparser.parseResource(CodeSystem.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the CodeSystem");
      }
  }
  public void loadDataOperationDefinition(String resource) {
      String p = FileLoader.loadResource(resource);
      OperationDefinition r = jparser.parseResource(OperationDefinition.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the OperationDefinition");
      }
  }
  public void loadDataOrganization(String resource) {
      String p = FileLoader.loadResource(resource);
      Organization r = jparser.parseResource(Organization.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Organization");
      }
  }
  public void loadDataPatient(String resource) {
      String p = FileLoader.loadResource(resource);
      Patient r = jparser.parseResource(Patient.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Patient");
      }
  }
  public void loadDataPractitioner(String resource) {
      String p = FileLoader.loadResource(resource);
      Practitioner r = jparser.parseResource(Practitioner.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Practitioner");
      }
  }
  public void loadDataContract(String resource) {
      String p = FileLoader.loadResource(resource);
      Contract r = jparser.parseResource(Contract.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Contract");
      }
  }
  public void loadDataCoverage(String resource) {
      String p = FileLoader.loadResource(resource);
      Coverage r = jparser.parseResource(Coverage.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Coverage");
      }
  }
  public void loadDataLocation(String resource) {
      String p = FileLoader.loadResource(resource);
      Location r = jparser.parseResource(Location.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Location");
      }
  }
  public void loadDataClaim(String resource) {
      String p = FileLoader.loadResource(resource);
      Claim r = jparser.parseResource(Claim.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Claim");
      }
  }
  public void loadDataExplanationOfBenefit(String resource) {
      String p = FileLoader.loadResource(resource);
      ExplanationOfBenefit r = jparser.parseResource(ExplanationOfBenefit.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the ExplanationOfBenefit");
      }
  }
  public void loadDataPractitionerRole(String resource) {
      String p = FileLoader.loadResource(resource);
      PractitionerRole r = jparser.parseResource(PractitionerRole.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the PractitionerRole");
      }
  }
  public void loadDataBundle(String resource) {
      String p = FileLoader.loadResource(resource);
      Bundle r = jparser.parseResource(Bundle.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Bundle");
      }
  }
  public void loadDataEndpoint(String resource) {
      String p = FileLoader.loadResource(resource);
      Endpoint r = jparser.parseResource(Endpoint.class, p);
      try {
          System.out.println("Uploading resource " + resource);
          MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
      } catch(Exception e) {
          System.out.println("Failure to update the Bundle");
      }
  }


}
