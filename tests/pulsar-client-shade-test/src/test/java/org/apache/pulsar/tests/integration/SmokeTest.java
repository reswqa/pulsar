/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.tests.integration;

import lombok.Cleanup;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SmokeTest {

    private PulsarContainer pulsarContainer;

    @BeforeClass
    public void setup(){
        pulsarContainer = new PulsarContainer();
        pulsarContainer.start();
    }

    @Test
    public void checkClient() throws PulsarClientException {

        @Cleanup
        PulsarClient client = PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPlainTextPulsarBrokerUrl())
                .build();

        final String inputTopic = "input";

        Producer<String> producer = client.newProducer(Schema.STRING)
                .topic(inputTopic)
                .enableBatching(false)
                .create();

        Consumer<String> consumer = client.newConsumer(Schema.STRING)
                .topic(inputTopic)
                .subscriptionName("test-subs")
                .ackTimeout(10, TimeUnit.SECONDS)
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();

        producer.send("Hello!");
        Message<String> message = consumer.receive(10, TimeUnit.SECONDS);

        Assert.assertEquals(message.getValue(), "Hello!");

    }

    @AfterClass
    public void cleanup(){
        pulsarContainer.stop();
    }

}