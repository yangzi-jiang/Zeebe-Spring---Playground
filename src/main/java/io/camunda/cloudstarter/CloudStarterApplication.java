package io.camunda.cloudstarter;

import ch.qos.logback.classic.Logger;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.response.ProcessInstanceResult;
import io.zeebe.client.api.response.Topology;
import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.ZeebeClientLifecycle;
import io.zeebe.spring.client.annotation.ZeebeDeployment;
import io.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableZeebeClient
@RestController
@ZeebeDeployment(classPathResources = "pricing-monitoring-2.bpmn")
public class CloudStarterApplication {
	@Autowired
	private ZeebeClientLifecycle client;
	org.slf4j.Logger logger = LoggerFactory.getLogger(CloudStarterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CloudStarterApplication.class, args);
	}

	// Creates a REST endpoint
	// Request a topology from Zeebe cluster
	@GetMapping("/status")
	public String getStatus() {
		Topology topology = client.newTopologyRequest().send().join();
		return topology.toString();
	}
//
//	@Scheduled(cron = "${cron.expression}", zone = "${cron.zone}")
//	public void startPricingWorkflow() throws InterruptedException {
//		if (!client.isRunning()) {
//			return;
//		}
//
//		// Set time as the UUID for BPMN process ID
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
//		LocalDateTime now = LocalDateTime.now();
//
//		final ProcessInstanceResult event =
//				client
//						.newCreateInstanceCommand()
//						.bpmnProcessId("{\"time\": \"" + dtf.format(now) + "\"}")
//						.latestVersion()
//						.withResult()
//						.send()
//						.join();
//
//		logger.info("started instance for workflowKey='{}', bpmnProcessId='{}', version='{}' with workflowInstanceKey='{}'",
//				event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
//
//		// Prepare variable for data flow in Zeebe processes
//		final FetchStatus fetchStatus = new FetchStatus();
//		fetchStatus.setFetchStatusID(UUID.randomUUID());
//		fetchStatus.setCustomerName("yangzi");
//		fetchStatus.setBusinessLabel("yangzi's workflow #1");
//		fetchStatus.setCatalogFileName("N/A");
//
//		client
//				.newPublishMessageCommand()
//				.messageName("fetching").correlationKey("correlationValue")
//				.variables(fetchStatus)
//				.send().join();
//
//
//		TimeUnit.SECONDS.sleep(5);
//
//		// Send message to the Message Receive Task in the Event Sub Process
//		fetchStatus.setFetchComplete(Boolean.TRUE);
//		client
//				.newPublishMessageCommand()
//				.messageName("Pricing Integration Running").correlationKey(fetchStatus.getBusinessLabel())
//				.variables(fetchStatus)
//				.send().join();
//	}
}
