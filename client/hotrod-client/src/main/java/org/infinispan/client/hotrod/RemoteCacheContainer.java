package org.infinispan.client.hotrod;

import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.commons.marshall.Marshaller;

import jakarta.transaction.TransactionManager;

public interface RemoteCacheContainer extends BasicCacheContainer {

   /**
    * @see BasicCacheContainer#getCache()
    */
   @Override
   <K, V> RemoteCache<K, V> getCache();

   /**
    * @see BasicCacheContainer#getCache(String)
    */
   @Override
   <K, V> RemoteCache<K, V> getCache(String cacheName);

   /**
    * Retrieves the configuration currently in use. The configuration object
    * is immutable. If you wish to change configuration, you should use the
    * following pattern:
    *
    * <pre><code>
    * ConfigurationBuilder builder = new ConfigurationBuilder();
    * builder.read(remoteCacheManager.getConfiguration());
    * // modify builder
    * remoteCacheManager.stop();
    * remoteCacheManager = new RemoteCacheManager(builder.build());
    * </code></pre>
    *
    * @return The configuration of this RemoteCacheManager
    */
   Configuration getConfiguration();

   /**
    * Same as {@code getCache(cacheName, forceReturnValue, null, null)}
    *
    * @see #getCache(String, boolean, TransactionMode, TransactionManager)
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   default <K, V> RemoteCache<K, V> getCache(String cacheName, boolean forceReturnValue) {
      return getCache(cacheName, forceReturnValue, null, null);
   }

   /**
    * Same as {@code getCache("", forceReturnValue, null, null)}
    *
    * @see #getCache(String, boolean, TransactionMode, TransactionManager)
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   default <K, V> RemoteCache<K, V> getCache(boolean forceReturnValue) {
      return getCache("", forceReturnValue, null, null);
   }

   /**
    * Same as {@code getCache(cacheName, transactionMode, null)}
    *
    * @see #getCache(String, TransactionMode, TransactionManager)
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   default <K, V> RemoteCache<K, V> getCache(String cacheName, TransactionMode transactionMode) {
      return getCache(cacheName, transactionMode, null);
   }

   /**
    * Same as {@code getCache(cacheName, forceReturnValue, transactionMode, null)}
    *
    * @see #getCache(String, boolean, TransactionMode, TransactionManager)
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   default <K, V> RemoteCache<K, V> getCache(String cacheName, boolean forceReturnValue,
         TransactionMode transactionMode) {
      return getCache(cacheName, forceReturnValue, transactionMode, null);
   }

   /**
    * Same as {@code getCache(cacheName, null, transactionManager)}
    *
    * @see #getCache(String, TransactionMode, TransactionManager)
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   default <K, V> RemoteCache<K, V> getCache(String cacheName, TransactionManager transactionManager) {
      return getCache(cacheName, null, transactionManager);
   }

   /**
    * Same as {@code getCache(cacheName, forceReturnValue, null, transactionManager)}
    *
    * @see #getCache(String, boolean, TransactionMode, TransactionManager)
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   default <K, V> RemoteCache<K, V> getCache(String cacheName, boolean forceReturnValue,
         TransactionManager transactionManager) {
      return getCache(cacheName, forceReturnValue, null, transactionManager);
   }

   /**
    *
    * @param cacheName The cache's name.
    * @param transactionMode The {@link TransactionMode} to override. If {@code null}, it uses the configured value.
    * @param transactionManager The {@link TransactionManager} to override. If {@code null}, it uses the configured value.
    * @return the {@link RemoteCache} implementation.
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   <K, V> RemoteCache<K, V> getCache(String cacheName, TransactionMode transactionMode,
         TransactionManager transactionManager);

   /**
    * @param cacheName          The cache's name.
    * @param forceReturnValue   {@code true} to force a return value when it is not needed.
    * @param transactionMode    The {@link TransactionMode} to override. If {@code null}, it uses the configured value.
    * @param transactionManager The {@link TransactionManager} to override. If {@code null}, it uses the configured
    *                           value.
    * @return the {@link RemoteCache} implementation.
    * @deprecated since 11.0. Use {@link org.infinispan.client.hotrod.configuration.ConfigurationBuilder#remoteCache(String)} to configure the cache and then {@link #getCache(String)} to obtain it.
    */
   @Deprecated
   <K, V> RemoteCache<K, V> getCache(String cacheName, boolean forceReturnValue, TransactionMode transactionMode,
         TransactionManager transactionManager);

   boolean isStarted();

   /**
    * Switch remote cache manager to a different cluster, previously
    * declared via configuration. If the switch was completed successfully,
    * this method returns {@code true}, otherwise it returns {@code false}.
    *
    * @param clusterName name of the cluster to which to switch to
    * @return {@code true} if the cluster was switched, {@code false} otherwise
    */
   boolean switchToCluster(String clusterName);

   /**
    * Switch remote cache manager to a the default cluster, previously
    * declared via configuration. If the switch was completed successfully,
    * this method returns {@code true}, otherwise it returns {@code false}.
    *
    * @return {@code true} if the cluster was switched, {@code false} otherwise
    */
   boolean switchToDefaultCluster();

   /**
    * Returns the name of the currently active cluster.
    *
    * @return the name of the active cluster
    */
   String getCurrentClusterName();

   Marshaller getMarshaller();

   /**
    * @return {@code true} if the cache with name {@code cacheName} can participate in transactions.
    */
   boolean isTransactional(String cacheName);
}
