package edu.mayo.kmdp.health.service;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.Status;
import java.util.function.Supplier;
import org.springframework.util.StopWatch;

/**
 * A {@link HealthService} determines if a given application component, such as a database or other
 * external system, is functioning as expected (ie: UP, DOWN, or IMPAIRED).
 *
 * An example of how to add an implementation to your application's Spring
 * ApplicationContext is:
 *
 * <pre>{@code
 *
 *  @Configuration
 *  public class ApplicationMonitoringConfig {
 *
 *   @Bean
 *   public Supplier<ApplicationComponent> sampleHealth(
 *       @Autowired(required = false) SampleHealthService sampleHealthService) {
 *
 *     return sampleHealthService.getApplicationComponentSupplier();
 *
 *   }
 *
 *  }
 *
 * }
 * }</pre>
 *
 */
public interface HealthService {

  String STATUS_AVAILABLE = "Available";
  String STATUS_UNAVAILABLE = "Unavailable";
  String EXECUTION_TIME_MS = "executionTimeMs";
  String MESSAGE_EXCEPTION = "Unable to interrogate, exception: '%s'";

  default Supplier<ApplicationComponent> getApplicationComponentSupplier() {

    // Invoke the implementation's assessHealth() when the Supplier.get() is invoked
    return this::assessHealth;

  }

  ApplicationComponent assessHealth();

  default ApplicationComponent initializeApplicationComponent(String name) {

    ApplicationComponent applicationComponent = new ApplicationComponent();
    applicationComponent.setName(name);
    applicationComponent.setStatus(Status.DOWN);
    applicationComponent.setStatusMessage(STATUS_UNAVAILABLE);

    MiscProperties miscProperties = new MiscProperties();
    applicationComponent.setDetails(miscProperties);

    return applicationComponent;

  }

  default void assignSuccess(ApplicationComponent applicationComponent) {

    applicationComponent.setStatus(Status.UP);
    applicationComponent.setStatusMessage(STATUS_AVAILABLE);

  }

  default void handleException(ApplicationComponent applicationComponent, Exception exception) {

    String message = String.format(MESSAGE_EXCEPTION,
        exception.getMessage());

    applicationComponent.setStatusMessage(message);

  }

  default void stopAndAddToDetails(StopWatch stopWatch, ApplicationComponent applicationComponent) {

    if (stopWatch.isRunning()) {
      stopWatch.stop();
    }

    long executionMillis = stopWatch.getTotalTimeMillis();

    MiscProperties miscProperties = applicationComponent.getDetails();
    miscProperties.put(EXECUTION_TIME_MS, String.valueOf(executionMillis));

  }

}
