package org.infinispan.multimap.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.infinispan.functional.FunctionalTestUtils.await;

import java.util.Map;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "multimap.impl.EmbeddedMultimapPairCacheTest")
public class EmbeddedMultimapPairCacheTest extends SingleCacheManagerTest {

   EmbeddedMultimapPairCache<String, String, String> embeddedPairCache;

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      EmbeddedCacheManager cm = TestCacheManagerFactory.createCacheManager(MultimapSCI.INSTANCE);
      ConfigurationBuilder builder = new ConfigurationBuilder();
      cm.createCache("test", builder.build());
      embeddedPairCache = new EmbeddedMultimapPairCache<>(cm.getCache("test"));
      return cm;
   }

   public void testSetGetOperations() {
      assertThat(await(embeddedPairCache.set("person1", Map.entry("name", "Oihana"))))
            .isEqualTo(1);
      assertThat(await(embeddedPairCache.set("person1", Map.entry("age", "1"), Map.entry("birthday", "2023-05-26"))))
            .isEqualTo(2);

      assertThat(await(embeddedPairCache.set("person1", Map.entry("name", "Ramon"))))
            .isEqualTo(0);

      Map<String, String> person1 = await(embeddedPairCache.get("person1"));
      assertThat(person1)
            .containsEntry("name", "Ramon")
            .containsEntry("age", "1")
            .containsEntry("birthday", "2023-05-26")
            .hasSize(3);
   }
}