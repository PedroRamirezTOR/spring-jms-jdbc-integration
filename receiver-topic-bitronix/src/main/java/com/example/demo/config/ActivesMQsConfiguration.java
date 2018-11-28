package com.example.demo.config;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import bitronix.tm.resource.jms.PoolingConnectionFactory;

@Configuration
@EnableTransactionManagement
public class ActivesMQsConfiguration {
	
	@Autowired
	PlatformTransactionManager platformTransactionManager;

	@Bean(name="jmsConnectionFactoryLocal")
	public ConnectionFactory jmsConnectionFactoryLocal() {
		PoolingConnectionFactory btmPoolingConnectionFactory = new PoolingConnectionFactory();
		btmPoolingConnectionFactory.setClassName("org.apache.activemq.ActiveMQXAConnectionFactory");
		btmPoolingConnectionFactory.setUniqueName("AMQLocal");
		btmPoolingConnectionFactory.setMinPoolSize(1);
		btmPoolingConnectionFactory.setMaxPoolSize(5);
		btmPoolingConnectionFactory.setUser("admin");
		btmPoolingConnectionFactory.setPassword("admin");
		btmPoolingConnectionFactory.setAllowLocalTransactions(false);
		btmPoolingConnectionFactory.getDriverProperties().setProperty("brokerURL", "tcp://localhost:61616");
		btmPoolingConnectionFactory.init();
		return btmPoolingConnectionFactory;
	}

	@Bean(name="jmsListenerContainerFactoryLocal")
	public JmsListenerContainerFactory<?> jmsListenerContainerFactoryLocal(@Qualifier("jmsConnectionFactoryLocal") ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setTransactionManager(platformTransactionManager);
		return factory;
	}
	
	@Bean(name="jmsTemplateLocal")
	public JmsTemplate jmsTemplateLocal(@Qualifier("jmsConnectionFactoryLocal") ConnectionFactory connectionFactory) {
		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setConnectionFactory(connectionFactory);
		return jmsTemplate;
	}

	@Primary
	@Bean(name="jmsConnectionFactoryRemote")
	public ConnectionFactory jmsConnectionFactoryRemote() {
		PoolingConnectionFactory btmPoolingConnectionFactory = new PoolingConnectionFactory();
		btmPoolingConnectionFactory.setClassName("org.apache.activemq.ActiveMQXAConnectionFactory");
		btmPoolingConnectionFactory.setUniqueName("AMQRemote");
		btmPoolingConnectionFactory.setMinPoolSize(1);
		btmPoolingConnectionFactory.setMaxPoolSize(5);
		btmPoolingConnectionFactory.setUser("admin");
		btmPoolingConnectionFactory.setPassword("admin");
		btmPoolingConnectionFactory.setAllowLocalTransactions(false);
		btmPoolingConnectionFactory.getDriverProperties().setProperty("brokerURL", "tcp://10.10.10.10:61616");
		btmPoolingConnectionFactory.init();
		return btmPoolingConnectionFactory;
	}

	@Primary
	@Bean(name="jmsTemplateRemote")
	public JmsTemplate jmsTemplateRemote(@Qualifier("jmsConnectionFactoryRemote") ConnectionFactory connectionFactory) {
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		return jmsTemplate;
	}
	
	@Primary
	@Bean(name="jmsListenerContainerFactoryRemote")
	public JmsListenerContainerFactory<?> jmsListenerContainerFactoryRemote(@Qualifier("jmsConnectionFactoryRemote") ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setTransactionManager(platformTransactionManager);
		return factory;
	}

}