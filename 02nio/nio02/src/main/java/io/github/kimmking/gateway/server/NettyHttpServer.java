package io.github.kimmking.gateway.server;

/**
 * @program: netty-gateway
 * @description:
 * @author: Yu Bin
 * @create: 2021-05-23 09:35
 **/

import io.github.kimmking.gateway.inbound.HttpRequestServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class NettyHttpServer {
    private int port;

    public NettyHttpServer(int port) {
        this.port = port;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        int threadSize = Runtime.getRuntime().availableProcessors();
        //创建主线程组，接收请求
        EventLoopGroup bossGroup = new NioEventLoopGroup(threadSize);
        //创建从线程组，处理主线程组分配下来的io操作
        EventLoopGroup workerGroup = new NioEventLoopGroup(threadSize * 2);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpRequestServerInitializer());
        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
