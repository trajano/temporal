package net.trajano.temporal;

import net.trajano.temporal.sample.SampleTemporalEntity;
import net.trajano.temporal.sample.SampleTemporalObjectRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
@DataJpaTest
public class TemporalDataPatternApplicationTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private SampleTemporalObjectRepo repo;

    @Test
    public void contextLoads() {
    }

    @Test
    public void failed() {
        SampleTemporalEntity object = new SampleTemporalEntity();
        object.setKey("lookMeUp");
        object.setEffectiveOn(LocalDate.now());
        object.setProperty("hello");
        SampleTemporalEntity saved = repo.save(object);

        em.flush();

        final Optional<SampleTemporalEntity> lookMeUp = repo.findTemporally(
          "lookMeUp",
          LocalDate.now().plusDays(-2),
          SampleTemporalEntity.class);
        assertFalse(lookMeUp.isPresent());

    }

    @Test
    public void insert() {
        SampleTemporalEntity object = new SampleTemporalEntity();
        object.setKey("lookMeUp");
        object.setEffectiveOn(LocalDate.now());
        object.setProperty("hello");
        SampleTemporalEntity saved = repo.save(object);

        em.flush();

        final Optional<SampleTemporalEntity> byId = repo.findById(saved.getId());
        assertTrue(byId.isPresent());
        assertEquals("hello", byId.get().getProperty());
        assertNotNull(byId.get().getId());

        final Optional<SampleTemporalEntity> lookMeUp = repo.findTemporally(
          "lookMeUp",
          LocalDate.now().plusDays(2),
          SampleTemporalEntity.class
        );
        assertTrue(lookMeUp.isPresent());
        assertEquals("hello", lookMeUp.get().getProperty());
        assertNotNull(lookMeUp.get().getId());

    }

}
