/*
 * Copyright 2014-2018 the original author or authors.
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

package com.example.sbaserverdemo.notifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Notifier submitting events to Slack.
 *
 * @author Artur Dobosiewicz
 */
public class FlyBookNotifier extends AbstractStatusChangeNotifier {

    private static final String DEFAULT_MESSAGE = "#{instance.registration.name} (#{instance.id}) 状态发生转变 #{lastStatus} ➡️ #{instance.statusInfo.status} " +
            "\n" +
            "\n 实例详情：#{instanceEndpoint}";

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private RestTemplate restTemplate;

    private URI webhookUrl;

    private Expression message;

    public FlyBookNotifier(InstanceRepository repository, RestTemplate restTemplate) {
        super(repository);
        this.restTemplate = restTemplate;
        this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify( InstanceEvent event,  Instance instance) {
        if (webhookUrl == null) {
            return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
        }
        return Mono
                .fromRunnable(() -> restTemplate.postForEntity(webhookUrl, createMessage(event, instance), Void.class));
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected Object createMessage(InstanceEvent event, Instance instance) {
        Map<String, Object> messageJson = new HashMap<>();
        messageJson.put("title", "👹警告&👼提醒");
        messageJson.put("text", getText(event, instance));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(messageJson, headers);
    }


    protected String getText(InstanceEvent event, Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("instance", instance);
        root.put("instanceEndpoint", instance.getEndpoints().toString());
        root.put("lastStatus", getLastStatus(event.getInstance()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());
        return message.getValue(context, String.class);
    }


    public URI getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(URI webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getMessage() {
        return message.getExpressionString();
    }

    public void setMessage(String message) {
        this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
    }

}
