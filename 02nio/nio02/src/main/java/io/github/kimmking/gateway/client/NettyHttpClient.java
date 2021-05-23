package io.github.kimmking.gateway.client;

/**
 * @program: netty-gateway
 * @description:
 * @author: Yu Bin
 * @create: 2021-05-23 09:35
 **/

import io.github.kimmking.gateway.outbound.netty4.HttpClientInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

@Data
@Slf4j
public class NettyHttpClient {
    private String url;
    private String body;

    public NettyHttpClient(String url, String body) {
        this.url = url;
        this.body = body;
    }

    public void start() {
        Bootstrap bootstrap = new Bootstrap();
        //创建从线程组，处理主线程组分配下来的io操作
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
                        pipeline.addLast(new HttpClientInboundHandler());
                    }
                });
        try {
            URI url = new URI(this.url);
            String host = url.getHost();
            int port = url.getPort();
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    url.toASCIIString(), Unpooled.wrappedBuffer(this.body.getBytes("UTF-8")));
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
            channelFuture.channel().write(request);
            channelFuture.channel().flush();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException | URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        NettyHttpClient nettyHttpClient = new NettyHttpClient("http://localhost:8088/api/hello", "你好");
        nettyHttpClient.start();
    }
}
