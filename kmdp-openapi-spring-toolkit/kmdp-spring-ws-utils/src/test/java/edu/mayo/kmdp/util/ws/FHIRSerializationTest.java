/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.util.ws;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.StringDt;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mayo.kmdp.util.fhir.fhir2.FHIR2JsonUtil;
import edu.mayo.kmdp.util.fhir.fhir2_hl7.FHIR2HL7JsonUtil;
import edu.mayo.kmdp.util.fhir.fhir3.FHIR3JsonUtil;
import edu.mayo.kmdp.util.ws.JsonRestWSUtils.WithFHIR;
import java.io.ByteArrayOutputStream;
import org.hl7.fhir.dstu2.model.MedicationStatement;
import org.hl7.fhir.dstu2.model.MedicationStatement.MedicationStatementDosageComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FHIRSerializationTest {

  private Patient getFHIR2Resource() {
    return (Patient) new Patient()
        .setName(singletonList(new HumanNameDt()
            .setGiven(singletonList(new StringDt("John")))
            .setFamily(singletonList(new StringDt("Doe")))
        ))
        .setId("thePt123");
  }

  @Test
  void testNonFHIR() {
    Patient patient = getFHIR2Resource();
    ObjectMapper mapper = JsonRestWSUtils.getObjectMapper(WithFHIR.NONE);

    Assertions.assertThrows(JsonMappingException.class,
        () -> mapper.writer().writeValue(new ByteArrayOutputStream(), patient));
  }

  @Test
  void testFHIR2() {
    Patient patient = getFHIR2Resource();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    ObjectMapper mapper = JsonRestWSUtils.getObjectMapper(WithFHIR.DSTU2);

    Assertions.assertDoesNotThrow(
        () -> mapper.writer().writeValue(baos, patient));
    String jsonStr = new String(baos.toByteArray());
    assertTrue(jsonStr.contains(patient.getNameFirstRep().getGivenFirstRep().getValue()));

    Patient reconstructed = FHIR2JsonUtil.instance.parse(baos.toByteArray(),Patient.class);
    assertEquals(patient.getName().get(0).getFamily(),
        reconstructed.getName().get(0).getFamily());
  }


  private MedicationStatement getFHIR2HL7Resource() {
    return (MedicationStatement) new MedicationStatement()
        .setMedication(new org.hl7.fhir.dstu2.model.CodeableConcept()
            .addCoding(new org.hl7.fhir.dstu2.model.Coding()
                .setCode("aspirin")))
        .addDosage(new MedicationStatementDosageComponent()
            .setText("Not too much"))
        .setId("theMed789");
  }

  @Test
  void testNonFHIRHL7() {
    MedicationStatement med = getFHIR2HL7Resource();
    ObjectMapper mapper = JsonRestWSUtils.getObjectMapper(WithFHIR.NONE);

    Assertions.assertThrows(JsonMappingException.class,
        () -> mapper.writer().writeValue(new ByteArrayOutputStream(), med));
  }

  @Test
  void testFHIR2HL7() {
    MedicationStatement med = getFHIR2HL7Resource();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    ObjectMapper mapper = JsonRestWSUtils.getObjectMapper(WithFHIR.DSTU2HL7);

    Assertions.assertDoesNotThrow(
        () -> mapper.writer().writeValue(baos, med));
    String jsonStr = new String(baos.toByteArray());
    assertTrue(jsonStr.contains(med.getDosage().get(0).getText()));

    MedicationStatement reconstructed = FHIR2HL7JsonUtil.instance.parse(baos.toByteArray(),MedicationStatement.class);
    assertEquals(med.getDosage().get(0).getText(),
        reconstructed.getDosage().get(0).getText());

  }



  private Observation getFHIR3Resource() {
    return (Observation) new Observation()
        .setCode(new CodeableConcept()
            .setCoding(singletonList(new Coding()
                .setCode("123")
                .setSystem("foo"))))
        .setId("theObs456");
  }

  @Test
  void testNonFHIR3() {
    Observation obs = getFHIR3Resource();
    ObjectMapper mapper = JsonRestWSUtils.getObjectMapper(WithFHIR.NONE);

    Assertions.assertThrows(JsonMappingException.class,
        () -> mapper.writer().writeValue(new ByteArrayOutputStream(), obs));
  }

  @Test
  void testFHIR3() {
    Observation obs = getFHIR3Resource();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    ObjectMapper mapper = JsonRestWSUtils.getObjectMapper(WithFHIR.STU3);

    Assertions.assertDoesNotThrow(
        () -> mapper.writer().writeValue(baos, obs));

    String jsonStr = new String(baos.toByteArray());
    assertTrue(jsonStr.contains(obs.getCode().getCodingFirstRep().getCode()));

    Observation reconstructed = FHIR3JsonUtil.instance.parse(baos.toByteArray(),Observation.class);
    assertEquals(obs.getCode().getCodingFirstRep().getCode(),
        reconstructed.getCode().getCodingFirstRep().getCode());

  }

}
