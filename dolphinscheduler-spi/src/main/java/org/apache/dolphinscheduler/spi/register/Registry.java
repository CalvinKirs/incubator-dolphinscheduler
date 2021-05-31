package org.apache.dolphinscheduler.spi.register;/*
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

import java.util.List;
import java.util.Map;

public interface Registry {

    void init(Map<String, String> registerData);

    void close();

    void subscribe(String path, SubscribeListener subscribeListener);

    void unsubscribe(String path);

    String get(String key);

    void remove(String key);

    void persist(String key, String value);

    void update(String key, String value);

    List<String> getChildren(String path);

    String getData(String key);

    boolean isExisted(String key);

    boolean delete(String key) throws Exception;

    boolean acquireLock(String key);

    boolean releaseLock(String key);
}