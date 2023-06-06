package edu.mayo.kmdp.health;

import edu.mayo.kmdp.health.datatype.MiscProperties;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * Helper class that holds a State/Health property, its default value, and an operator to obfuscate
 * the value, in case the property value contains a secret
 * <p>
 * This class is definitional, in the sense that it holds the parameters needed to resolve the
 * property value, to be reported via the monitoring API, but does not contain the actual value
 * itself.
 *
 * @see MiscProperties
 */
public class MonitorablePropertyDef {

  /**
   * The property key/name.
   */
  private final String key;

  /**
   * The property default value, in case the property is not set or cannot be resolved in the
   * Environment. Can be null.
   */
  private final String defaultValue;

  /**
   * An operator used to obfuscate a property value, for properties that contain secrets.
   */
  private final BinaryOperator<String> censor;

  public MonitorablePropertyDef(String key) {
    this(key, null, (k, v) -> v);
  }

  public MonitorablePropertyDef(String key, String defaultValue) {
    this(key, defaultValue, (k, v) -> v);
  }

  public MonitorablePropertyDef(String key, BinaryOperator<String> censor) {
    this(key, null, censor);
  }

  public MonitorablePropertyDef(String key, String defaultValue, BinaryOperator<String> censor) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.censor = censor;
  }

  /**
   * Resolves a set of {@link MonitorablePropertyDef} in the Environment.
   * <p>
   * Note that the values returned by this function should be for monitoring / presentation only.
   * This function is best-effort, does not attempt to validate the results, and may obfuscate some
   * actual values
   *
   * @param environmentProperties a set of {@link MonitorablePropertyDef}
   * @return a Map of property keys / resolved property values
   * @see #resolveProperty(MonitorablePropertyDef)
   */
  public static MiscProperties resolveProperties(
      Set<MonitorablePropertyDef> environmentProperties) {
    var props = new MiscProperties();
    environmentProperties.forEach(propDef -> {
      var value = resolveProperty(propDef);
      props.put(propDef.key, propDef.censor.apply(propDef.key, value));
    });
    return props;
  }

  /**
   * Resolves a {@link MonitorablePropertyDef} in the Environment. Uses the Environment value, else
   * a client-provided default, else returns null
   **/
  private static String resolveProperty(MonitorablePropertyDef propDef) {
    return Optional.ofNullable(System.getenv(propDef.key))
        .orElse(propDef.defaultValue);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonitorablePropertyDef that = (MonitorablePropertyDef) o;
    return Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key);
  }

}
