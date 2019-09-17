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
    public void saveAndLoad() {
        SampleTemporalEntity sample = new SampleTemporalEntity();
        sample.setKey("myLookupKey");
        sample.setProperty("test");

        final SampleTemporalEntity saved = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/myLookupKey", port),
          sample,
          SampleTemporalEntity.class);
        assertThat(saved.getProperty(), is("test"));
        assertThat(saved.getKey(), is("myLookupKey"));

        final SampleTemporalEntity found = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/myLookupKey", port),
          SampleTemporalEntity.class);
        assertThat(found.getProperty(), is("test"));
        assertThat(found.getKey(), is("myLookupKey"));

        final SampleTemporalEntity foundWithDate = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/myLookupKey?at=%s", port, LocalDate.now().toString()),
          SampleTemporalEntity.class);
        assertThat(foundWithDate.getProperty(), is("test"));
        assertThat(foundWithDate.getKey(), is("myLookupKey"));

        final SampleTemporalEntity foundWithDateForTomorrow = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/myLookupKey?at=%s", port, LocalDate.now().plusDays(1).toString()),
          SampleTemporalEntity.class);
        assertThat(foundWithDateForTomorrow.getProperty(), is("test"));
        assertThat(foundWithDateForTomorrow.getKey(), is("myLookupKey"));
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
        sample.setKey("myLookupKey");
        sample.setProperty("test");

        final SampleTemporalEntity saved = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/myLookupKey", port),
          sample,
          SampleTemporalEntity.class);
        assertThat(saved.getProperty(), is("test"));
        assertThat(saved.getKey(), is("myLookupKey"));

        saved.setProperty("MyNewProperty");
        final SampleTemporalEntity updated = restTemplate.postForObject(
          String.format("http://localhost:%d/sample/myLookupKey", port),
          saved,
          SampleTemporalEntity.class);
        assertThat(updated.getProperty(), is("MyNewProperty"));
        assertThat(updated.getKey(), is("myLookupKey"));

        final SampleTemporalEntity found = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/myLookupKey", port),
          SampleTemporalEntity.class);
        assertThat(found.getProperty(), is("MyNewProperty"));
        assertThat(found.getKey(), is("myLookupKey"));

        final SampleTemporalEntity foundWithDate = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/myLookupKey?at=%s", port, LocalDate.now().toString()),
          SampleTemporalEntity.class);
        assertThat(foundWithDate.getProperty(), is("MyNewProperty"));
        assertThat(foundWithDate.getKey(), is("myLookupKey"));

        final SampleTemporalEntity foundWithDateForTomorrow = restTemplate.getForObject(
          String.format("http://localhost:%d/sample/myLookupKey?at=%s", port, LocalDate.now().plusDays(1).toString()),
          SampleTemporalEntity.class);
        assertThat(foundWithDateForTomorrow.getProperty(), is("MyNewProperty"));
        assertThat(foundWithDateForTomorrow.getKey(), is("myLookupKey"));
        assertThat(foundWithDateForTomorrow.getEffectiveOn(), is(LocalDate.now()));
        assertNotNull(foundWithDateForTomorrow.getId());
        assertThat(foundWithDateForTomorrow.getId(), is(foundWithDate.getId()));
        assertThat(foundWithDateForTomorrow.getId(), is(found.getId()));
        assertThat(foundWithDateForTomorrow, is(found));
        assertThat(foundWithDateForTomorrow, is(foundWithDate));
    }

}
