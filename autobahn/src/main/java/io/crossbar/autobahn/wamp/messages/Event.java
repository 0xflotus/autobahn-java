package io.crossbar.autobahn.wamp.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crossbar.autobahn.wamp.exceptions.ProtocolError;
import io.crossbar.autobahn.wamp.interfaces.IMessage;

public class Event implements IMessage {

    public static final int MESSAGE_TYPE = 36;

    public final long subscription;
    public final long publication;
    public final List<Object> args;
    public final Map<String, Object> kwargs;

    public Event(long subscription, long publication, List<Object> args, Map<String, Object> kwargs) {
        this.subscription = subscription;
        this.publication = publication;
        this.args = args;
        this.kwargs = kwargs;
    }

    public static Event parse(List<Object> wmsg) {
        if (wmsg.size() == 0 || !(wmsg.get(0) instanceof Integer) || (int) wmsg.get(0) != MESSAGE_TYPE) {
            throw new IllegalArgumentException("Invalid message.");
        }

        if (wmsg.size() < 3 || wmsg.size() > 6) {
            throw new ProtocolError(String.format("invalid message length %s for EVENT", wmsg.size()));
        }

        long subscription = (long) wmsg.get(1);
        long publication = (long) wmsg.get(2);
        Map<String, Object> details = (Map<String, Object>) wmsg.get(3);
        List<Object> args = null;
        if (wmsg.size() > 4) {
            if (wmsg.get(4) instanceof byte[]) {
                throw new ProtocolError("Binary payload not supported");
            }
            args = (List<Object>) wmsg.get(4);
        }
        Map<String, Object> kwargs = null;
        if (wmsg.size() > 5) {
            kwargs = (Map<String, Object>) wmsg.get(5);
        }
        return new Event(subscription, publication, args, kwargs);
    }

    @Override
    public List<Object> marshal() {
        List<Object> marshaled = new ArrayList<>();
        marshaled.add(MESSAGE_TYPE);
        marshaled.add(subscription);
        marshaled.add(publication);
        // Empty details.
        marshaled.add(new HashMap<>());
        if (kwargs != null) {
            if (args == null) {
                // Empty args.
                marshaled.add(new ArrayList<String>());
            } else {
                marshaled.add(args);
            }
            marshaled.add(kwargs);
        } else if (args != null) {
            marshaled.add(args);
        }
        return marshaled;
    }
}
