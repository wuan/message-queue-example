package net.wuerl.example.queue;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Modifiable
public interface Node {
    Optional<Message> message();
    Optional<ModifiableNode> next();
}
