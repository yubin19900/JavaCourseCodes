package io.github.kimmking.gateway.inbound;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Map;

/**
 * @program: netty-gateway
 * @description:
 * @author: Yu Bin
 * @create: 2021-05-23 09:51
 **/
public class HttpRequestServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        if (fullHttpRequest.uri().contains("/test")) {
            handlerTest(fullHttpRequest, ctx);
        }
    }

    private void handlerTest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        Map<String, Object> result = Maps.newHashMapWithExpectedSize(2);
        result.put("code", 0);
        result.put("msg", "success");
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(JSON.toJSONString(result), CharsetUtil.UTF_8));
        response.headers().set("Content-Type", "application/json");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
