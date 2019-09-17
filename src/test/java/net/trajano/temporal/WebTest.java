package net.trajano.temporal;

import net.trajano.temporal.sample.SampleTemporalEntity;
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

import java.time.LocalDate;

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
public class WebTest {

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
        final ResponseEntity<SampleTemporalEntity> result = restTemplate.getForEntity(
          String.format("http://localhost:%d/sample/not-found", port),
          SampleTemporalEntity.class);

        assertThat(result.getStatusCodeValue(), is(404));
    }

    @Test
    public void saveAndLoad() {
        SampleTemporalEntity sample = new SampleTemporalEntity();
        sample.setKey("saveAndLoad");
        sample.setProperty("test");

        final SampleTemporalEntity saved = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/saveAndLoad", port),
          sample,
          SampleTemporalEntity.class);
        System.out.println(saved);
        assertThat(saved.getProperty(), is("test"));
        assertThat(saved.getKey(), is("saveAndLoad"));

        final SampleTemporalEntity found = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/saveAndLoad", port),
          SampleTemporalEntity.class);
        assertThat(found.getProperty(), is("test"));
        assertThat(found.getKey(), is("saveAndLoad"));

        final SampleTemporalEntity foundWithDate = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/saveAndLoad?at=%s", port, LocalDate.now().toString()),
          SampleTemporalEntity.class);
        assertThat(foundWithDate.getProperty(), is("test"));
        assertThat(foundWithDate.getKey(), is("saveAndLoad"));

        final SampleTemporalEntity foundWithDateForTomorrow = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/saveAndLoad?at=%s", port, LocalDate.now().plusDays(1).toString()),
          SampleTemporalEntity.class);
        assertThat(foundWithDateForTomorrow.getProperty(), is("test"));
        assertThat(foundWithDateForTomorrow.getKey(), is("saveAndLoad"));
        assertThat(foundWithDateForTomorrow.getEffectiveOn(), is(LocalDate.now()));
        assertNotNull(foundWithDateForTomorrow.getId());
        assertThat(foundWithDateForTomorrow.getId(), is(foundWithDate.getId()));
        assertThat(foundWithDateForTomorrow.getId(), is(found.getId()));
        assertThat(foundWithDateForTomorrow, is(found));
        assertThat(foundWithDateForTomorrow, is(foundWithDate));
    }

}
