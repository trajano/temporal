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
public class SampleWebTest {

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

    @Test
    public void saveAndUpdate() {
        SampleTemporalEntity sample = new SampleTemporalEntity();
        sample.setKey("saveAndUpdate");
        sample.setProperty("test");

        final SampleTemporalEntity saved = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/saveAndUpdate", port),
          sample,
          SampleTemporalEntity.class);
        assertThat(saved.getProperty(), is("test"));
        assertThat(saved.getKey(), is("saveAndUpdate"));

        saved.setProperty("newValue");
        final SampleTemporalEntity updated = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/saveAndUpdate", port),
          saved,
          SampleTemporalEntity.class);
        assertThat(updated.getProperty(), is("newValue"));
        assertThat(updated.getKey(), is("saveAndUpdate"));

        final SampleTemporalEntity found = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/saveAndUpdate", port),
          SampleTemporalEntity.class);
        assertThat(found.getProperty(), is("newValue"));
        assertThat(found.getKey(), is("saveAndUpdate"));

    }

    @Test
    public void saveAndUpdateAdditionalProperties() {
        SampleTemporalEntity sample = new SampleTemporalEntity();
        sample.setKey("saveAndUpdateAdditionalProperties");
        sample.setProperty("test");
        sample.setAdditionalAttribute("abc", "123");

        final SampleTemporalEntity saved = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/saveAndUpdateAdditionalProperties", port),
          sample,
          SampleTemporalEntity.class);
        assertThat(saved.getProperty(), is("test"));
        assertThat(saved.getKey(), is("saveAndUpdateAdditionalProperties"));
        assertThat(saved.getAdditionalAttribute("abc"), is("123"));

        saved.setProperty("newValue");
        saved.setAdditionalAttribute("abc", "456");
        saved.setAdditionalAttribute("do", "re-mi");
        final SampleTemporalEntity updated = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/saveAndUpdateAdditionalProperties", port),
          saved,
          SampleTemporalEntity.class);
        assertThat(updated.getProperty(), is("newValue"));
        assertThat(updated.getKey(), is("saveAndUpdateAdditionalProperties"));

        final SampleTemporalEntity found = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/saveAndUpdateAdditionalProperties", port),
          SampleTemporalEntity.class);
        assertThat(found.getProperty(), is("newValue"));
        assertThat(found.getKey(), is("saveAndUpdateAdditionalProperties"));
        assertThat(found.getAdditionalAttribute("abc"), is("456"));
        assertThat(found.getAdditionalAttribute("do"), is("re-mi"));

    }

}
