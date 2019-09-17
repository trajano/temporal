package net.trajano.temporal.web;

import net.trajano.temporal.anemic.AnemicTemporal;
import net.trajano.temporal.anemic.AnemicTemporalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/anemic")
public class AnemicTemporalController {

    @Autowired
    private AnemicTemporalRepository repository;

    @GetMapping("/{key}")
    public Mono<AnemicTemporal> getByKey(
      @PathVariable final String key,
      @RequestParam(
        name = "at",
        required = false
      ) ZonedDateTime at
    ) {
        final Optional<AnemicTemporal> optional;
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
    public Mono<AnemicTemporal> save(
      @PathVariable final String key,
      @RequestBody final AnemicTemporal object) {
        object.setKey(key);
        if (object.getEffectiveOn() == null) {
            object.setEffectiveOn(ZonedDateTime.now());
        }
        AnemicTemporal saved = repository.saveTemporal(object);
        return Mono.justOrEmpty(saved);
    }

}
