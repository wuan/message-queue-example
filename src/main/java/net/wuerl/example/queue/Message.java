package net.wuerl.example.queue;

import org.immutables.value.Value;

@Value.Immutable
public interface Message {
    int what();
    int arg1();
    int arg2();
    Object obj();
}
