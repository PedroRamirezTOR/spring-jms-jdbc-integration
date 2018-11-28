package com.example.demo.jms;

import java.sql.Timestamp;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.TrainDao;
import com.example.demo.dao.VersionsConfDao;
import com.example.demo.domain.Train;
import com.example.demo.domain.VersionsConf;

@Service
@Transactional(rollbackFor = Exception.class)
public class ContactTransactionReceiver {

	private int mensajesConsumer1 = 0;
	private int mensajesConsumer2 = 0;

	@Autowired
	@Qualifier("versionJdbcTemplate")
	private JdbcTemplate versionJdbcTemplate;

	@Autowired
	@Qualifier("tpJdbcTemplate")
	private JdbcTemplate tpjdbcTemplate;

	@Autowired
	private VersionsConfDao versionsConfDao;

	@Autowired
	private TrainDao trainDao;

	@Autowired
	@Qualifier("jmsTemplateRemote")
	private JmsTemplate jmsTemplateRemote;
	
	private long initTime;

	@JmsListener(destination = "Consumer.consumer2.VirtualTopic.TopicPrueba", containerFactory = "jmsListenerContainerFactoryLocal")
	public void receiveMessageFromContacts2(Message message) throws RuntimeException, JMSException {
		mensajesConsumer1++;
		TextMessage txtMessage = (TextMessage) message;
		System.out.println("Segundo consumer:" + txtMessage.getText() + " recibidos:" + mensajesConsumer1);

		VersionsConf versionsconf = new VersionsConf("V" + mensajesConsumer1, "V" + mensajesConsumer1, false,
				new Timestamp(1L), 1);
		VersionsConf versionsResult = versionsConfDao.insertUpdate(versionJdbcTemplate, versionsconf);

		if (mensajesConsumer1 == 2) {
			throw new JMSException("Error 2");
		}

		Train train = new Train("101" + mensajesConsumer1, 1L, 2L, false, true, "atp");
		Train trainResult = trainDao.insertUpdate(tpjdbcTemplate, train);

		if (mensajesConsumer1 == 3) {
			throw new JMSException("Error 2");
		}
	}

	@JmsListener(destination = "QueuePrueba", containerFactory = "jmsListenerContainerFactoryLocal")
	public void receiveMessageSendMessage(Message message) throws Exception {
		mensajesConsumer2++;
		if(mensajesConsumer2==1 || mensajesConsumer2==2) {
			initTime = System.currentTimeMillis();
		}
		TextMessage txtMessage = (TextMessage) message;
		System.out.println(txtMessage.getText());

		if (mensajesConsumer2 == 2) {
			throw new JMSException("Error 1");
		}
		
		VersionsConf versionsconf = new VersionsConf("V" + mensajesConsumer2, "V" + mensajesConsumer2, false, new Timestamp(1L), 1);
		VersionsConf versionsResult = versionsConfDao.insertUpdate(versionJdbcTemplate, versionsconf);

		if (mensajesConsumer2 == 3) {
			throw new JMSException("Error 2");
		}

		Train train = new Train(String.valueOf(mensajesConsumer2), 1L, 2L, false, true, "atp");
		Train trainResult = trainDao.insertUpdate(tpjdbcTemplate, train);

		if (mensajesConsumer2 == 4) {
			throw new JMSException("Error 3");
		}		
		
		jmsTemplateRemote.convertAndSend("Queue2", message);

		if (mensajesConsumer2 == 5) {
			throw new JMSException("Error 4");
		}
		System.out.println("Tiempo total:"+(System.currentTimeMillis()-initTime));
	}

}