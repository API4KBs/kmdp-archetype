package edu.mayo.kmdp.util.ws;

import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.SNAPSHOT;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.omg.spec.api4kp._20200801.id.Pointer;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.taxonomy.clinicalknowledgeassettype.ClinicalKnowledgeAssetTypeSeries;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Spring HTML adapter, specialized in the handling of collections of API4KP {@link Pointer}, when
 * the client negotiates text/html instead of application/json or application/xml.
 * <p>
 * By default, builds a table, where each pointer is a row, and the columns correspond to
 * <li>
 *   <ul>{@link Pointer#getVersionId()}, linked to the {@link Pointer#getHref()}</ul>
 *   <ul>{@link Pointer#getName()}</ul>
 *   <ul>{@link Pointer#getType()}, mapped to Term#getPrefLabel</ul>
 * </li>
 */
public class PointerHTMLAdapter implements HttpMessageConverter<List<Pointer>> {

  @Override
  public boolean canRead(@Nonnull Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public boolean canWrite(@Nonnull Class<?> clazz, MediaType mediaType) {
    return MediaType.TEXT_HTML.equals(mediaType) && List.class.isAssignableFrom(clazz);
  }

  @Override
  @Nonnull
  public List<MediaType> getSupportedMediaTypes() {
    return List.of(MediaType.TEXT_HTML);
  }

  @Override
  @Nonnull
  public List<Pointer> read(
      @Nonnull Class<? extends List<Pointer>> clazz, @Nonnull HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    return Collections.emptyList();
  }

  @Override
  public void write(List<Pointer> pointer, MediaType contentType,
      HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    var pointerDescrs = pointer.stream()
        .sorted(Comparator.comparing(this::resolveType)
            .thenComparing(this::resolveName))
        .toArray();
    var html = ReflectionToStringBuilder.toString(
        pointerDescrs, new PointerTableStyle(getTranslator()));
    outputMessage.getBody().write(html.getBytes());
  }

  /**
   * Core rendering method that associates a Pointer to a collection of field/element extractors,
   * which is later used to assemble the rendered HTML
   *
   * @return a Map of element names to the functions that extract the element values
   */
  protected Map<String, BiConsumer<Pointer, StringBuffer>> getTranslator() {
    var translator = new LinkedHashMap<String, BiConsumer<Pointer, StringBuffer>>();

    translator.put("ID", (item, buffer) -> {
      if (item.getHref() != null) {
        buffer.append("<a href=\"")
            .append(item.getHref())
            .append("\">")
            .append(item.asKey())
            .append("</a>");
      } else {
        buffer.append(item.asKey());
      }
    });

    translator.put("Date", (item, buffer) -> buffer.append(
        item.getVersionTag().contains(SNAPSHOT)
            ? item.getEstablishedOn() : ""));

    translator.put("Label", (item, buffer) -> buffer.append(item.getName()));

    translator.put("Type", (item, buffer) -> buffer.append(resolveType(item)));


    return translator;
  }


  protected String resolveType(Pointer item) {
    return KnowledgeAssetTypeSeries.resolveRef(item.getType())
        .or(() -> ClinicalKnowledgeAssetTypeSeries.resolveRef(item.getType()))
        .map(Term::getPrefLabel)
        .or(() -> Optional.ofNullable(item.getType()).map(Objects::toString))
        .orElse("n/a");
  }

  protected String resolveName(Pointer ptr) {
    return Optional.ofNullable(ptr.getName())
        .orElse("(no name)");
  }


  /**
   * Style that assembles the Pointer's data elements into a HTML structure
   */
  protected static class PointerTableStyle extends ToStringStyle {

    final Map<String, BiConsumer<Pointer, StringBuffer>> translator;

    public PointerTableStyle(Map<String, BiConsumer<Pointer, StringBuffer>> translator) {
      this.translator = translator;
      initStyleContext();
    }

    private void initStyleContext() {
      setContentStart("<table><tbody>");

      setSummaryObjectStartText("<tr><td>");
      setArrayStart("");
      setArrayEnd("");
      setArraySeparator("");
      setFieldSeparator("</td><td>");
      setSummaryObjectEndText("</td></tr>");

      setUseFieldNames(false);
      setArrayContentDetail(false);
      setUseShortClassName(false);
      setUseClassName(false);
      setUseIdentityHashCode(false);

      setContentEnd("</tbody></table>");
    }


    @Override
    protected void appendInternal(final StringBuffer buffer, final String fieldName,
        final Object obj, final boolean detail) {
      if (obj instanceof Pointer) {
        Pointer item = (Pointer) obj;
        buffer.append(getSummaryObjectStartText());

        var iter = translator.values().iterator();
        while (iter.hasNext()) {
          iter.next().accept(item, buffer);
          if (iter.hasNext()) {
            buffer.append(getFieldSeparator());
          }
        }

        buffer.append(getSummaryObjectEndText());
      }
    }

  }

}
