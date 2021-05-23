package io.github.kimmking.gateway.inbound;

import com.google.common.collect.Lists;
import io.github.kimmking.gateway.outbound.httpclient4.HttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

/**
 * @program: netty-gateway
 * @description:
 * @author: Yu Bin
 * @create: 2021-05-23 13:00
 **/
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private HttpOutboundHandler outboundHandler;

    public HttpInboundHandler() {
        this.outboundHandler = new HttpOutboundHandler(Lists.newArrayList("http://localhost:8088/api/hello"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        outboundHandler.handle(request, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
