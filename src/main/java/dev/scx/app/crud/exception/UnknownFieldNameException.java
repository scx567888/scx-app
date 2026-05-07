package dev.scx.app.crud.exception;


import dev.scx.app.web.Result;
import dev.scx.http.exception.BadRequestException;

/**
 * UnknownFieldNameException
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class UnknownFieldNameException extends BadRequestException {

    public UnknownFieldNameException(String fieldName) {
        super(Result.fail("unknown-field-name").put("field-name", fieldName).toJson());
    }

}
