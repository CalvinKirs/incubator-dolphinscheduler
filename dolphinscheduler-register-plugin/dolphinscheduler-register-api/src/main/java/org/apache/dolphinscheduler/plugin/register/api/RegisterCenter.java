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

package org.apache.dolphinscheduler.plugin.register.api;

import java.util.List;
import java.util.Map;

public interface RegisterCenter {

    void register(Map<String, Object> registerData) throws Exception;

    void upRegister();

    void subscribe(String key, SubscribeListener subscribeListener);

    void unsubscribe(String key, SubscribeListener subscribeListener);

    String get(String key);

    void remove(String key);

    void persist(String key, String value);

    void update(String key, String value);


    void remove();

    List<String> getChildren(String path);

    String getData(String key);


}