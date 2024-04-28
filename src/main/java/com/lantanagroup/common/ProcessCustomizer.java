package com.lantanagroup.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.lantanagroup.servers.uscoreserver.UsCoreServerProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import org.hl7.fhir.r4.model.*;

// TODO, no need to use client for updating. Consider moving to Dao transactions.
// Also switch over to a common framework for preloading resources.

@Interceptor
public class ProcessCustomizer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProcessCustomizer.class);

    @Autowired
    protected UsCoreServerProperties serverProperties;

    protected FhirContext fhirContext;
    protected DaoRegistry theDaoRegistry;
    protected String key;
    private IGenericClient client;
    private boolean dataLoaded;

    private IParser jparser;
    /*
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
            "ri_resources/b_references/Coverage-coverage2931.json"
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
    */

    public ProcessCustomizer(FhirContext fhirContext, DaoRegistry theDaoRegistry, String key) {
        dataLoaded = false;
        this.fhirContext = fhirContext;
        this.theDaoRegistry = theDaoRegistry;
        this.key = key;
        client = null;

        jparser = fhirContext.newJsonParser();
        jparser.setPrettyPrint(true);

    }

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    public boolean incomingRequestPreProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
            HttpServletResponse theResponse) {
        // String[] parts = theRequest.getRequestURI().toString().split("/");
        // Here is where the Claim should be evaluated
        if (!dataLoaded) {

            // client =
            // fhirContext.newRestfulGenericClient(theRequestDetails.getFhirServerBase() +
            // "/fhir");
            // String test = theRequest.getScheme() + "://" + theRequest.getServerName() +
            // ":" + theRequest.getServerPort() + theRequest.getContextPath();
            client = fhirContext.newRestfulGenericClient(theRequest.getScheme() + "://" + theRequest.getServerName() + ":" + theRequest.getServerPort() + "/fhir");
            dataLoaded = true;
            System.out.println("First request made to Server: " + theRequest.getScheme() + "://" + theRequest.getServerName() + ":" + theRequest.getServerPort() + "/fhir");
            System.out.println("Loading all data");

            try{
                
                for (String filename : getServerResources("ri_resources", "StructureDefinition-*.json")) {
                    loadDataStructureDefinition(filename);
                }
                for (String filename : getServerResources("ri_resources", "ValueSet-*.json")) {
                    loadDataValueSet(filename);
                }
                for (String filename : getServerResources("ri_resources", "CodeSystem-*.json")) {
                    loadDataCodeSystem(filename);
                }
                for (String filename : getServerResources("ri_resources", "OperationDefinition-*.json")) {
                    loadDataOperationDefinition(filename);
                }
                for (String filename : getServerResources("ri_resources", "Organization-*.json")) {
                    loadDataOrganization(filename);
                }
                for (String filename : getServerResources("ri_resources", "Patient-*.json")) {
                    loadDataPatient(filename);
                }
                
                for (String filename : getServerResources("ri_resources", "Practitioner-*.json")) {
                    loadDataPractitioner(filename);
                }
                for (String filename : getServerResources("ri_resources", "PractitionerRole-*.json")) {
                    loadDataPractitionerRole(filename);
                }

                for (String filename : getServerResources("ri_resources", "Endpoint-*.json")) {
                    loadDataEndpoint(filename);
                }
                for (String filename : getServerResources("ri_resources", "Contract-*.json")) {
                    loadDataContract(filename);
                }
                for (String filename : getServerResources("ri_resources", "Coverage-*.json")) {
                    loadDataCoverage(filename);
                }
                for (String filename : getServerResources("ri_resources", "Location-*.json")) {
                    loadDataLocation(filename);
                }
                for (String filename : getServerResources("ri_resources", "Claim-*.json")) {
                    loadDataClaim(filename);
                }

                for (String filename : getServerResources("ri_resources", "ExplanationOfBenefit-*.json")) {
                    loadDataExplanationOfBenefit(filename);
                }

            }
            catch(Exception e)
            {
                logger.info("Error during resource initialization load.");
            }
        }
        return true;
    }


    public void loadDataStructureDefinition(String resource) {
        String p = util.loadResource(resource);
        StructureDefinition r = jparser.parseResource(StructureDefinition.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            
            System.out.println("Failure to update the StructureDefinition: " + e.getMessage());
        }
    }

    public void loadDataValueSet(String resource) {
        String p = util.loadResource(resource);
        ValueSet r = jparser.parseResource(ValueSet.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the ValueSet");
        }
    }

    public void loadDataCodeSystem(String resource) {
        String p = util.loadResource(resource);
        CodeSystem r = jparser.parseResource(CodeSystem.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the CodeSystem");
        }
    }

    public void loadDataOperationDefinition(String resource) {
        String p = util.loadResource(resource);
        OperationDefinition r = jparser.parseResource(OperationDefinition.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the OperationDefinition");
        }
    }

    public void loadDataOrganization(String resource) {
        String p = util.loadResource(resource);
        Organization r = jparser.parseResource(Organization.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Organization");
        }
    }

    public void loadDataPatient(String resource) {
        String p = util.loadResource(resource);
        Patient r = jparser.parseResource(Patient.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Patient");
        }
    }

    public void loadDataPractitioner(String resource) {
        String p = util.loadResource(resource);
        Practitioner r = jparser.parseResource(Practitioner.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Practitioner");
        }
    }

    public void loadDataContract(String resource) {
        String p = util.loadResource(resource);
        Contract r = jparser.parseResource(Contract.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Contract");
        }
    }

    public void loadDataCoverage(String resource) {
        String p = util.loadResource(resource);
        Coverage r = jparser.parseResource(Coverage.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Coverage");
        }
    }

    public void loadDataLocation(String resource) {
        String p = util.loadResource(resource);
        Location r = jparser.parseResource(Location.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Location");
        }
    }

    public void loadDataClaim(String resource) {
        String p = util.loadResource(resource);
        Claim r = jparser.parseResource(Claim.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Claim");
        }
    }

    public void loadDataExplanationOfBenefit(String resource) {
        String p = util.loadResource(resource);
        ExplanationOfBenefit r = jparser.parseResource(ExplanationOfBenefit.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the ExplanationOfBenefit");
        }
    }

    public void loadDataPractitionerRole(String resource) {
        String p = util.loadResource(resource);
        PractitionerRole r = jparser.parseResource(PractitionerRole.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the PractitionerRole");
        }
    }

    public void loadDataBundle(String resource) {
        String p = util.loadResource(resource);
        Bundle r = jparser.parseResource(Bundle.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Bundle");
        }
    }

    public void loadDataEndpoint(String resource) {
        String p = util.loadResource(resource);
        Endpoint r = jparser.parseResource(Endpoint.class, p);
        try {
            System.out.println("Uploading resource " + resource);
            MethodOutcome outcome = client.update().resource(r).prettyPrint().encodedJson().execute();
        } catch (Exception e) {
            System.out.println("Failure to update the Bundle");
        }
    }



    public List<String> getServerResources(String path, String pattern)
    {
        List<String> files  = new ArrayList<>();

        String localPath = path;
        if(!localPath.substring(localPath.length() - 1, localPath.length() - 1).equals("/"))
        {
            localPath = localPath + "/";
        }

        try{
            
            ClassLoader cl = this.getClass().getClassLoader(); 
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            //ces("classpath*:/*.json") ;
//org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:/" + localPath + pattern);
            //org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:ri_resources/*.json");
            org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:" + localPath + pattern);

            //org.springframework.core.io.Resource[] resources2 = resolver.getResources("classpath*:/*.json");
            org.springframework.core.io.Resource[] resources2 = resolver.getResources("classpath*/ri_resources/a_resources/*.json");
            


            
            for (org.springframework.core.io.Resource resource: resources){
                files.add(localPath + resource.getFilename());
                logger.info(localPath + resource.getFilename());
            }
        }catch(Exception e)
        {
            logger.info("Error retrieving file names from " + localPath + pattern);
        }

        return files;
    }

}
