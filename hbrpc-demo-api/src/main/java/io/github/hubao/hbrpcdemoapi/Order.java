package io.github.hubao.hbrpcdemoapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    Long id;
    Float amount;

}
