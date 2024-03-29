package net.trajano.temporal;

import net.trajano.temporal.sample.SampleTemporalEntity;
import net.trajano.temporal.sample.SampleTemporalEntityRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
@DataJpaTest(
  showSql = false
)
public class TemporalDataPatternApplicationTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private SampleTemporalEntityRepository repo;

    @Test
    public void contextLoads() {
    }

    @Test
    public void failed() {
        SampleTemporalEntity object = new SampleTemporalEntity();
        object.setKey("lookMeUp");
        object.setEffectiveOn(LocalDate.now());
        object.setProperty("hello");
        SampleTemporalEntity saved = repo.saveTemporal(object);

        em.flush();

        final Optional<SampleTemporalEntity> lookMeUp = repo.findByKeyAt(
          "lookMeUp",
          LocalDate.now().plusDays(-2)
        );
        assertFalse(lookMeUp.isPresent());

    }

    @Test(expected = ConstraintViolationException.class)
    public void failedValidation() {
        SampleTemporalEntity object = new SampleTemporalEntity();
        object.setProperty("hello");
        repo.saveTemporal(object);
        em.flush();
    }

    @Test
    public void insert() {
        SampleTemporalEntity object = new SampleTemporalEntity();
        object.setKey("lookMeUp");
        object.setEffectiveOn(LocalDate.now());
        object.setProperty("hello");
        SampleTemporalEntity saved = repo.saveTemporal(object);

        em.flush();

        final Optional<SampleTemporalEntity> byId = repo.findById(saved.getId());
        assertTrue(byId.isPresent());
        assertEquals("hello", byId.get().getProperty());
        assertNotNull(byId.get().getId());

        final Optional<SampleTemporalEntity> lookMeUp = repo.findByKeyAt(
          "lookMeUp",
          LocalDate.now().plusDays(2)
        );
        assertTrue(lookMeUp.isPresent());
        assertEquals("hello", lookMeUp.get().getProperty());
        assertNotNull(lookMeUp.get().getId());

        final Optional<SampleTemporalEntity> lookMeUpNow = repo.findByKey(
          "lookMeUp"
        );
        assertTrue(lookMeUpNow.isPresent());
        assertEquals("hello", lookMeUpNow.get().getProperty());
        assertNotNull(lookMeUpNow.get().getId());

        assertEquals(lookMeUpNow.get(), lookMeUp.get());
        assertEquals(lookMeUpNow.get(), saved);
        assertEquals(lookMeUpNow.get(), byId.get());

    }

    @Test
    public void insertWithKeys() {
        SampleTemporalEntity object = new SampleTemporalEntity();
        object.setKey("lookMeUp");
        object.setEffectiveOn(LocalDate.now());
        object.setProperty("hello");
        SampleTemporalEntity saved = repo.saveTemporal(object, "notWhatWasSet", LocalDate.now().minusDays(4));

        em.flush();

        final Optional<SampleTemporalEntity> lookMeUpNow = repo.findByKey(
          "lookMeUp"
        );
        assertFalse(lookMeUpNow.isPresent());

        final Optional<SampleTemporalEntity> theOneThatWasSet = repo.findByKey(
          "notWhatWasSet"
        );

        assertTrue(theOneThatWasSet.isPresent());
        assertEquals("hello", theOneThatWasSet.get().getProperty());
        assertNotNull(theOneThatWasSet.get().getId());

        assertEquals(theOneThatWasSet.get(), saved);

    }

}
