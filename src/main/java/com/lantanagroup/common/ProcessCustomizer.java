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
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import org.apache.poi.ss.formula.functions.T;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.ResourceTypes;

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

    //@Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
    public boolean incomingRequestPreProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
            HttpServletResponse theResponse) {
        
        if (!dataLoaded) {
            
            dataLoaded = true;
            System.out.println("Loading initial data");

            try{
                
                for (String filename : getServerResources("ri_resources", "StructureDefinition-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(StructureDefinition.class).update(jparser.parseResource(StructureDefinition.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the StructureDefinition: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "CodeSystem-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(CodeSystem.class).update(jparser.parseResource(CodeSystem.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the CodeSystem: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "ValueSet-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(ValueSet.class).update(jparser.parseResource(ValueSet.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the ValueSet: " + e.getMessage());
                    }
                }
                
                for (String filename : getServerResources("ri_resources", "OperationDefinition-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(OperationDefinition.class).update(jparser.parseResource(OperationDefinition.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the OperationDefinition: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Organization-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Organization.class).update(jparser.parseResource(Organization.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Organization: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Patient-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Patient.class).update(jparser.parseResource(Patient.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Patient: " + e.getMessage());
                    }
                }
                
                for (String filename : getServerResources("ri_resources", "Practitioner-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Practitioner.class).update(jparser.parseResource(Practitioner.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Practitioner: " + e.getMessage());
                    }
                }

                for (String filename : getServerResources("ri_resources", "Endpoint-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Endpoint.class).update(jparser.parseResource(Endpoint.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Endpoint: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Contract-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Contract.class).update(jparser.parseResource(Contract.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Contract: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Coverage-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Coverage.class).update(jparser.parseResource(Coverage.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Coverage: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Location-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Location.class).update(jparser.parseResource(Location.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Location: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "PractitionerRole-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(PractitionerRole.class).update(jparser.parseResource(PractitionerRole.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the PractitionerRole: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Claim-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Claim.class).update(jparser.parseResource(Claim.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Claim: " + e.getMessage());
                    }
                }

                for (String filename : getServerResources("ri_resources", "ExplanationOfBenefit-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(ExplanationOfBenefit.class).update(jparser.parseResource(ExplanationOfBenefit.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the ExplanationOfBenefit: " + e.getMessage());
                    }
                }
                

            }
            catch(Exception e)
            {
                logger.info("Error during resource initialization load.");
            }
        }
        return true;
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

            org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:" + localPath + pattern);

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
