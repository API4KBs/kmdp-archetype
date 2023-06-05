package edu.mayo.kmdp.function.common.handlers.fhir;

import ca.uhn.fhir.context.FhirContext;
import edu.mayo.kmdp.function.common.handlers.MessageHandler;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.omg.spec.api4kp._20200801.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link edu.mayo.kmdp.function.common.handlers.MessageHandler} that can be used
 * in Function Apps that use HAPI FHIR as Input/Output datatypes.
 * <p>
 * Adapts the HAPI FHIR objects in/to the {@link com.microsoft.azure.functions.HttpRequestMessage}
 * and {@link com.microsoft.azure.functions.HttpResponseMessage}.
 * <p>
 * Scope note: Handles both FHIR versions STU3 and R4. Falls back to default JSON de/serialization
 * if the objects to be handled are not recognized as either STU3 or R4 FHIR resources, making this
 * class usable with hybrid APIs (e.g. non-FHIR in, FHIR out functions)
 */
public class FHIRMessageHandler implements MessageHandler {

  protected static final FhirContext stu3 = FhirContext.forDstu3();
  protected static final FhirContext r4 = FhirContext.forR4();
  private static final Logger logger = LoggerFactory.getLogger(FHIRMessageHandler.class);

  public FHIRMessageHandler() {
    // nothing to do
  }

  /**
   * Tries to parse the input JSON string into a HAPI FHIR resource of the given type T. If unable
   * or unsuccessful, falls back to the default JSON parsing
   *
   * @param json the String to be parsed, assumed to be serialized JSON (of serialized FHIR)
   * @param type the Class to parse the JSON string into
   * @param <T>  the Type to parse the JSON string into (e.g.
   *             {@link org.hl7.fhir.dstu3.model.Observation}
   * @return the input, as an instance of T, wrapped in Answer.
   * @see #parseFhir(String, Class)
   */
  @Override
  public <T> Answer<T> parse(String json, Class<T> type) {
    return parseFhir(json, type)
        .or(() -> MessageHandler.super.parse(json, type));
  }

  /**
   * Tries to serialize an object of type T as a JSON string. If T is an instance of (HAPI) FHIR,
   * will use the HAPI facilities. Otherwise, falls back to the default JSON serialization
   *
   * @param result the object to be serialized
   * @return T, in JSON String format, wrapped in Answer.
   * @see #serializeFhir(Object)
   */
  @Override
  public <T> Answer<String> serialize(T result) {
    return serializeFhir(result)
        .or(() -> MessageHandler.super.serialize(result));
  }

  /**
   * Tries to parse the input JSON string into a HAPI FHIR resource of the given type T.
   * <p>
   * Uses the input argument type to determine whether FHIR is involved.
   *
   * @param json the String to be parsed, assumed to be serialized JSON (of serialized FHIR)
   * @param type the Class to parse the JSON string into
   * @param <T>  the Type to parse the JSON string into (e.g.
   *             {@link org.hl7.fhir.dstu3.model.Observation}
   * @return the input, as an instance of T, wrapped in Answer.
   */
  protected <T> Answer<T> parseFhir(String json, Class<T> type) {
    try {
      if (org.hl7.fhir.dstu3.model.Base.class.isAssignableFrom(type)) {
        logger.trace("Try to parse FHIR resource as STU3");
        return Answer.of((T) stu3.newJsonParser().parseResource(json));
      } else if (org.hl7.fhir.r4.model.Base.class.isAssignableFrom(type)) {
        logger.trace("Try to parse FHIR resource as R4");
        return Answer.of((T) r4.newJsonParser().parseResource(json));
      } else {
        return Answer.unacceptable();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Answer.failed(e);
    }
  }

  /**
   * Tries to serialize a FHIR object as a JSON string.
   * <p>
   * If result is not an instance of {@link IBaseResource} - an interface implemented by all HAPI
   * FHIR objects across versions of FHIR - returns. Otherwise, uses
   * {@link IBaseResource#getStructureFhirVersionEnum()} to determine the FHIR version, and
   * serialize in the appropriate context
   *
   * @param result the object to be serialized
   * @return T, in JSON String format, wrapped in Answer.
   */
  protected <T> Answer<String> serializeFhir(T result) {
    try {
      if (result instanceof IBaseResource) {
        var resource = (IBaseResource) result;
        switch (resource.getStructureFhirVersionEnum()) {
          case DSTU3:
            return Answer.of(stu3.newJsonParser()
                .setPrettyPrint(true).encodeResourceToString(resource));
          case R4:
            return Answer.of(r4.newJsonParser()
                .setPrettyPrint(true).encodeResourceToString(resource));
          default:
        }
      }
      return Answer.unsupported();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Answer.failed(e);
    }
  }
}
