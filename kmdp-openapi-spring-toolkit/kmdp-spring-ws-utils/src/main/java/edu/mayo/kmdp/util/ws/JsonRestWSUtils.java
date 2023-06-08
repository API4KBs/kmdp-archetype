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

import ca.uhn.fhir.model.api.BaseElement;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import edu.mayo.kmdp.util.JSonUtil;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hl7.fhir.dstu3.model.Base;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class JsonRestWSUtils {

  public enum WithFHIR {
    DSTU2, DSTU2HL7, STU3, STU3ONLY, R4, R4ONLY, NONE;
  }

  public static MappingJackson2HttpMessageConverter jacksonFHIRAdapter() {
    return jacksonFHIRAdapter(WithFHIR.NONE);
  }

  public static MappingJackson2HttpMessageConverter jacksonFHIRAdapter(WithFHIR fhir) {
    return buildFHIRAdapter(fhir);
  }

  protected static ObjectMapper getObjectMapper(WithFHIR fhir) {
    return configure(new ObjectMapper(), fhir);
  }

  private static ObjectMapper configure(ObjectMapper objectMapper, WithFHIR fhir) {
    objectMapper = JSonUtil.configureMapper(objectMapper);
    try {
      // The dependencies that provide the Jackson Modules are *provided* only as needed.
      // The use of the reflective constructors avoids the need to have ALL of them at runtime.
      switch (fhir) {
        case DSTU2:
          objectMapper.registerModule(
              (Module) Class.forName("edu.mayo.kmdp.util.fhir.fhir2.FHIR2JacksonModule")
                  .getConstructor().newInstance());
          break;
        case DSTU2HL7:
          objectMapper.registerModule(
              (Module) Class.forName("edu.mayo.kmdp.util.fhir.fhir2_hl7.FHIR2HL7JacksonModule")
                  .getConstructor().newInstance());
          break;
        case STU3:
        case STU3ONLY:
          objectMapper.registerModule(
              (Module) Class.forName("edu.mayo.kmdp.util.fhir.fhir3.FHIR3JacksonModule")
                  .getConstructor().newInstance());
          break;
        case R4:
        case R4ONLY:
          objectMapper.registerModule(
              (Module) Class.forName("edu.mayo.kmdp.util.fhir.fhir4.FHIR4JacksonModule")
                  .getConstructor().newInstance());
          break;
        case NONE:
        default:
      }
    } catch (Exception e) {
      throw new FHIRConfigurationException(e);
    }

    return objectMapper;
  }

  public static RestTemplate enableFHIR(RestTemplate restTemplate, WithFHIR fhir) {
    return configure(
        replaceConverters(
            restTemplate,
            buildFHIRAdapter(fhir)),
        fhir);
  }

  public static RestTemplate replaceConverters(RestTemplate restTemplate,
      MappingJackson2HttpMessageConverter replacementConverter) {
    Set<AbstractJackson2HttpMessageConverter> defaultJacksonConverters =
        restTemplate.getMessageConverters().stream()
            .filter(AbstractJackson2HttpMessageConverter.class::isInstance)
            .map(AbstractJackson2HttpMessageConverter.class::cast)
            .collect(Collectors.toSet());
    restTemplate.getMessageConverters().removeAll(defaultJacksonConverters);
    restTemplate.getMessageConverters().add(replacementConverter);
    return restTemplate;
  }

  public static RestTemplate configure(RestTemplate restTemplate, WithFHIR fhir) {
    MappingJackson2HttpMessageConverter messageConverter = restTemplate.getMessageConverters()
        .stream()
        .filter(MappingJackson2HttpMessageConverter.class::isInstance)
        .map(MappingJackson2HttpMessageConverter.class::cast)
        .findFirst().orElseThrow(
            () -> new IllegalStateException("MappingJackson2HttpMessageConverter not found"));

    configure(messageConverter.getObjectMapper(), fhir);

    return restTemplate;
  }

  public static MappingJackson2HttpMessageConverter buildFHIRAdapter(WithFHIR fhir) {
    var objectMapper = getObjectMapper(fhir);
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter() {
      @Override
      protected JavaType getJavaType(Type type, Class<?> contextClass) {
        if (type instanceof Class<?> && Map.class.isAssignableFrom((Class<?>) type)) {
          switch (fhir) {
            case R4:
            case R4ONLY:
              return TypeFactory.defaultInstance()
                  .constructMapType((Class<? extends Map<String,?>>) type, String.class,
                      org.hl7.fhir.r4.model.Base.class);
            case STU3:
            case STU3ONLY:
              return TypeFactory.defaultInstance()
                  .constructMapType((Class<? extends Map<String,?>>) type, String.class, Base.class);
            case DSTU2:
              return TypeFactory.defaultInstance()
                  .constructMapType((Class<? extends Map<String,?>>) type, String.class, BaseElement.class);
            case NONE:
            default:
              return super.getJavaType(type, contextClass);
          }
        } else {
          return super.getJavaType(type, contextClass);
        }
      }
    };
    jsonConverter.setObjectMapper(objectMapper);
    switch (fhir){
      case R4ONLY:
        jsonConverter.registerObjectMappersForType(org.hl7.fhir.r4.model.Base.class, map -> map.put(
            MediaType.APPLICATION_JSON, objectMapper));
        jsonConverter.registerObjectMappersForType(org.hl7.fhir.dstu3.model.Base.class, map -> {});
        break;
      case STU3ONLY:
        jsonConverter.registerObjectMappersForType(org.hl7.fhir.dstu3.model.Base.class, map -> map.put(
            MediaType.APPLICATION_JSON, objectMapper));
        jsonConverter.registerObjectMappersForType(org.hl7.fhir.r4.model.Base.class, map -> {});
        break;
      default:
    }

    return jsonConverter;
  }


  private static class FHIRConfigurationException extends RuntimeException {

    public FHIRConfigurationException(Exception e) {
      super(e);
    }
  }
}