/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.rpc.remote;

import org.apache.dolphinscheduler.rpc.client.ConsumerConfig;
import org.apache.dolphinscheduler.rpc.client.ConsumerConfigCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestCache;
import org.apache.dolphinscheduler.rpc.client.RpcRequestTable;
import org.apache.dolphinscheduler.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.rpc.common.RpcResponse;
import org.apache.dolphinscheduler.rpc.common.ThreadPoolManager;
import org.apache.dolphinscheduler.rpc.future.RpcFuture;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.FastThreadLocalThread;

/**
 * NettyClientHandler
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {


    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private final ThreadPoolManager threadPoolManager = ThreadPoolManager.INSTANCE;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcResponse rsp = (RpcResponse) msg;
        RpcRequestCache rpcRequest = RpcRequestTable.get(rsp.getRequestId());

        if (null == rpcRequest) {
            logger.warn("rpc read error,this request does not exist");
            return;
        }
        threadPoolManager.addExecuteTask(() -> readHandler(rsp, rpcRequest));
    }

    private void readHandler(RpcResponse rsp, RpcRequestCache rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
        ConsumerConfig consumerConfig = ConsumerConfigCache.getConfigByServersName(serviceName);
        if (!consumerConfig.getAsync()) {
            RpcFuture future = rpcRequest.getRpcFuture();
            RpcRequestTable.remove(rsp.getRequestId());
            future.done(rsp);
            return;

        }
        //async
        new FastThreadLocalThread(() -> {
            try {
                if (rsp.getStatus() == 0) {
                    try {
                        consumerConfig.getServiceCallBackClass().getDeclaredConstructor().newInstance().run(rsp.getResult());
                    } catch (InvocationTargetException | NoSuchMethodException e) {
                        logger.error("rpc call back error, serviceName {} ", serviceName, e);
                    }

                } else {
                    logger.error("rpc response error ,serviceName {}", serviceName);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("execute async error,serviceName {}", serviceName, e);
            }
        }).start();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            RpcRequest request = new RpcRequest();
            request.setEventType((byte) 0);
            ctx.channel().writeAndFlush(request);
            logger.debug("send heart beat msg...");

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }

}