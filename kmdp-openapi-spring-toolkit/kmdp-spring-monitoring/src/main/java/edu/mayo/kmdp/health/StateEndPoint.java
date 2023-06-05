package edu.mayo.kmdp.health;

import static edu.mayo.kmdp.health.utils.MonitorUtil.getBuildProps;
import static edu.mayo.kmdp.health.utils.MonitorUtil.getServiceNowInfo;
import static edu.mayo.kmdp.util.Util.isEmpty;
import static edu.mayo.kmdp.util.Util.isNotEmpty;

import edu.mayo.kmdp.health.datatype.Flags;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.SchemaMetaInfo;
import edu.mayo.kmdp.health.datatype.StateData;
import edu.mayo.kmdp.health.utils.MonitorUtil;
import edu.mayo.kmdp.health.utils.PropKey;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class StateEndPoint implements StateApiDelegate {

  @Autowired
  protected ConfigurableEnvironment environment;

  @Autowired
  protected BuildProperties build;

  /**
   * Application specific Predicate bean Provides logic for identifying 'feature flag' application
   * properties Feature flags are boolean properties that enable/disable application capabilities
   */
  @Autowired(required = false)
  @Qualifier("flag")
  protected Predicate<String> flagTester;

  /**
   * Application-specific bean to override the lenience when obfuscating sensitive configuration
   * properties
   */
  @Autowired(required = false)
  @Qualifier("obfuscate")
  protected Integer obfuscateLenience;

  /**
   * Application specific Predicate bean Provides logic for identifying 'secret' application
   * properties Secret properties need to be obfuscated
   */
  @Autowired(required = false)
  @Qualifier("secret")
  protected Predicate<String> secretTester;

  /**
   * Metadata about the {@link StateData} schema used to report information
   *
   * @return SchemaMetaInfo for {@link StateData}
   */
  static SchemaMetaInfo schemaMetaInfo() {
    return MonitoringSchema.stateMetaInfo();
  }

  /**
   * Collects {@link StateData} information for the application, as of the time the application was
   * loaded
   *
   * @return the StateData payload
   */
  @Override
  public ResponseEntity<StateData> getStateData() {
    var state = new StateData();
    var props = MonitorUtil.streamConvert(
        MonitorUtil.getEnvironmentProperties(environment),
        LinkedHashMap::new);

    state.setServiceNowReference(getServiceNowInfo(environment));
    state.setBuildConfiguration(getBuildProps(build));
    state.setDeploymentEnvironmentConfiguration(getDeploymentProperties(props));
    state.setFeatures(getFeatureFlags(props));

    state.setSchemaInfo(schemaMetaInfo());

    return ResponseEntity.ok(state);
  }

  /**
   * Extracts the subset of configuration properties that are feature flags
   * <p>
   * Uses the application-provided filter if present, otherwise falls back to a default strategy
   *
   * @param envProperties The environment configuration properties
   * @return the feature flags
   * @see #isFeatureFlag(String)
   */
  protected Flags getFeatureFlags(Map<String, String> envProperties) {
    var flags = new Flags();
    envProperties.entrySet().stream()
        .filter(e -> isFeatureFlag(e.getKey()))
        .forEach(e -> flags.put(e.getKey(), Boolean.valueOf(e.getValue())));
    return flags;
  }

  /**
   * Predicate that determines whether a property is a feature flag or not based on its name
   * Delegates to the application-provided logic, if present. Otherwise, checks whether the property
   * name starts with a canonical prefix
   *
   * @param key the configuration property name
   * @return true if the property is a feature flag
   * @see MonitorUtil#defaultIsFlag(String)
   */
  private boolean isFeatureFlag(String key) {
    return flagTester != null
        ? flagTester.test(key)
        : defaultIsFlag(key);
  }

  /**
   * Predicate that determines whether a property is a secret or not based on its name Delegates to
   * the application-provided logic, if present. Otherwise, checks whether the property name
   * contains "token", "secret" or "pasword".
   *
   * @param key the configuration property name
   * @return true if the property is a secret that should be obfuscated
   */
  private boolean isSecret(String key) {
    return secretTester != null
        ? secretTester.test(key)
        : defaultIsSecret(key);
  }

  protected MiscProperties getDeploymentProperties(Map<String, String> envProperties) {
    var deployProperties = new MiscProperties();

    Map<String, List<String>> secrets = new HashMap<>();
    envProperties.entrySet().stream()
        .filter(e -> isSecret(e.getKey()))
        .forEach(
            p -> secrets.computeIfAbsent(p.getValue(), x -> new LinkedList<>()).add(p.getKey()));

    envProperties.entrySet().stream()
        .filter(e -> !PropKey.isKnownProperty(e.getKey()))
        .filter(e -> !isFeatureFlag(e.getKey()))
        .map(this::obfuscate)
        .forEach(e -> deployProperties.put(e.getKey(), cleanse(e.getValue(), e.getKey(), secrets)));
    return deployProperties;
  }

  /**
   * Removes secrets from the value of other properties.
   * <p>
   * Prevents situations where secrets are embedded in other non-secret properties
   * <p>
   * This method excludes the secret properties themselves, whose value should have been obfuscated
   * at this point. That is, if 'my.secret=XYZ' was a secret property, one would have
   * 'key=my.secret', 'value='X**', 'secrets={XYZ=[my.secret]}'.
   * <p>
   * #1353140 - since multiple properties can share the same secret, the reverse index should be a
   * MultiMap: String -> List(String)
   *
   * @param value   The value of a (non-secret) property
   * @param key     The name of the (non-secret) property
   * @param secrets The values of the secret properties
   * @return The value of the (non-secret) property, with any secret component obfuscated
   */
  protected String cleanse(String value, String key, Map<String, List<String>> secrets) {
    if (isSecret(key) || isEmpty(value)) {
      return value;
    }
    for (var secretUsage : secrets.entrySet()) {
      String secret = secretUsage.getKey();
      // do not rewrite empty or 'mock' secrets that would occur naturally
      if (isNotEmpty(secret) && secret.length() > 2 && value.contains(secret)) {
        value = value.replaceAll(secret,
            "**" + String.join("|", secretUsage.getValue() + "**"));
      }
    }
    return value;
  }

  /**
   * Checks whether a property needs to be obfuscated and, if so, replaces the characters of the
   * property value with '*', minus the first three characters for partial transparency Properties
   * are selected based on an application-specific predicate, if present. Otherwise, uses a default
   * strategy
   *
   * @param e a key/value pair that represents the configuration property and its value
   * @return the key/value, where the value has been obfuscated if necessary
   * @see MonitorUtil#obfuscate(String, int)
   * @see #defaultIsSecret(String)
   */
  protected Entry<String, String> obfuscate(Entry<String, String> e) {
    String key = e.getKey().toLowerCase();
    if (isSecret(key)) {
      e.setValue(MonitorUtil.obfuscate(e.getValue(), obfuscateLenience != null ? Math.max(obfuscateLenience, 0) : 3));
    }
    return e;
  }

  /**
   * Checks the property name for the presence of "password", "token" or "secret"
   *
   * @param key The property name to be tested
   * @return true if the property is considered to be secret, thus needing obfuscation
   */
  protected boolean defaultIsSecret(String key) {
    return MonitorUtil.defaultIsSecret(key);
  }

  /**
   * Checks the property name for the presence of "flag."
   *
   * @param key The property name to be tested
   * @return true if the property is considered to be a feature flag
   */
  protected boolean defaultIsFlag(String key) {
    return MonitorUtil.defaultIsFlag(key);
  }


}
