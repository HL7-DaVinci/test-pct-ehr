package com.lantanagroup.common;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;

import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;

@Interceptor
public class CapabilityStatementCustomizer {

  private static final String US_CORE_IG_BASE = "http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core";
  private static final String DAVINCI_PCT_IG_BASE = "http://hl7.org/fhir/us/davinci-pct/ImplementationGuide/hl7.fhir.us.davinci-pct";
  private static final String US_CORE_IG_KEY = "hl_7_fhir_us_core";
  private static final String DAVINCI_PCT_IG_KEY = "hl_7_fhir_us_davinci_pct";
  private static final String PCT_CAPABILITY_NAME = "PatientCostTransparencyEHRServerCapabilityStatement";
  private static final String PCT_CAPABILITY_TITLE = "Patient Cost Transparency EHR Reference Implementation Capability Statement";
  private static final String PCT_PUBLISHER = "HL7 Da Vinci PCT";
  private static final String PCT_SOFTWARE_NAME = "test-pct-ehr";
  private static final String PCT_IMPLEMENTATION_DESCRIPTION = "Patient Cost Transparency EHR Reference Implementation Capability Statement, for sharing Good Faith Documents";
  private static final Set<String> PCT_SUPPORTED_RESOURCE_TYPES = Set.of();

  private final String usCoreImplementationGuide;
  private final String davinciPctImplementationGuide;
  private final String davinciPctVersion;

  public CapabilityStatementCustomizer(ServerProperties serverProperties) {
    usCoreImplementationGuide = buildImplementationGuideCanonical(serverProperties, US_CORE_IG_KEY, US_CORE_IG_BASE);
    davinciPctImplementationGuide = buildImplementationGuideCanonical(serverProperties, DAVINCI_PCT_IG_KEY, DAVINCI_PCT_IG_BASE);
    davinciPctVersion = getImplementationGuideVersion(serverProperties, DAVINCI_PCT_IG_KEY);
  }

  @Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
  public IBaseConformance customize(IBaseConformance theCapabilityStatement) {
    if (theCapabilityStatement instanceof CapabilityStatement) {
      CapabilityStatement cs = (CapabilityStatement) theCapabilityStatement;
      applyPctMetadata(cs);
      //trimRestResources(cs);
      applyImplementationGuides(cs);
      return cs;
    }

    return theCapabilityStatement;
  }

  private void applyPctMetadata(CapabilityStatement cs) {
    cs.setName(PCT_CAPABILITY_NAME);
    cs.setTitle(PCT_CAPABILITY_TITLE);
    cs.setPublisher(PCT_PUBLISHER);
    cs.setStatus(PublicationStatus.ACTIVE);
    if (!davinciPctVersion.isBlank()) {
      cs.setVersion(davinciPctVersion);
    }
    if (!cs.hasImplementation()) {
      cs.setImplementation(new CapabilityStatement.CapabilityStatementImplementationComponent());
    }
    cs.getImplementation().setDescription(PCT_IMPLEMENTATION_DESCRIPTION);

    if (!cs.hasSoftware()) {
      cs.setSoftware(new CapabilityStatementSoftwareComponent());
    }
    if (!cs.getSoftware().hasName()) {
      cs.getSoftware().setName(PCT_SOFTWARE_NAME);
    }
  }

  private void applyImplementationGuides(CapabilityStatement cs) {
    if (!usCoreImplementationGuide.isBlank() && !containsCanonical(cs, usCoreImplementationGuide)) {
      cs.addImplementationGuide(usCoreImplementationGuide);
    }
    if (!davinciPctImplementationGuide.isBlank() && !containsCanonical(cs, davinciPctImplementationGuide)) {
      cs.addImplementationGuide(davinciPctImplementationGuide);
    }
  }

  private String buildImplementationGuideCanonical(
      ServerProperties serverProperties,
      String guideKey,
      String canonicalBase) {
    String version = getImplementationGuideVersion(serverProperties, guideKey);

    if (version.isBlank()) {
      return "";
    }

    return canonicalBase + "|" + version;
  }

  /**
   * Retrieves the implementation guide version from YAML configuration properties.
   *
   * @param serverProperties server configuration loaded from application.yaml
   * @param guideKey the implementation guide key (e.g., "hl_7_fhir_us_core", "hl_7_fhir_us_davinci_pct")
   * @return the version string (e.g., "7.0.0", "2.0.1"), or empty string if not configured
   */
  private String getImplementationGuideVersion(
      ServerProperties serverProperties,
      String guideKey) {
    String version = "";
    if (serverProperties != null) {
      Map<String, PackageInstallationSpec> guides = serverProperties.getImplementationGuides();
      if (guides != null) {
        PackageInstallationSpec spec = guides.get(guideKey);
        if (spec != null && spec.getVersion() != null && !spec.getVersion().isBlank()) {
          version = spec.getVersion();
        }
      }
    }

    return version;
  }

  private boolean containsCanonical(CapabilityStatement cs, String canonical) {
    for (CanonicalType c : cs.getImplementationGuide()) {
      if (canonical.equals(c.getValue())) {
        return true;
      }
    }
    return false;
  }
  private void trimRestResources(CapabilityStatement cs) {
    for (CapabilityStatement.CapabilityStatementRestComponent restComponent : cs.getRest()) {
      var filteredResources = new LinkedHashSet<CapabilityStatement.CapabilityStatementRestResourceComponent>();
      for (CapabilityStatement.CapabilityStatementRestResourceComponent resource : restComponent.getResource()) {
        if (resource.hasType() && PCT_SUPPORTED_RESOURCE_TYPES.contains(resource.getType())) {
          limitResourceClaims(resource);
          filteredResources.add(resource);
        }
      }
      restComponent.getResource().clear();
      restComponent.getResource().addAll(filteredResources);
    }
  }

  private void limitResourceClaims(CapabilityStatement.CapabilityStatementRestResourceComponent resource) {
    resource.getOperation().clear();

    List<ResourceInteractionComponent> interactions = resource.getInteraction();
    interactions.removeIf(i ->
            i.getCode() != CapabilityStatement.TypeRestfulInteraction.READ
                    && i.getCode() != CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
  }

}
