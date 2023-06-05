package edu.mayo.kmdp.health.service;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import java.sql.Connection;
import org.springframework.util.StopWatch;

/**
 * A {@link HealthService} that builds a SQL connection for the purpose of determining if the system
 * is available and functioning as expected (ie: UP, DOWN, or IMPAIRED).
 * <p>
 * Note that the connection is only built and no query is executed.
 */
public interface SqlConnectionHealthService extends HealthService {

  String getName();

  Connection getConnection();

  @Override
  default ApplicationComponent assessHealth() {

    ApplicationComponent applicationComponent = initializeApplicationComponent(getName());

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    // The try-with-resource syntax is used to automatically close the connection
    try (Connection connection = getConnection()) {

      if (connection != null) {
        assignSuccess(applicationComponent);
      }

    } catch (Exception exception) {

      handleException(applicationComponent, exception);

    } finally {

      stopAndAddToDetails(stopWatch, applicationComponent);

    }

    return applicationComponent;

  }

}
