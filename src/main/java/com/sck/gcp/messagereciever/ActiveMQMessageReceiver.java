package com.sck.gcp.messagereciever;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.Message;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.sck.gcp.gateway.PubSubOutboundGateway;
import com.sck.gcp.processor.FileProcessor;
import com.sck.gcp.service.CloudStorageService;

@Component
public class ActiveMQMessageReceiver {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQMessageReceiver.class);

	@Autowired
	private FileProcessor fileProcessor;


	@Value("${com.sck.upload.backup.dir:backup}")
	private String backupFolder;

	
	@Autowired
	private CloudStorageService cloudStorageService;
	
	@Autowired
	private PubSubOutboundGateway messagingGateway;

	@JmsListener(destination = "${inbound.endpoint}", containerFactory = "jmsListenerContainerFactory")
	public void receiveMessage(Message msg) throws JMSException {
		try {
			String xml = ((TextMessage) msg).getText();
			LOGGER.info("readFile() completed");

			JSONArray jsonProducts = fileProcessor.convertToJSONs(xml);
			String jsonl = fileProcessor.convertToJSONL(jsonProducts);
			LOGGER.info("convertToJSONL() completed");

			String uploadFile = cloudStorageService.getFileName("product");
			String backupFilePath = cloudStorageService.getFilePath(backupFolder, uploadFile);
			
			byte[] arr = jsonl.getBytes();
			cloudStorageService.uploadToCloudStorage(backupFilePath, arr);
			LOGGER.info("File uploaded to bucket with name " + backupFilePath);
			
			jsonProducts.forEach(i -> messagingGateway.sendToPubSub(i.toString()));
			LOGGER.info("Published JSONs to PubSub ");

		} catch (JMSException e) {
			LOGGER.error("JMSException occured", e);
		} 
	}

}
