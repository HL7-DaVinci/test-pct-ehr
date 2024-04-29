package com.lantanagroup.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.lantanagroup.servers.uscoreserver.UsCoreServerProperties;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.parser.IParser;

import org.hl7.fhir.r4.model.*;

// TODO switch over to a common framework for preloading resources.
// TODO Handle resource loading reference dependencies. Currently can't load a resource with a reference to a resource not yet on the server.

@Interceptor
public class ProcessCustomizer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProcessCustomizer.class);

    @Autowired
    protected UsCoreServerProperties serverProperties;

    protected FhirContext fhirContext;
    protected DaoRegistry theDaoRegistry;
    protected String key;
    private boolean dataLoaded;

    private IParser jparser;

    public ProcessCustomizer(FhirContext fhirContext, DaoRegistry theDaoRegistry, String key) {
        dataLoaded = false;
        this.fhirContext = fhirContext;
        this.theDaoRegistry = theDaoRegistry;
        this.key = key;

        jparser = fhirContext.newJsonParser();
        jparser.setPrettyPrint(true);

    }

    // @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
    public boolean incomingRequestPreProcessed(RequestDetails theRequestDetails) {

        if (!dataLoaded) {

            dataLoaded = true;
            logger.info("First request made to Server");
            System.out.println("Loading initial data");

            try {

                for (String filename : getServerResources("ri_resources", "StructureDefinition-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(StructureDefinition.class).update(
                                jparser.parseResource(StructureDefinition.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the StructureDefinition: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "CodeSystem-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(CodeSystem.class).update(
                                jparser.parseResource(CodeSystem.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the CodeSystem: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "ValueSet-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(ValueSet.class).update(
                                jparser.parseResource(ValueSet.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the ValueSet: " + e.getMessage());
                    }
                }

                for (String filename : getServerResources("ri_resources", "OperationDefinition-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(OperationDefinition.class).update(
                                jparser.parseResource(OperationDefinition.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the OperationDefinition: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Organization-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Organization.class).update(
                                jparser.parseResource(Organization.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Organization: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Patient-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Patient.class).update(
                                jparser.parseResource(Patient.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Patient: " + e.getMessage());
                    }
                }

                for (String filename : getServerResources("ri_resources", "Practitioner-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Practitioner.class).update(
                                jparser.parseResource(Practitioner.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Practitioner: " + e.getMessage());
                    }
                }

                for (String filename : getServerResources("ri_resources", "Endpoint-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Endpoint.class).update(
                                jparser.parseResource(Endpoint.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Endpoint: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Contract-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Contract.class).update(
                                jparser.parseResource(Contract.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Contract: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Coverage-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Coverage.class).update(
                                jparser.parseResource(Coverage.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Coverage: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Location-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Location.class).update(
                                jparser.parseResource(Location.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Location: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "PractitionerRole-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(PractitionerRole.class).update(
                                jparser.parseResource(PractitionerRole.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the PractitionerRole: " + e.getMessage());
                    }
                }
                for (String filename : getServerResources("ri_resources", "Claim-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(Claim.class).update(
                                jparser.parseResource(Claim.class, util.loadResource(filename)), theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the Claim: " + e.getMessage());
                    }
                }

                for (String filename : getServerResources("ri_resources", "ExplanationOfBenefit-*.json")) {
                    try {
                        System.out.println("Uploading resource " + filename);
                        theDaoRegistry.getResourceDao(ExplanationOfBenefit.class).update(
                                jparser.parseResource(ExplanationOfBenefit.class, util.loadResource(filename)),
                                theRequestDetails);
                    } catch (Exception e) {
                        System.out.println("Failure to update the ExplanationOfBenefit: " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                logger.info("Error during resource initialization load.");
            }
        }
        return true;
    }

    public List<String> getServerResources(String path, String pattern) {
        List<String> files = new ArrayList<>();

        String localPath = path;
        if (!localPath.substring(localPath.length() - 1, localPath.length() - 1).equals("/")) {
            localPath = localPath + "/";
        }

        try {

            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);

            org.springframework.core.io.Resource[] resources = resolver
                    .getResources("classpath*:" + localPath + pattern);

            for (org.springframework.core.io.Resource resource : resources) {
                files.add(localPath + resource.getFilename());
                logger.info(localPath + resource.getFilename());
            }
        } catch (Exception e) {
            logger.info("Error retrieving file names from " + localPath + pattern);
        }

        return files;
    }

}
