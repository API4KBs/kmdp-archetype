package edu.mayo.kmdp.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class VersionEndPoint implements VersionApiDelegate {

  @Autowired
  protected BuildProperties buildProperties;

  @Override
  public ResponseEntity<String> getVersionData() {
    try {
      return ResponseEntity.ok(buildProperties.getVersion());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
