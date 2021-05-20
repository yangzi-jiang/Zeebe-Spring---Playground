package io.camunda.cloudstarter;

import ch.qos.logback.classic.Logger;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.response.ProcessInstanceResult;
import io.zeebe.client.api.response.Topology;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.client.api.response.WorkflowInstanceResult;
import io.zeebe.client.api.worker.JobClient;
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

@SpringBootApplication
@EnableZeebeClient
@RestController
@ZeebeDeployment(classPathResources = {"supplier-service.bpmn"})
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

	@Scheduled(cron = "${cron.expression}", zone = "${cron.zone}")
	public void startPricingWorkflow() {
		if (!client.isRunning()) {
			return;
		}

		// Set time as the UUID for BPMN process ID
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();

		final ProcessInstanceResult event =
				client
						.newCreateInstanceCommand()
						.bpmnProcessId("{\"time\": \"" + dtf.format(now) + "\"}")
						.latestVersion()
						.withResult()
						.send()
						.join();

		log.info("started instance for workflowKey='{}', bpmnProcessId='{}', version='{}' with workflowInstanceKey='{}'",
				event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
	}
}
