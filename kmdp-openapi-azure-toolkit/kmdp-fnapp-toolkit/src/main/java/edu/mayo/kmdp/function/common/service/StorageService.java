/*
  Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package edu.mayo.kmdp.function.common.service;

import static edu.mayo.kmdp.function.common.service.BlobStorageService.BLOB_STORAGE_CONNECTION_STRING;
import static edu.mayo.kmdp.function.common.service.BlobStorageService.BLOB_STORAGE_READ_TOKEN;

import edu.mayo.kmdp.function.monitor.MonitorableFunctionAppComponent;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Interface to be implemented by file-oriented persistence providers
 */
public interface StorageService extends MonitorableFunctionAppComponent {

  /**
   * Obfuscates the secrets contained in a connection string, token, url (fragment) or similar
   * <p>
   * Blob connection strings are actually complex sub-key/value pairs that need to be deconstructed
   * before they can be obfuscated properly
   *
   * @param key   the property name
   * @param value the property value, a complex key/value pair string in itself
   * @return the value, with secrets replaced by '*'
   */
  static String censorStoragePropString(String key, String value) {
    if (value == null) {
      return value;
    }
    String separator;
    switch (key) {
      case BLOB_STORAGE_CONNECTION_STRING:
        separator = ";";
        break;
      case BLOB_STORAGE_READ_TOKEN:
      default:
        separator = "&";
    }
    return Arrays.stream(value.split(separator))
        .map(pair -> {
          int x = pair.indexOf('=');
          if (x < 0) {
            return pair;
          }
          String k = pair.substring(0, x);
          String v = pair.substring(x + 1);
          return k + "=" + obfuscateBlobStorageProperty(k, v);
        }).collect(Collectors.joining(separator));
  }

  /**
   * Detects the sensitive part of a connection string component, and replaces it with '*'
   *
   * @param key   the sub-property name
   * @param value the sub-property value
   * @return the value, replaced by '*', except for the first 5 characters
   */
  static String obfuscateBlobStorageProperty(String key, String value) {
    var k = key.toLowerCase();
    var len = 5;
    if (k.length() < len) {
      return "*".repeat(len);
    }
    if (k.startsWith("sig") || k.contains("key") || k.contains("token")) {
      return value.substring(0, len) + "*".repeat(value.length() - len);
    } else {
      return value;
    }
  }

  /**
   * Predicate that checks whether the Storage is active and working
   *
   * @return true if connected
   */
  boolean isConnected();

  /**
   * Uses the data from 'inputStream' to create a file with name 'targetFilename' in the underlying
   * Storage.
   *
   * @param inputStream    the stream providing the data to be saved
   * @param targetFilename the name of the file that will contain the data
   * @param requestId      a random identifier of a specific request
   * @return a URL pointing to the newly generated file
   */
  String store(InputStream inputStream, String targetFilename, String requestId);

  /**
   * Looks up a file with name 'targetFilename' in the underlying Storage, and returns an
   * Inputstream to it.
   *
   * @param targetFilename the name of the file to retrieve
   * @param requestId      a random identifier of a specific request
   * @return an Inputstream to the file in the BLOB storage
   */
  InputStream retrieve(String targetFilename, String requestId);
}
