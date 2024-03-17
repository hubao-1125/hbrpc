package io.github.hubao.hbrpccore.registry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {

    List<String> data;
}
