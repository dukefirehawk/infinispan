package org.infinispan.hotrod.impl.multimap.operations;

import static org.infinispan.hotrod.impl.multimap.protocol.MultimapHotRodConstants.CONTAINS_KEY_MULTIMAP_REQUEST;
import static org.infinispan.hotrod.impl.multimap.protocol.MultimapHotRodConstants.CONTAINS_KEY_MULTIMAP_RESPONSE;

import org.infinispan.api.common.CacheOptions;
import org.infinispan.hotrod.impl.operations.OperationContext;
import org.infinispan.hotrod.impl.protocol.HotRodConstants;
import org.infinispan.hotrod.impl.transport.netty.HeaderDecoder;

import io.netty.buffer.ByteBuf;

/**
 * Implements "contains key" for multimap cache as defined by  <a href="http://community.jboss.org/wiki/HotRodProtocol">Hot
 * Rod protocol specification</a>.
 *
 * @since 14.0
 */
public class ContainsKeyMultimapOperation<K> extends AbstractMultimapKeyOperation<K, Boolean> {
   public ContainsKeyMultimapOperation(OperationContext operationContext, K key, byte[] keyBytes,
                                       CacheOptions options, boolean supportsDuplicates) {
      super(operationContext, CONTAINS_KEY_MULTIMAP_REQUEST, CONTAINS_KEY_MULTIMAP_RESPONSE, key, keyBytes, options, null, supportsDuplicates);
   }

   @Override
   public void acceptResponse(ByteBuf buf, short status, HeaderDecoder decoder) {
      if (HotRodConstants.isNotExist(status)) {
         complete(Boolean.FALSE);
      } else {
         complete(buf.readByte() == 1 ? Boolean.TRUE : Boolean.FALSE);
      }
   }
}
