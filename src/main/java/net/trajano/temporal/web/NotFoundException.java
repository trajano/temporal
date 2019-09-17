package net.trajano.temporal.web;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND, reason = "Not found")
class NotFoundException extends RuntimeException {

}
