package net.trajano.temporal;

import net.trajano.temporal.anemic.AnemicTemporal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
@AutoConfigureTestDatabase
@AutoConfigureWebTestClient
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = TemporalDataPatternApplication.class
)
public class AnemicWebTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        assertNotNull(restTemplate);
    }

    @Test
    public void notFound() {
        final ResponseEntity<AnemicTemporal> result = restTemplate.getForEntity(
          String.format("http://localhost:%d/anemic/not-found", port),
          AnemicTemporal.class);

        assertThat(result.getStatusCodeValue(), is(404));
    }

    @Test
    public void saveAndLoad() {
        AnemicTemporal sample = new AnemicTemporal();
        sample.setKey("saveAndLoad");
        sample.setAdditionalAttribute("hello", "test");

        final AnemicTemporal saved = restTemplate.postForObject(
          String.format("http://localhost:%d/anemic/saveAndLoad", port),
          sample,
          AnemicTemporal.class);
        assertThat(saved.getAdditionalAttribute("hello"), is("test"));
        assertThat(saved.getKey(), is("saveAndLoad"));

        final AnemicTemporal found = restTemplate.getForObject(
          String.format("http://localhost:%d/anemic/saveAndLoad", port),
          AnemicTemporal.class);
        assertThat(found.getAdditionalAttribute("hello"), is("test"));
        assertThat(found.getKey(), is("saveAndLoad"));
    }

}
