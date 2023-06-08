/**
 * Copyright © 2020 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
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


import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Message Converter used to adapt HTML streams into KnowledgeCarriers
 *
 * Used to handle redirects, helps ensure compatibility between KMDP APIs and non-KMDP endpoints
 */
public class HTMLKnowledgeCarrierWrapper implements HttpMessageConverter<KnowledgeCarrier> {

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return getSupportedMediaTypes().contains(mediaType)
        && KnowledgeCarrier.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return getSupportedMediaTypes().contains(mediaType)
        && KnowledgeCarrier.class.isAssignableFrom(clazz);
  }


  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return Arrays.asList(MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML);
  }

  @Override
  public void write(KnowledgeCarrier knowledgeCarrier, MediaType contentType,
      HttpOutputMessage outputMessage) throws IOException {
    if (HTML.sameAs(knowledgeCarrier.getRepresentation().getLanguage())) {
     String html = knowledgeCarrier.asString()
         .orElseThrow(() -> new HttpMessageNotWritableException(
             "Unable to stream HTML content: expected a 'serialized expression', but was "
                 + knowledgeCarrier.getLevel().getLabel()));
     outputMessage.getBody().write(html.getBytes());
    } else {
      throw new HttpMessageNotWritableException("Expected HTML Carrier, but found "
          + knowledgeCarrier.getRepresentation().getLanguage().getLabel());
    }
  }

  @Override
  public KnowledgeCarrier read(Class clazz, HttpInputMessage inputMessage)
      throws IOException {
    return AbstractCarrier.of(inputMessage.getBody())
        .withRepresentation(AbstractCarrier.rep(HTML,TXT))
        .withAssetId(SemanticIdentifier.newId(UUID.randomUUID()))
        .withArtifactId(SemanticIdentifier.newId(UUID.randomUUID()));
  }
}
