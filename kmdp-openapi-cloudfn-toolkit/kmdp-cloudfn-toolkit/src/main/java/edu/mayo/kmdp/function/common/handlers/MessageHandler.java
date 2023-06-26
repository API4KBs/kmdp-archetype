package edu.mayo.kmdp.function.common.handlers;


/*
 * Injectable service that handles complex datatypes in body and responses payloads
 */
public interface MessageHandler {

  /**
   * Parses an incoming message
   */
  default <T> org.omg.spec.api4kp._20200801.Answer<T> parse(String json, Class<T> type) {
    return org.omg.spec.api4kp._20200801.Answer.ofTry(
        edu.mayo.kmdp.util.JSonUtil.parseJson(json, type));
  }

  /**
   * Serializes an outgoing message
   */
  default <T> org.omg.spec.api4kp._20200801.Answer<String> serialize(T result) {
    return org.omg.spec.api4kp._20200801.Answer.ofTry(
        edu.mayo.kmdp.util.JSonUtil.writeJsonAsString(result));
  }
}
