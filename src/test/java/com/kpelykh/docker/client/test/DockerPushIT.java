package com.kpelykh.docker.client.test;


import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.CommitConfig;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

import static com.kpelykh.docker.client.DockerClient.asString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// delete here : https://index.docker.io/u/alexec/busybox/delete/
public class DockerPushIT extends AbstractDockerClientIT {
	
	public static final Logger LOG = LoggerFactory
			.getLogger(DockerPushIT.class);

    String username;

	@BeforeTest
	public void beforeTest() throws DockerException {
		super.beforeTest();
        username = dockerClient.authConfig().getUsername();
	}
	@AfterTest
	public void afterTest() {
		super.afterTest();
	}

	@BeforeMethod
	public void beforeMethod(Method method) {
	    super.beforeMethod(method);
	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		super.afterMethod(result);
	}

	@Test
	public void testPushLatest() throws Exception {

		
		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setImage("busybox");
		containerConfig.setCmd(new String[] { "true" });

		ContainerCreateResponse container = dockerClient
				.createContainer(containerConfig);

		LOG.info("Created container {}", container.toString());

		assertThat(container.getId(), not(isEmptyString()));

		tmpContainers.add(container.getId());
		
		LOG.info("Commiting container: {}", container.toString());
		CommitConfig commitConfig = new CommitConfig(container.getId());

        commitConfig.setRepo(username + "/busybox");
		
		String imageId = dockerClient.commit(commitConfig);

		logResponseStream(dockerClient.push(username + "/busybox"));
		
		dockerClient.removeImage(imageId);
		
		assertThat(asString(dockerClient.pull(username + "/busybox")), not(containsString("404")));
	}

	@Test
	public void testNotExistentImage() throws Exception {

		assertThat(logResponseStream(dockerClient.push(username + "/xxx")), containsString("error"));
	}

	
}

