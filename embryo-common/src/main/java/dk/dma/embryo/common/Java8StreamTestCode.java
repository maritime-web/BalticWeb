/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class Java8StreamTestCode {

    public static void testing() {

        List<TestOrderTest> orderBook = new ArrayList<>();
        TestOrderTest buyGoogle = new TestOrderTest("GOOG.NS", 300, 900.30, TestOrderTest.Side.BUY);
        TestOrderTest sellGoogle = new TestOrderTest("GOOG.NS", 600, 890.30, TestOrderTest.Side.SELL);
        TestOrderTest buyApple = new TestOrderTest("APPL.NS", 400, 552, TestOrderTest.Side.BUY);
        TestOrderTest sellApple = new TestOrderTest("APPL.NS", 200, 550, TestOrderTest.Side.SELL);
        TestOrderTest buyGS = new TestOrderTest("GS.NS", 300, 130, TestOrderTest.Side.BUY);
        orderBook.add(buyGoogle);
        orderBook.add(sellGoogle);
        orderBook.add(buyApple);
        orderBook.add(sellApple);
        orderBook.add(buyGS);

        Stream<TestOrderTest> stream = orderBook.stream();

        Stream buyOrders = stream.filter((TestOrderTest o) -> o.side().equals(TestOrderTest.Side.BUY));
        System.out.println("No of Buy Order Placed :" + buyOrders.count());

        Stream<TestOrderTest> sellOrders = orderBook.stream().filter((TestOrderTest o) -> o.side() == TestOrderTest.Side.SELL);
        System.out.println("No of Sell Order Placed : " + sellOrders.count());

        double value = orderBook.stream().mapToDouble((TestOrderTest o) -> o.price()).sum();
        System.out.println("Total value of all orders : " + value);

        long quantity = orderBook.stream().mapToLong((TestOrderTest o) -> o.quantity()).sum();
        System.out.println("Total quantity of all orders : " + quantity);
    }
    
    public static class TestOrderTest {
        enum Side {
            BUY, SELL;
        }

        private final String symbol;
        private final int quantity;
        private double price; 
        private final Side side;

        public TestOrderTest(String symbol, int quantity, double price, Side side) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.price = price;
            this.side = side;
        }

        public double price() {
            return price;
        }

        public void price(double price) {
            this.price = price;
        }

        public String symbol() {
            return symbol;
        }

        public int quantity() {
            return quantity;
        }

        public Side side() {
            return side;
        }
    }
}
