package io.camunda.cloudstarter;

import ch.qos.logback.classic.Logger;
import io.zeebe.client.api.response.ActivatedJob;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

	// An endpoint to start a new instance of the workflow
	@GetMapping("/start")
	public String startWorkflowInstance() {
		WorkflowInstanceResult workflowInstanceResult = client
				.newCreateInstanceCommand()
				.bpmnProcessId("test-process")
				.latestVersion()
				.variables("{\"name\": \"Yangzi Jiang\"}")
				.withResult()
				.send()
				.join();
		return (String) workflowInstanceResult
				.getVariablesAsMap()
				.getOrDefault("say", "Error: No greeting returned");
	}

	@ZeebeWorker(type = "get-completion-status")
	public void handleGetTime(final JobClient client, final ActivatedJob job) {
		final String uri = "https://json-api.joshwulf.com/time";

		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(uri, String.class);

		client.newCompleteCommand(job.getKey())
				.variables("{\"time\":" + result + "}")
				.send().join();
	}

	@ZeebeWorker(type = "make-greeting")
	public void handleMakeGreeting(final JobClient client, final ActivatedJob job) {
		Map<String, String> headers = job.getCustomHeaders();
		String greeting = headers.getOrDefault("greeting", "Good day");
		Map<String, Object> variablesAsMap = job.getVariablesAsMap();
		String name = (String) variablesAsMap.getOrDefault("name", "there");
		String say = greeting + " " + name;
		client.newCompleteCommand(job.getKey())
				.variables("{\"say\": \"" + say + "\"}")
				.send().join();
	}
}
