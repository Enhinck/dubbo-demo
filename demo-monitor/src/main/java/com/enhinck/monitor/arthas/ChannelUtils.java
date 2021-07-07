

package com.enhinck.monitor.arthas;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
/**
 *
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
public final class ChannelUtils {

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelUtils() {
    }
}