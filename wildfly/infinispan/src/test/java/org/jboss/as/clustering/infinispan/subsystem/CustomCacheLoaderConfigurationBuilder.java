package org.jboss.as.clustering.infinispan.subsystem;

import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

/**
 * @author Mircea Markus
 * @since 6.0
 */
public class CustomCacheLoaderConfigurationBuilder extends AbstractStoreConfigurationBuilder<CustomCacheLoaderConfiguration, CustomCacheLoaderConfigurationBuilder> {


   private String location;

   public CustomCacheLoaderConfigurationBuilder(PersistenceConfigurationBuilder builder) {
      super(builder, CustomCacheLoaderConfiguration.attributeDefinitionSet());
   }

   @Override
   public CustomCacheLoaderConfiguration create() {
      return new CustomCacheLoaderConfiguration(attributes.protect(), async.create(), location);
   }

   @Override
   public CustomCacheLoaderConfigurationBuilder self() {
      return this;
   }

   public CustomCacheLoaderConfigurationBuilder location(String some) {
      this.location = some;
      return this;
   }

}