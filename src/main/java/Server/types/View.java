package Server.types;

import Server.Request;
import Server.Responses.Response;

@FunctionalInterface
public interface View {
    Response apply(Request request);
}
