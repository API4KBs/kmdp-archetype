package edu.mayo.kmdp.util.ws;

import java.util.Locale;
import org.springframework.context.support.ResourceBundleMessageSource;

// TODO MOVE ME to the components/messages package
public class ExternalBundleMessageProvider {

  private static final Object[] noArgs = new Object[0];

  ResourceBundleMessageSource msgSource;

  public ExternalBundleMessageProvider(String srcBundle) {
    this.msgSource = loadMessageSourceBundle(srcBundle);
  }

  public String getDefaultMessage(String msgCode) {
    return msgSource != null
        ? msgSource.getMessage(msgCode, noArgs, msgCode, Locale.getDefault())
        : null;
  }

  public String getMessage(
      String msgCode, Object[] args, String defaultMessage, Locale aDefault) {
    return msgSource != null
        ? msgSource.getMessage(msgCode, args, defaultMessage, aDefault)
        : null;
  }

  private ResourceBundleMessageSource loadMessageSourceBundle(String srcBundle) {
    var source = new ResourceBundleMessageSource();
    source.setBasenames(srcBundle);
    source.setUseCodeAsDefaultMessage(true);
    return source;
  }

}
