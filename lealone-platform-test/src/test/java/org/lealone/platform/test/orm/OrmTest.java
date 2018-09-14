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
package org.lealone.platform.test.orm;

import java.util.List;

import org.lealone.platform.test.SqlScript;
import org.lealone.platform.test.generated.model.Customer;
import org.lealone.platform.test.generated.model.Order;
import org.lealone.test.UnitTestBase;

public class OrmTest extends UnitTestBase {

    public static void main(String[] args) {
        new OrmTest().runTest();
    }

    @Override
    public void test() {
        // 会自动生成Customer和Order两个模型类
        SqlScript.createCustomerTable(this);
        SqlScript.createOrderTable(this);

        // insert
        new Customer().id.set(1001).name.set("rob").phone.set(12345678).insert();

        // find
        Customer c = Customer.dao.where().id.eq(1001).findOne();

        // update
        c.notes.set("test").update();

        c = Customer.dao.where().id.eq(1001).findOne();
        assertEquals("test", c.notes.get());

        // delete
        Customer.dao.where().id.eq(1001).delete();

        // count
        int count = Customer.dao.findCount();
        assertEquals(0, count);

        // 批量增加有关联的记录
        Order o1 = new Order().orderId.set(2001).orderDate.set("2018-01-01");
        Order o2 = new Order().orderId.set(2002).orderDate.set("2018-01-01");
        Customer customer = new Customer().id.set(1002).name.set("customer1");
        customer.addOrder(o1, o2).insert();
        // 调用addOrder后，Order的customerId字段会自动对应Customer的id字段
        assertEquals(o1.customerId.get(), customer.id.get());

        // 关联查询
        c = Customer.dao;
        Order o = Order.dao;
        customer = c.join(o).on().id.eq(o.customerId).where().id.eq(1002).findOne();

        // 一个customer对应两个Order
        List<Order> orderList = customer.getOrderList();
        assertEquals(2, orderList.size());
        assertTrue(customer == orderList.get(0).getCustomer());

        // 测试事务
        try {
            Customer.dao.beginTransaction();

            new Customer().id.set(1003).name.set("rob3").insert();
            new Customer().id.set(1004).name.set("rob4").insert();

            Customer.dao.commitTransaction();
        } catch (Exception e) {
            Customer.dao.rollbackTransaction();
            e.printStackTrace();
        }
    }

}
