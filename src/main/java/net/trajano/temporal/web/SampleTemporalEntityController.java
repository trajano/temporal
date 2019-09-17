package net.trajano.temporal.web;

import net.trajano.temporal.sample.SampleTemporalEntity;
import net.trajano.temporal.sample.SampleTemporalEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/sample")
public class SampleTemporalEntityController {

    @Autowired
    private SampleTemporalEntityRepository repository;

    @GetMapping("/{key}")
    public Mono<SampleTemporalEntity> getByKey(
      @PathVariable final String key,
      @RequestParam(
        name = "at",
        required = false
      ) LocalDate at
    ) {
        if (at == null) {
            return Mono.justOrEmpty(repository.findByKey(key));
        } else {
            return Mono.justOrEmpty(repository.findByKeyAt(key, at));
        }
    }

    @PostMapping("/{key}")
    public Mono<SampleTemporalEntity> save(
      @PathVariable final String key,
      @RequestBody final SampleTemporalEntity object) {
        object.setId(null);
        object.setKey(key);
        if (object.getEffectiveOn() == null) {
            object.setEffectiveOn(LocalDate.now());
        }
        SampleTemporalEntity saved = repository.saveChecked(object, SampleTemporalEntity.class);
        return Mono.justOrEmpty(saved);
    }

}
