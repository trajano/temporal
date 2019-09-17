package net.trajano.temporal.web;

import net.trajano.temporal.sample.SampleTemporalEntity;
import net.trajano.temporal.sample.SampleTemporalEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

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
        final Optional<SampleTemporalEntity> optional;
        if (at == null) {
            optional = repository.findByKey(key);
        } else {
            optional = repository.findByKeyAt(key, at);
        }
        if (optional.isPresent()) {
            return Mono.just(optional.get());
        } else {
            throw new NotFoundException();
        }
    }

    @PostMapping("/{key}")
    public Mono<SampleTemporalEntity> save(
      @PathVariable final String key,
      @RequestBody final SampleTemporalEntity object) {
        object.setKey(key);
        if (object.getEffectiveOn() == null) {
            object.setEffectiveOn(LocalDate.now());
        }
        SampleTemporalEntity saved = repository.saveTemporal(object);
        return Mono.justOrEmpty(saved);
    }

}
