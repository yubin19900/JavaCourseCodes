package io.github.kimmking.gateway.outbound.httpclient4;

import io.github.kimmking.gateway.filter.HeaderHttpRequestFilter;
import io.github.kimmking.gateway.filter.HeaderHttpResponseFilter;
import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.github.kimmking.gateway.filter.HttpResponseFilter;
import io.github.kimmking.gateway.router.RandomHttpEndpointRouter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: netty-gateway
 * @description:
 * @author: Yu Bin
 * @create: 2021-05-23 11:05
 **/
public class HttpOutboundHandler {
    private CloseableHttpAsyncClient httpClient;
    private ExecutorService executorService;
    private HttpRequestFilter requestFilter = new HeaderHttpRequestFilter();
    private HttpResponseFilter responseFilter = new HeaderHttpResponseFilter();
    private RandomHttpEndpointRouter router = new RandomHttpEndpointRouter();
    private List<String> backendUrls;

    public HttpOutboundHandler(List<String> backendUrls) {
        this.backendUrls = backendUrls;
        int threadSize = Runtime.getRuntime().availableProcessors();
        executorService = new ThreadPoolExecutor(threadSize, threadSize, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>(100), new ThreadPoolExecutor.CallerRunsPolicy());
        RequestConfig config = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).setConnectionRequestTimeout(2000).build();
        httpClient = HttpAsyncClients.custom().setDefaultRequestConfig(config).build();
        httpClient.start();
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        String backendUrl = router.route(this.backendUrls);
        final String url = backendUrl + fullRequest.uri();
        requestFilter.filter(fullRequest, ctx);
        executorService.submit(() -> httpGet(fullRequest, ctx, url));
    }

    private void httpGet(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final String url) {
        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("token", fullRequest.headers().get("token"));
        httpClient.execute(httpGet, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                handleResponse(fullRequest, ctx, httpResponse);
            }

            @Override
            public void failed(Exception e) {

            }

            @Override
            public void cancelled() {

            }
        });
    }

    private void handleResponse(final FullHttpRequest fullHttpRequest, final ChannelHandlerContext ctx, HttpResponse httpResponse) {
        HttpEntity entity = httpResponse.getEntity();
        FullHttpResponse response = null;
        try {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK, Unpooled.wrappedBuffer(EntityUtils.toByteArray(entity)));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", Integer.parseInt(httpResponse.getFirstHeader("Content-Length").getValue()));
            responseFilter.filter(response);
            ctx.write(response);
            ctx.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
