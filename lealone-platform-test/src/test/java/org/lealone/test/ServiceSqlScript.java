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
package org.lealone.test;

import org.lealone.test.TestBase.SqlExecuter;
import org.lealone.test.service.impl.HelloWorldServiceImpl;
import org.lealone.test.service.impl.UserServiceImpl;

public class ServiceSqlScript {

    public static void main(String[] args) {
        new SqlScriptTest().runTest();
    }

    private static class SqlScriptTest extends UnitTestBase {
        @Override
        public void test() {
            createUserService(this);
            createHelloWorldService(this);
        }
    }

    private static final String SERVICE_PACKAGE_NAME = "org.lealone.test.generated.service";
    private static final String GENERATED_CODE_PATH = "./src/test/java";

    public static void createUserService(SqlExecuter executer) {
        System.out.println("create service: user_service");

        // 创建服务: user_service
        executer.execute("create service if not exists user_service (" //
                + " add(user user) long," // 第一个user是参数名，第二个user是参数类型
                + " find(name varchar) user," //
                + " update(user user) int," //
                + " delete(name varchar) int)" //
                + " package '" + SERVICE_PACKAGE_NAME + "'" //
                // 如果是内部类，不能用getClassName()，会包含$字符
                + " implement by '" + UserServiceImpl.class.getCanonicalName() + "'" //
                + " generate code '" + GENERATED_CODE_PATH + "'");
    }

    public static void createHelloWorldService(SqlExecuter executer) {
        System.out.println("create service: hello_world_service");

        // 创建服务: hello_world_service
        executer.execute("create service hello_world_service (" //
                + "             say_hello() void," //
                + "             say_goodbye_to(name varchar) varchar" //
                + "         ) package '" + SERVICE_PACKAGE_NAME + "'" //
                + "           implement by '" + HelloWorldServiceImpl.class.getName() + "'" //
                + "           generate code '" + GENERATED_CODE_PATH + "'");
    }
}
