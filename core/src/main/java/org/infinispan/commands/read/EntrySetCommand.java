package org.infinispan.commands.read;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

import org.infinispan.Cache;
import org.infinispan.CacheSet;
import org.infinispan.CacheStream;
import org.infinispan.cache.impl.AbstractDelegatingCache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableSpliterator;
import org.infinispan.commons.util.Closeables;
import org.infinispan.commons.util.EnumUtil;
import org.infinispan.commons.util.IteratorMapper;
import org.infinispan.commons.util.RemovableIterator;
import org.infinispan.container.DataContainer;
import org.infinispan.container.impl.SegmentedDataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.context.InvocationContext;
import org.infinispan.context.impl.FlagBitSets;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.stream.impl.local.EntryStreamSupplier;
import org.infinispan.stream.impl.local.LocalCacheStream;
import org.infinispan.stream.impl.local.SegmentedEntryStreamSupplier;
import org.infinispan.util.DataContainerRemoveIterator;
import org.infinispan.util.EntryWrapper;

/**
 * Command implementation for {@link java.util.Map#entrySet()} functionality.
 *
 * @author Galder Zamarreño
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author William Burns
 * @since 4.0
 */
public class EntrySetCommand<K, V> extends AbstractLocalCommand implements VisitableCommand {
   private final Cache<K, V> cache;
   private final KeyPartitioner keyPartitioner;

   public EntrySetCommand(Cache<K, V> cache, KeyPartitioner keyPartitioner, long flagsBitSet) {
      setFlagsBitSet(flagsBitSet);
      cache = AbstractDelegatingCache.unwrapCache(cache);
      if (flagsBitSet != EnumUtil.EMPTY_BIT_SET) {
         this.cache = cache.getAdvancedCache().withFlags(EnumUtil.enumArrayOf(flagsBitSet, Flag.class));
      } else {
         this.cache = cache;
      }
      this.keyPartitioner = keyPartitioner;
   }

   @Override
   public Object acceptVisitor(InvocationContext ctx, Visitor visitor) throws Throwable {
      return visitor.visitEntrySetCommand(ctx, this);
   }

   @Override
   public LoadType loadType() {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<CacheEntry<K, V>> perform(InvocationContext ctx) throws Throwable {
      // We have to check for the flags when we do perform - as interceptor could change this while going down
      // the stack
      boolean isRemoteIteration = EnumUtil.containsAny(getFlagsBitSet(), FlagBitSets.REMOTE_ITERATION);
      Object lockOwner = ctx.getLockOwner();
      if (ctx.getLockOwner() != null) {
         return new BackingEntrySet<>(cache.getAdvancedCache().lockAs(lockOwner), keyPartitioner, isRemoteIteration);
      }
      return new BackingEntrySet<>(cache, keyPartitioner, isRemoteIteration);
   }

   @Override
   public String toString() {
      return "EntrySetCommand{" +
            "cache=" + cache.getName() +
            '}';
   }

   static class BackingEntrySet<K, V> extends AbstractCollection<CacheEntry<K, V>> implements CacheSet<CacheEntry<K, V>> {
      private final boolean isRemoteIteration;
      private final Cache<K, V> cache;
      private final KeyPartitioner keyPartitioner;

      BackingEntrySet(Cache<K, V> cache, KeyPartitioner keyPartitioner, boolean isRemoteIteration) {
         this.cache = cache;
         this.keyPartitioner = keyPartitioner;
         this.isRemoteIteration = isRemoteIteration;
      }

      @Override
      public CloseableIterator<CacheEntry<K, V>> iterator() {
         if (isRemoteIteration) {
            // If remote don't do all this wrapping
            return Closeables.iterator(cache.getAdvancedCache().getDataContainer().iterator());
         }
         Iterator<CacheEntry<K, V>> iterator = new DataContainerRemoveIterator<>(cache);
         RemovableIterator<CacheEntry<K, V>> removableIterator = new RemovableIterator<>(iterator, e -> cache.remove(e.getKey(), e.getValue()));
         return new IteratorMapper<>(removableIterator, e -> new EntryWrapper<>(cache, e));
      }

      static <K, V> CloseableSpliterator<CacheEntry<K, V>> closeableCast(Spliterator spliterator) {
         if (spliterator instanceof CloseableSpliterator) {
            return (CloseableSpliterator<CacheEntry<K, V>>) spliterator;
         }
         return Closeables.spliterator((Spliterator<CacheEntry<K, V>>) spliterator);
      }

      @Override
      public CloseableSpliterator<CacheEntry<K, V>> spliterator() {
         DataContainer<K, V> dc = cache.getAdvancedCache().getDataContainer();

         // Spliterator doesn't support remove so just return it without wrapping
         return closeableCast(dc.spliterator());
      }

      @Override
      public int size() {
         return cache.getAdvancedCache().getDataContainer().size();
      }

      @Override
      public boolean contains(Object o) {
         Map.Entry entry = toEntry(o);
         if (entry != null) {
            DataContainer<K, V> container = cache.getAdvancedCache().getDataContainer();
            InternalCacheEntry<K, V> value = container.get(entry.getKey());
            return value != null && value.getValue().equals(entry.getValue());
         }
         return false;
      }

      @Override
      public boolean remove(Object o) {
         Map.Entry entry = toEntry(o);
         // Remove must be done by the cache
         return entry != null && cache.remove(entry.getKey(), entry.getValue());
      }

      private Map.Entry<K, V> toEntry(Object obj) {
         if (obj instanceof Map.Entry) {
            return (Map.Entry) obj;
         } else {
            return null;
         }
      }

      private CacheStream<CacheEntry<K, V>> doStream(boolean parallel) {
         DataContainer<K, V> dc = cache.getAdvancedCache().getDataContainer();
         if (dc instanceof SegmentedDataContainer) {
            SegmentedDataContainer<K, V> segmentedDataContainer = (SegmentedDataContainer) dc;
            return new LocalCacheStream<>(new SegmentedEntryStreamSupplier<>(cache, keyPartitioner,
                  segmentedDataContainer), parallel, cache.getAdvancedCache().getComponentRegistry());
         } else {
            return new LocalCacheStream<>(new EntryStreamSupplier<>(cache, keyPartitioner,
                  super::stream), parallel, cache.getAdvancedCache().getComponentRegistry());
         }
      }

      @Override
      public CacheStream<CacheEntry<K, V>> stream() {
         return doStream(false);
      }

      @Override
      public CacheStream<CacheEntry<K, V>> parallelStream() {
         return doStream(true);
      }
   }
}
