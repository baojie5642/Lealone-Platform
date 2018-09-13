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

public class OrmSqlScript {

    public static void main(String[] args) {
        new SqlScriptTest().runTest();
    }

    private static class SqlScriptTest extends UnitTestBase {
        @Override
        public void test() {
            createCustomerTable(this);
            createCustomerAddressTable(this);
            createUserTable(this);
            createProductTable(this);
            createOrderTable(this);
            createOrderItemTable(this);
            createAllModelPropertyTable(this);
        }
    }

    private static final String MODEL_PACKAGE_NAME = "org.lealone.test.generated.model";
    private static final String GENERATED_CODE_PATH = "./src/test/java";

    public static void createUserTable(SqlExecuter executer) {
        System.out.println("create table: user");

        // 创建表: user
        executer.execute("create table user(name char(10) primary key, notes varchar, phone int, id long)" //
                + " package '" + MODEL_PACKAGE_NAME + "'" //
                + " generate code '" + GENERATED_CODE_PATH + "'");
    }

    public static void createCustomerTable(SqlExecuter executer) {
        System.out.println("create table: customer");

        executer.execute("create table customer(id long primary key, name char(10), notes varchar, phone int)" //
                + " package '" + MODEL_PACKAGE_NAME + "'" //
                + " generate code '" + GENERATED_CODE_PATH + "'" // 生成领域模型类和查询器类的代码
        );
    }

    public static void createCustomerAddressTable(SqlExecuter executer) {
        System.out.println("create table: customer_address");

        executer.execute("create table customer_address(customer_id long, city varchar, street varchar, "
                + " FOREIGN KEY(customer_id) REFERENCES customer(id))" //
                + " package '" + MODEL_PACKAGE_NAME + "'" //
                + " generate code '" + GENERATED_CODE_PATH + "'" //
        );
    }

    public static void createProductTable(SqlExecuter executer) {
        System.out.println("create table: product");

        executer.execute("create table product(product_id long primary key, product_name varchar, "
                + " category varchar, unit_price double)" //
                + " package '" + MODEL_PACKAGE_NAME + "'" //
                + " generate code '" + GENERATED_CODE_PATH + "'" // 生成领域模型类和查询器类的代码
        );
    }

    public static void createOrderTable(SqlExecuter executer) {
        System.out.println("create table: order");

        // order是关键字，索引要用特殊方式表式
        executer.execute(
                "create table `order`(customer_id long, order_id int primary key, order_date date, total double,"
                        + " FOREIGN KEY(customer_id) REFERENCES customer(id))" //
                        + " package '" + MODEL_PACKAGE_NAME + "'" //
                        + " generate code '" + GENERATED_CODE_PATH + "'" // 生成领域模型类和查询器类的代码
        );
    }

    public static void createOrderItemTable(SqlExecuter executer) {
        System.out.println("create table: order_item");

        // order是关键字，索引要用特殊方式表式
        executer.execute("create table order_item(order_id int, product_id long, product_count int, "
                + " FOREIGN KEY(order_id) REFERENCES `order`(order_id)," //
                + " FOREIGN KEY(product_id) REFERENCES product(product_id))" //
                + " package '" + MODEL_PACKAGE_NAME + "'" //
                + " generate code '" + GENERATED_CODE_PATH + "'" // 生成领域模型类和查询器类的代码
        );
    }

    public static void createAllModelPropertyTable(SqlExecuter executer) {
        // 21种模型属性类型，目前不支持GEOMETRY类型
        // INT
        // BOOLEAN
        // TINYINT
        // SMALLINT
        // BIGINT
        // IDENTITY
        // DECIMAL
        // DOUBLE
        // REAL
        // TIME
        // DATE
        // TIMESTAMP
        // BINARY
        // OTHER
        // VARCHAR
        // VARCHAR_IGNORECASE
        // CHAR
        // BLOB
        // CLOB
        // UUID
        // ARRAY
        executer.execute("CREATE TABLE all_model_property (" //
                + " f1  INT," //
                + " f2  BOOLEAN," //
                + " f3  TINYINT," //
                + " f4  SMALLINT," //
                + " f5  BIGINT," //
                + " f6  IDENTITY," //
                + " f7  DECIMAL," //
                + " f8  DOUBLE," //
                + " f9  REAL," //
                + " f10 TIME," //
                + " f11 DATE," //
                + " f12 TIMESTAMP," //
                + " f13 BINARY," //
                + " f14 OTHER," //
                + " f15 VARCHAR," //
                + " f16 VARCHAR_IGNORECASE," //
                + " f17 CHAR," //
                + " f18 BLOB," //
                + " f19 CLOB," //
                + " f20 UUID," //
                + " f21 ARRAY" //
                + ")" //
                + " PACKAGE '" + MODEL_PACKAGE_NAME + "'" //
                + " GENERATE CODE '" + GENERATED_CODE_PATH + "'");

        System.out.println("create table: all_model_property");
    }
}
