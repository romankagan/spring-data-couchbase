/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.couchbase.repository.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.data.couchbase.CouchbaseTestHelper.getRepositoryWithRetry;

import com.couchbase.client.java.cluster.ClusterInfo;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.util.features.CouchbaseFeature;
import com.couchbase.client.java.util.features.Version;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
;
import org.springframework.data.couchbase.ContainerResourceRunner;
import org.springframework.data.couchbase.core.CouchbaseOperations;
import org.springframework.data.couchbase.core.UnsupportedCouchbaseFeatureException;
import org.springframework.data.couchbase.repository.User;
import org.springframework.data.couchbase.repository.UserRepository;
import org.springframework.data.couchbase.repository.config.RepositoryOperationsMapping;
import org.springframework.data.couchbase.repository.support.CouchbaseRepositoryFactory;
import org.springframework.data.couchbase.repository.support.IndexManager;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;

/**
 * An integration test that validates feature checking with Java Config.
 *
 * @author Simon Baslé
 */
@RunWith(ContainerResourceRunner.class)
@ContextConfiguration(classes = FeatureDetectionTestApplicationConfig.class)
public class FeatureDetectionRepositoryIntegrationTests {

  @Autowired
  private RepositoryOperationsMapping operationsMapping;

  @Autowired
  private IndexManager indexManager;

  @Autowired
  private ClusterInfo clusterInfo;

  @Before
  public void checkClusterInfo() {
    Assume.assumeTrue(clusterInfo.getMinVersion() == Version.NO_VERSION);
  }

  @Test
  public void testN1qlIncompatibleClusterFailsFastForN1qlBasedRepository() throws Exception {
    RepositoryFactorySupport factory = new CouchbaseRepositoryFactory(operationsMapping, indexManager);
    try {
      factory.getRepository(UserRepository.class);
      fail("expected UnsupportedCouchbaseFeatureException");
    } catch (UnsupportedCouchbaseFeatureException e) {
      assertThat(e.getFeature()).isEqualTo(CouchbaseFeature.N1QL);
    }
  }

  @Test
  public void testN1qlIncompatibleClusterDoesntFailForViewBasedRepository() throws Exception {
    RepositoryFactorySupport factory = new CouchbaseRepositoryFactory(operationsMapping, indexManager);
    ViewOnlyUserRepository repository = getRepositoryWithRetry(factory, ViewOnlyUserRepository.class);
    assertThat(repository).isNotNull();
  }

  @Test
  public void testN1qlIncompatibleClusterTemplateFails() {
    final CouchbaseOperations template = operationsMapping.getDefault();

    N1qlQuery query = N1qlQuery.simple("SELECT * FROM `" + template.getCouchbaseBucket().name() + "`");
    try {
      template.findByN1QL(query, User.class);
      fail("expected findByN1QL to fail with UnsupportedCouchbaseFeatureException");
    } catch (UnsupportedCouchbaseFeatureException e) {
      assertThat(e.getFeature()).isEqualTo(CouchbaseFeature.N1QL);
    }

    try {
      template.findByN1QLProjection(query, User.class);
      fail("expected findByN1QLProjection to fail with UnsupportedCouchbaseFeatureException");
    } catch (UnsupportedCouchbaseFeatureException e) {
      assertThat(e.getFeature()).isEqualTo(CouchbaseFeature.N1QL);
    }

    try {
      template.queryN1QL(query);
      fail("expected queryN1QL to fail with UnsupportedCouchbaseFeatureException");
    } catch (UnsupportedCouchbaseFeatureException e) {
      assertThat(e.getFeature()).isEqualTo(CouchbaseFeature.N1QL);
    }
  }
}
