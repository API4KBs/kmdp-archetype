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

import static edu.mayo.kmdp.util.EnvironmentUtils.getRequiredProperty;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import edu.mayo.kmdp.health.MonitorablePropertyDef;
import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.util.EnvironmentUtils;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service that abstracts Azure's Blog Storage infrastructure to provide easy upload
 * capabilities.
 * <p>
 */
public class BlobStorageService implements StorageService {

  private static final Logger logger = LoggerFactory.getLogger(BlobStorageService.class);
  public static final String BLOB_STORAGE_CONTAINER_NAME = "blob_storage_container_name";
  public static final String BLOB_STORAGE_CONNECTION_STRING = "blob_storage_connection_string";
  public static final String BLOB_STORAGE_READ_TOKEN = "blob_storage_url_read_token";

  protected BlobContainerClient containerClient;

  public BlobStorageService() {
    try {
      var blobServiceClient = getBlobServiceClient();
      containerClient = getContainerClient(blobServiceClient);
    } catch (Exception e) {
      // fail later - result in DOWN state if health is checked
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * Connects to the {@link BlobServiceClient}, using the BLOB_STORAGE_CONNECTION_STRING property
   * configured in the Environment
   *
   * @return a {@link BlobServiceClient}
   */
  protected BlobServiceClient getBlobServiceClient() {
    String blogStorageConnectionString = getRequiredProperty(BLOB_STORAGE_CONNECTION_STRING);
    return new BlobServiceClientBuilder()
        .connectionString(blogStorageConnectionString)
        .buildClient();
  }

  /**
   * Connects to a specific named BLOB Container, as specified by the BLOB_STORAGE_CONTAINER_NAME
   * property configured in the Environment
   *
   * @return a {@link BlobContainerClient}
   */
  protected BlobContainerClient getContainerClient(BlobServiceClient blobServiceClient) {
    String containerName = EnvironmentUtils.getRequiredProperty(BLOB_STORAGE_CONTAINER_NAME);
    return blobServiceClient.getBlobContainerClient(containerName);
  }

  /**
   * Predicate that checks whether the Blob Storage Container connection is working
   *
   * @return true if the container client exists
   */
  public boolean isConnected() {
    return containerClient != null && containerClient.exists();
  }

  /**
   * Uses the data from 'inputStream' to create a file with name 'targetFilename' in the Blob
   * Storage. Aborts if the Store is not connected
   *
   * @param inputStream    the stream providing the data to be saved
   * @param targetFilename the name of the file that will contain the data
   * @param requestId      a random identifier of a specific request
   * @return a URL pointing to the newly generated file
   */
  public String store(InputStream inputStream, String targetFilename, String requestId) {
    checkConnected(requestId);

    try {
      var blobClient = containerClient.getBlobClient(targetFilename);
      blobClient.upload(BinaryData.fromStream(inputStream));
      return toUrl(blobClient);
    } catch (Exception e) {
      String message = String.format("Unable to upload for request '%s': %s", requestId,
          e.getMessage());

      throw new ServerSideException(ResponseCodeSeries.InternalServerError, message);
    }

  }

  @Override
  @SuppressWarnings("java:S5411")
  public InputStream retrieve(String targetFilename, String requestId) {
    checkConnected(requestId);

    try {
      var blobClient = containerClient.getBlobClient(targetFilename);
      if (!blobClient.exists()) {
        throw new FileNotFoundException("Unable to find " + targetFilename);
      }
      return blobClient.downloadContent().toStream();
    } catch (Exception e) {
      String message = String.format("Unable to retrieve for request '%s': %s", requestId,
          e.getMessage());

      throw new ServerSideException(ResponseCodeSeries.NotFound, message);
    }
  }

  /**
   * Checks connection to the Storage before attempting any I/O operation
   *
   * @param requestId the request Id
   * @throws ServerSideException if not connected
   */
  private void checkConnected(String requestId) {
    if (!isConnected()) {
      throw new ServerSideException(ResponseCodeSeries.ServiceUnavailable,
          "Unable to connect to BLOB storage for request " + requestId);
    }
  }


  /**
   * Builds a URL that provides direct access to a resource stored in the BLOB container
   *
   * @param blobClient the reference to the resource
   * @return a URL with an optional security access token for direct access
   */
  private String toUrl(BlobClient blobClient) {
    var baseUrl = new StringBuilder(blobClient.getBlobUrl());

    var blobToken = EnvironmentUtils.getProperty(BLOB_STORAGE_READ_TOKEN);
    blobToken.ifPresent(tk -> baseUrl.append("?").append(tk));
    return baseUrl.toString();
  }

  // ***************************************************************************************** //


  /**
   * Defines the Environment Properties to be reported through state/health monitoring
   *
   * @return a Set of pertinent MonitorablePropertyDef
   */
  @Override
  public Set<MonitorablePropertyDef> getComponentPropertiesDefinition() {
    return Set.of(
        new MonitorablePropertyDef(BLOB_STORAGE_CONNECTION_STRING,
            StorageService::censorStoragePropString),
        new MonitorablePropertyDef(BLOB_STORAGE_CONTAINER_NAME),
        new MonitorablePropertyDef(BLOB_STORAGE_READ_TOKEN,
            StorageService::censorStoragePropString)
    );
  }

  /**
   * A descriptor of the health state of this Storage, as an application component
   *
   * @return a {@link ApplicationComponent} used in a /health endpoint
   */
  @Override
  public ApplicationComponent getComponentStatus() {
    var blob = new ApplicationComponent();
    blob.setName("Azure Blob Storage");

    var propDefs = getComponentPropertiesDefinition();
    blob.setDetails(MonitorablePropertyDef.resolveProperties(propDefs));

    if (isConnected()) {
      blob.setStatus(Status.UP);
      blob.setStatusMessage("OK");
    } else {
      blob.setStatus(Status.DOWN);
      blob.setStatusMessage("Storage Unavailable");
    }

    return blob;
  }


}
