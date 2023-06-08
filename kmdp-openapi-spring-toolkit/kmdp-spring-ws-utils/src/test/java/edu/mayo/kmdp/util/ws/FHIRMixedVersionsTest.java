package edu.mayo.kmdp.util.ws;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.ws.JsonRestWSUtils.WithFHIR;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class FHIRMixedVersionsTest {

  @Test
  void testFHIROnlyMapper() {
    Bundle b4 = new Bundle();
    Parameters p3 = new Parameters();
    var conv = JsonRestWSUtils.buildFHIRAdapter(WithFHIR.R4ONLY);

    var p3Write = conv.canWrite(p3.getClass(), MediaType.APPLICATION_JSON);
    var b4Write = conv.canWrite(b4.getClass(), MediaType.APPLICATION_JSON);
    var jWrite = conv.canWrite(Foo.class, MediaType.APPLICATION_JSON);

    assertTrue(b4Write);
    assertFalse(p3Write);
    assertFalse(jWrite);
  }
  static class Foo {

  }

}
