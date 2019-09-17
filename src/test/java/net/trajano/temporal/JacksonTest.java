package net.trajano.temporal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.trajano.temporal.anemic.AnemicTemporal;
import net.trajano.temporal.sample.SampleTemporalEntity;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

@Slf4j
public class JacksonTest {

    @Test
    public void mapAdditionalProperties() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SampleTemporalEntity test = new SampleTemporalEntity();
        test.setEffectiveOn(LocalDate.now());
        test.setAdditionalAttribute("hello", "world");

        final String json = mapper.writer().writeValueAsString(test);
        log.debug(json);
        SampleTemporalEntity read = mapper
          .readerFor(SampleTemporalEntity.class)
          .readValue(json);
        assertEquals("world", read.getAdditionalAttributes().get("hello"));
        assertEquals(LocalDate.now(), read.getEffectiveOn());

    }

    @Test
    public void mapAdditionalPropertiesOnAnemic() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        AnemicTemporal test = new AnemicTemporal();
        final ZonedDateTime now = ZonedDateTime.now();
        test.setEffectiveOn(now);
        test.setAdditionalAttribute("hello", "world");

        final String json = mapper.writer().writeValueAsString(test);
        log.debug(json);
        AnemicTemporal read = mapper
          .readerFor(AnemicTemporal.class)
          .readValue(json);
        assertEquals("world", read.getAdditionalAttributes().get("hello"));
        assertEquals(now.toInstant(), read.getEffectiveOn().toInstant());

    }

}
