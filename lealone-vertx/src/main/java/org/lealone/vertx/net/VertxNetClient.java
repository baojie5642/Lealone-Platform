/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lealone.vertx.net;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.lealone.common.exceptions.DbException;
import org.lealone.net.AsyncConnection;
import org.lealone.net.AsyncConnectionManager;
import org.lealone.net.NetEndpoint;
import org.lealone.net.TcpConnection;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

public class VertxNetClient implements org.lealone.net.NetClient {

    // 使用InetSocketAddress为key而不是字符串，是因为像localhost和127.0.0.1这两种不同格式实际都是同一个意思，
    // 如果用字符串，就会产生两条AsyncConnection，这是没必要的。
    private static final ConcurrentHashMap<InetSocketAddress, AsyncConnection> asyncConnections = new ConcurrentHashMap<>();

    private static Vertx vertx;
    private static NetClient client;

    private static void openClient(Properties prop) {
        synchronized (VertxNetClient.class) {
            if (client == null) {
                vertx = VertxNetUtils.getVertx(prop);
                NetClientOptions options = VertxNetUtils.getNetClientOptions(prop);
                options.setConnectTimeout(10000);
                client = vertx.createNetClient(options);
            }
        }
    }

    private static void closeClient() {
        synchronized (VertxNetClient.class) {
            if (client != null) {
                client.close();
                VertxNetUtils.closeVertx(vertx); // 不要像这样单独调用: vertx.close();
                client = null;
                vertx = null;
            }
        }
    }

    @Override
    public AsyncConnection createConnection(Properties prop, NetEndpoint endpoint) {
        return createConnection(prop, endpoint, null);
    }

    @Override
    public AsyncConnection createConnection(Properties prop, NetEndpoint endpoint,
            AsyncConnectionManager connectionManager) {
        if (client == null) {
            openClient(prop);
        }
        InetSocketAddress inetSocketAddress = endpoint.getInetSocketAddress();
        AsyncConnection asyncConnection = asyncConnections.get(inetSocketAddress);
        if (asyncConnection == null) {
            synchronized (VertxNetClient.class) {
                asyncConnection = asyncConnections.get(inetSocketAddress);
                if (asyncConnection == null) {
                    CountDownLatch latch = new CountDownLatch(1);
                    client.connect(endpoint.getPort(), endpoint.getHost(), res -> {
                        try {
                            if (res.succeeded()) {
                                NetSocket socket = res.result();
                                VertxWritableChannel channel = new VertxWritableChannel(socket);
                                AsyncConnection conn;
                                if (connectionManager != null) {
                                    conn = connectionManager.createConnection(channel, false);
                                } else {
                                    conn = new TcpConnection(channel, this);
                                }
                                conn.setHostAndPort(endpoint.getHostAndPort());
                                conn.setInetSocketAddress(inetSocketAddress);
                                asyncConnections.put(inetSocketAddress, conn);
                                socket.handler(buffer -> {
                                    conn.handle(new VertxBuffer(buffer));
                                });
                            } else {
                                throw DbException.convert(res.cause());
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                        asyncConnection = asyncConnections.get(inetSocketAddress);
                    } catch (InterruptedException e) {
                        throw DbException.convert(e);
                    }
                }
            }
        }
        return asyncConnection;
    }

    @Override
    public void removeConnection(InetSocketAddress inetSocketAddress, boolean closeClient) {
        asyncConnections.remove(inetSocketAddress);
        if (closeClient && asyncConnections.isEmpty()) {
            closeClient();
        }
    }
}
