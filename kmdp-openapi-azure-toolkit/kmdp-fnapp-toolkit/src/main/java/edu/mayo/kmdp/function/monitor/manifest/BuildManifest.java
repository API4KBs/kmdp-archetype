package edu.mayo.kmdp.function.monitor.manifest;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the self-introspection logic that relies on the metadata in MANIFEST.MF file to
 * extract build metadata such as GAV identifiers and/or build time.
 * <p>
 * The MANIFEST.MF is built using the maven-jar-plugin, then loaded at runtime using classloader
 * introspection. If successful, the content is parsed into a Map for quick access. Otherwise, any
 * attempt to get a metadata element will return null.
 */
public class BuildManifest {

  private static final Logger logger = LoggerFactory.getLogger(BuildManifest.class);

  private static final String ERROR_KEY = "__MANIFEST_LOAD_ERROR__";

  /**
   * The key/value pairs parsed from the MANIFEST.MF file
   * <p>
   * If parsing fails, the Map will contain an entry that associates ERROR_KEY to a description of
   * the parsing error
   */
  protected Map<String, String> info;

  /**
   * Flag. Set to true if unable to find or parse the MANIFEST.MF file
   */
  protected boolean errored;

  /**
   * Constructor.
   *
   * @param klazz The class to be monitored, used to locate the Jar that contains the MANIFEST
   */
  protected BuildManifest(Class<?> klazz) {
    info = parseManifestFile(klazz);
    errored = info.containsKey(ERROR_KEY);
  }

  /**
   * Retrieves the software package version, as stated in the MANIFEST.MF
   *
   * @return the software package version
   */
  public String getVersion() {
    checkStatus();
    return info.getOrDefault("Implementation-Version", null);
  }

  /**
   * Retrieves the software package artifact Id, as stated in the MANIFEST.MF
   *
   * @return the software package artifact Id
   */
  public String getArtifactId() {
    checkStatus();
    return info.getOrDefault("Implementation-Artifact", null);
  }

  /**
   * Retrieves the software package GroupId, as stated in the MANIFEST.MF
   *
   * @return the software package GroupId
   */
  public String getGroupId() {
    checkStatus();
    return info.getOrDefault("Implementation-Vendor-Id", null);
  }

  /**
   * Retrieves the software package name, as stated in the MANIFEST.MF
   *
   * @return the software package name
   */
  public String getName() {
    checkStatus();
    return info.getOrDefault("Implementation-Title", null);
  }

  /**
   * Retrieves the software package build timestamp, as stated in the MANIFEST.MF
   *
   * @return the software package build timestamp
   */
  public String getBuildTime() {
    checkStatus();
    return info.getOrDefault("Build-Time", null);
  }

  /**
   * Checks whether the MANIFEST.MF has been loaded correctly. Logs a warning if not.
   */
  private void checkStatus() {
    if (errored) {
      logger.warn("No MANIFEST Data Available: {}", getErrorMessage());
    }
  }

  /**
   * @return a description of the reason why the MANIFEST.MF could not be loaded, if errored
   */
  public Optional<String> getErrorMessage() {
    return errored ? Optional.ofNullable(info.get(ERROR_KEY)) : Optional.empty();
  }

  /**
   * Predicate
   *
   * @return true if the MANIFEST.MF was found and loaded, false otherwise
   */
  public boolean isLoaded() {
    return !errored;
  }


  /**
   * Tries to locate the MANIFEST.MF of this function app's package on the classpath, then parses it
   * to extract the key/value metadta properties
   * <p>
   * If unable to load the MANIFEST.MF, returns a Map with one entry, where the key is
   * {@link #ERROR_KEY}, and the value is a description of the underlying error/exception
   *
   * @param klazz The class to be monitored, used to locate the Jar that contains the MANIFEST
   * @return the key/value properties in the MANIFEST.MF
   */
  private Map<String, String> parseManifestFile(Class<?> klazz) {
    try {
      logger.debug("Trying to access MANIFEST.MF");

      var klassPath = klazz.getName().replace(".","/") + ".class";
      var url = klazz.getClassLoader().getResource(klassPath);
      var manifestRef = Optional.ofNullable(url)
          .map(Objects::toString)
          .map(s -> s.replace(klassPath,"META-INF/MANIFEST.MF"));

      if (logger.isDebugEnabled()) {
        logger.debug("Trying to load MANIFEST : {}", manifestRef);
      }

      if (manifestRef.isPresent()) {
        var manifestUrl = new URL(manifestRef.get());
        try (var inStream = manifestUrl.openStream()) {
          Manifest manifest = new Manifest(inStream);
          logger.debug("Reading MANIFEST.MF...");
          return manifest.getMainAttributes().entrySet().stream()
              .collect(Collectors.toMap(
                  e -> Objects.toString(e.getKey()),
                  e -> Objects.toString(e.getValue())
              ));
        }
      } else {
        return Map.of(ERROR_KEY, "Unable to find MANIFEST.MF");
      }
    } catch (Exception e) {
      logger.warn(e.getMessage(), e);
      return Map.of(ERROR_KEY, e.getMessage());
    }
  }

//  private Map<String, String> parseManifestFile() {
//    try {
//      logger.debug("Trying to access MANIFEST.MF");
//
//      var manifestUrl =
//          BuildManifest.class.getClassLoader().resources("META-INF/MANIFEST.MF")
//              .filter(mfUrl -> mfUrl.getPath().matches(
//                  ".*/kmdp-knowledge-package-assembler-fnapp-\\d+.\\d+.\\d+(-SNAPSHOT)?.jar.*"))
//              .findFirst();
//
//      if (logger.isDebugEnabled()) {
//        logger.debug("Selected Jar : {}", manifestUrl.map(Objects::toString).orElse(null));
//      }
//
//      if (manifestUrl.isPresent()) {
//        try (var inStream = manifestUrl.get().openStream()) {
//          Manifest manifest = new Manifest(inStream);
//          logger.debug("Reading MANIFEST.MF...");
//          return manifest.getMainAttributes().entrySet().stream()
//              .collect(Collectors.toMap(
//                  e -> Objects.toString(e.getKey()),
//                  e -> Objects.toString(e.getValue())
//              ));
//        }
//      } else {
//        return Map.of(ERROR_KEY, "Unable to find MANIFEST.MF");
//      }
//    } catch (Exception e) {
//      logger.warn(e.getMessage(), e);
//      return Map.of(ERROR_KEY, e.getMessage());
//    }
//  }

}
