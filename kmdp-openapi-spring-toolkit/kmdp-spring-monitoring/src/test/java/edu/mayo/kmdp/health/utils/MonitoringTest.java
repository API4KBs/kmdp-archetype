package edu.mayo.kmdp.health.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.health.HealthEndPoint;
import edu.mayo.kmdp.health.StateEndPoint;
import edu.mayo.kmdp.health.VersionEndPoint;
import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.StateData;
import edu.mayo.kmdp.health.datatype.Status;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

@SpringBootTest(classes = MonitoringTestConfig.class)
@TestPropertySource("classpath:mock.properties")
class MonitoringTest {

  @Autowired
  StateEndPoint state;
  @Autowired
  VersionEndPoint version;
  @Autowired
  HealthEndPoint health;

  @Test
  void testVersion() {
    assertNotNull(version);
    assertEquals("0.0.0-FAKE", version.getVersionData().getBody());
  }

  @Test
  void testHealth() {
    ResponseEntity<HealthData> data = health.getHealthData();
    assertNotNull(data.getBody());

    HealthData health = data.getBody();
    assertNotNull(health);

    assertNotNull(health.getStatus());
    assertNotNull(health.getAt());
    assertNotNull(health.getName());
  }

  @Test
  void testState() {
    assertNotNull(state);
    ResponseEntity<StateData> data = state.getStateData();
    assertNotNull(data.getBody());

    StateData state = data.getBody();
    assertNotNull(state);

    assertTrue(state.getFeatures().containsKey("my.feature.flag"));
    assertFalse(state.getFeatures().get("my.feature.flag"));
  }

  @Test
  void testObfuscation() {
    StateData data = state.getStateData().getBody();
    assertNotNull(data);
    Map<String, String> props = data.getDeploymentEnvironmentConfiguration();
    assertTrue(props.containsKey("foo"));
    assertEquals("bar", props.get("foo"));

    Map<String, Boolean> flags = data.getFeatures();
    assertEquals(1, flags.size());
  }

  @Test
  void testObfuscation1() {
    StateData data = state.getStateData().getBody();
    assertNotNull(data);
    Map<String, String> props = data.getDeploymentEnvironmentConfiguration();

    assertTrue(props.containsKey("my.secret"));
    String val1 = props.get("my.secret");
    assertNotNull(val1);
    assertTrue(val1.startsWith("nob"));
    assertFalse(val1.startsWith("nobody"));
  }

  @Test
  void testObfuscation2() {
    StateData data = state.getStateData().getBody();
    assertNotNull(data);
    Map<String, String> props = data.getDeploymentEnvironmentConfiguration();

    assertTrue(props.containsKey("my.password"));
    String val1 = props.get("my.password");
    assertEquals("123******", val1);
  }

  @Test
  void testObfuscation3() {
    StateData data = state.getStateData().getBody();
    assertNotNull(data);
    Map<String, String> props = data.getDeploymentEnvironmentConfiguration();

    assertTrue(props.containsKey("my.override"));
    String val1 = props.get("my.override");
    assertTrue(val1.startsWith("dis*"));
  }

  @Test
  void testObfuscation4() {
    StateData data = state.getStateData().getBody();
    assertNotNull(data);
    Map<String, String> props = data.getDeploymentEnvironmentConfiguration();

    assertTrue(props.containsKey("my.mixed.property"));
    String val1 = props.get("my.mixed.property");
    assertEquals("prefix::**[my.secret]**::**[my.password]**::suffix", val1);
  }


  @Test
  void testComponents() {
    HealthData data = health.getHealthData().getBody();
    assertNotNull(data);

    assertEquals(1, data.getComponents().size());

    ApplicationComponent comp = data.getComponents().get(0);
    assertEquals("Mock Piece", comp.getName());
    assertEquals(Status.IMPAIRED, comp.getStatus());

    MiscProperties detail = comp.getDetails();
    assertTrue(detail.containsKey("attr"));
    assertEquals("val", detail.get("attr"));
  }

  @Test
  void testNeverStartedStopwatch() {

    StopWatch stopWatch = new StopWatch();

    assertFalse(stopWatch.isRunning());

    long executionMillis = stopWatch.getTotalTimeMillis();

    // Verify that a StopWatch that was never started has a total time of 0
    assertEquals(0, executionMillis);

  }

  @Test
  void testStoppedStopwatch() throws InterruptedException {

    StopWatch stopWatch = new StopWatch();

    stopWatch.start();
    assertTrue(stopWatch.isRunning());

    Thread.sleep(1000);

    stopWatch.stop();
    assertFalse(stopWatch.isRunning());

    long executionMillis = stopWatch.getTotalTimeMillis();

    // Verify that you can get the execution time from a stopped StopWatch
    assertTrue(executionMillis > 0);

  }

}
